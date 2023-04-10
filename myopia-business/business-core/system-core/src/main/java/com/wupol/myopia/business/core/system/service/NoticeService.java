package com.wupol.myopia.business.core.system.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.system.domain.dos.AsyncExportNoticeDO;
import com.wupol.myopia.business.core.system.domain.dto.UnreadNoticeResponse;
import com.wupol.myopia.business.core.system.domain.mapper.NoticeMapper;
import com.wupol.myopia.business.core.system.domain.model.Notice;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知Service
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class NoticeService extends BaseService<NoticeMapper, Notice> {

    @Resource
    private ResourceFileService resourceFileService;

    @Autowired
    private OauthServiceClient oauthServiceClient;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取通知列表
     *
     * @param pageRequest 分页入参
     * @param currentUser 当前登录用户
     * @return {@link IPage} List<Notice>
     */
    public IPage<Notice> getLists(PageRequest pageRequest, CurrentUser currentUser) {
        IPage<Notice> noticeIPage = baseMapper.getByNoticeUserId(pageRequest.toPage(), currentUser.getId());
        List<Notice> records = noticeIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return noticeIPage;
        }
        records.forEach(notice -> {
            if (null != notice.getFileId()) {
                notice.setDownloadUrl(resourceFileService.getResourcePath(notice.getFileId()));
            }
        });
        return noticeIPage;
    }

    /**
     * 批量已读
     *
     * @param ids 通知ID
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean readNotice(List<Integer> ids) {
        return baseMapper.batchUpdateStatus(ids, CommonConst.STATUS_NOTICE_READ) > 0;
    }

    /**
     * 批量删除信息
     *
     * @param ids 通知ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletedNotice(List<Integer> ids) {
        return baseMapper.batchUpdateStatus(ids, CommonConst.STATUS_NOTICE_DELETED) > 0;
    }

    /**
     * 获取用户未读个数
     *
     * @param currentUser 当前用户
     * @return 未读列表
     */
    public UnreadNoticeResponse unreadCount(CurrentUser currentUser) {
        UnreadNoticeResponse response = new UnreadNoticeResponse();

        // 查找当前用户所有未读的
        List<Notice> notices = baseMapper.unreadCount(CommonConst.STATUS_NOTICE_UNREAD, currentUser.getId());
        response.setTotal(notices.size());

        if (!CollectionUtils.isEmpty(notices)) {
            // 站内信
            response.setStationLetter(notices.stream().filter(notice -> notice.getType().equals(CommonConst.NOTICE_STATION_LETTER)).collect(Collectors.toList()));
            // 筛查通知
            response.setScreeningNotice(notices.stream().filter(notice -> !notice.getType().equals(CommonConst.NOTICE_STATION_LETTER)).collect(Collectors.toList()));
        }
        return response;
    }

    /**
     * 通知已读
     *
     * @param currentUser       当前用户
     * @param screeningNoticeId 筛查通知ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean readNotice(CurrentUser currentUser, Integer screeningNoticeId) {
        return baseMapper.updateNotice(currentUser.getId(), screeningNoticeId) > 0;
    }

    /**
     * 批量创建筛查通知
     *
     * @param createUserId 创建人
     * @param linkId       关联ID(比如：筛查通知的表ID)
     * @param toUserIds    需要通知的用户
     * @param type         类型
     * @param title        标题
     * @param content      内容
     * @param startTime    开始时间
     * @param endTime      结束时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateNotice(Integer createUserId, Integer linkId, List<Integer> toUserIds, Byte type, String title, String content, Date startTime, Date endTime) {
        baseMapper.batchCreateNotice(createUserId, linkId, toUserIds,
                type, title, content, startTime, endTime);
    }

    /**
     * 发送导出成功通知
     *
     * @param createUserId 创建用户ID
     * @param noticeUserId 通知用户ID
     * @param keyContent   关键内容
     * @param fileId       导出文件ID
     * @return void
     **/
    public void sendExportSuccessNotice(Integer createUserId, Integer noticeUserId, String keyContent, Integer fileId) {
        String fullContent = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, keyContent, new Date());
        createExportNotice(createUserId, noticeUserId, fullContent, fullContent, fileId, CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 发送导出失败通知
     *
     * @param createUserId 创建用户ID
     * @param noticeUserId 通知用户ID
     * @param keyContent   关键内容
     * @return void
     **/
    public void sendExportFailNotice(Integer createUserId, Integer noticeUserId, String keyContent) {
        String fullContent = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_FAILURE, keyContent);
        createExportNotice(createUserId, noticeUserId, fullContent, fullContent, null, CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 发送信息给所有平台管理员（不发送给自己）
     *
     * @param createUserId
     * @param title
     * @param content
     * @param type
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendNoticeToAllAdmin(Integer createUserId, String title, String content, Byte type) {
        UserDTO userDTO = new UserDTO();
        userDTO.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setUserType(UserType.PLATFORM_ADMIN.getType());
        List<User> userList = oauthServiceClient.getUserList(userDTO);
        List<Integer> userIds = userList.stream().map(User::getId).filter(userId -> !userId.equals(createUserId)).collect(Collectors.toList());
        batchCreateNotice(createUserId, null, userIds, type, title, content, null, null);
    }

    /**
     * 异步导出发送异常站内信
     *
     * @param exportUuid     导出Uuid
     * @param pdfGeneratorVO PDF生成Redis对象
     */
    public void sendErrorNotice(String exportUuid, PdfGeneratorVO pdfGeneratorVO) {
        String noticeKey = String.format(RedisConstant.FILE_EXPORT_ASYNC_TASK_ERROR_NOTICE, exportUuid);
        AsyncExportNoticeDO notice = (AsyncExportNoticeDO) redisUtil.get(noticeKey);

        // 如果通知过，则不需要在发通知
        if (Objects.nonNull(notice)) {
            return;
        }
        log.info("发送异步导出任务通知:{}", JSON.toJSONString(pdfGeneratorVO));
        sendExportFailNotice(pdfGeneratorVO.getUserId(), pdfGeneratorVO.getUserId(), "【导出失败】，" + pdfGeneratorVO.getZipFileName() + "请稍后重试");
        // 默认给一天内处理
        redisUtil.set(noticeKey, new AsyncExportNoticeDO(), 86400);
    }

    /**
     * 导出Excel-通知
     *
     * @param createUserId 创建人
     * @param noticeUserId 通知人
     * @param title        标题
     * @param content      内容
     * @param fileId       资源文件ID
     * @param type         类型
     */
    public void createExportNotice(Integer createUserId, Integer noticeUserId, String title, String content, Integer fileId, Byte type) {
        createNotice(createUserId, noticeUserId, title, content, fileId, type);
    }

    /**
     * 关联通知
     *
     * @param createUserId 创建人
     * @param content      内容
     */
    public void createNoticeLinkResult(Integer createUserId, String content) {
        createNotice(createUserId, createUserId, content, content, null, CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 新建通知
     *
     * @param createUserId 创建人
     * @param noticeUserId 通知人
     * @param title        标题
     * @param content      内容
     * @param fileId       资源文件ID
     * @param type         类型
     */
    public void createNotice(Integer createUserId, Integer noticeUserId, String title, String content, Integer fileId, Byte type) {
        Notice notice = new Notice();
        notice.setCreateUserId(createUserId);
        notice.setNoticeUserId(noticeUserId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setFileId(fileId);
        baseMapper.insert(notice);
    }

}
