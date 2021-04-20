package com.wupol.myopia.business.core.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.system.domain.dto.UnreadNoticeResponse;
import com.wupol.myopia.business.core.system.domain.mapper.NoticeMapper;
import com.wupol.myopia.business.core.system.domain.model.Notice;
import com.wupol.myopia.business.management.constant.CommonConst;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
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
     * 筛查通知已读
     *
     * @param currentUser       当前用户
     * @param screeningNoticeId 筛查通知ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean readScreeningNotice(CurrentUser currentUser, Integer screeningNoticeId) {
        return baseMapper.updateScreeningNotice(currentUser.getId(), screeningNoticeId) > 0;
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
    @Transactional(rollbackFor = Exception.class)
    public void createExportNotice(Integer createUserId, Integer noticeUserId, String title, String content, Integer fileId, Byte type) {
        Notice notice = new Notice();
        notice.setCreateUserId(createUserId);
        notice.setNoticeUserId(noticeUserId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setFileId(fileId);
        baseMapper.insert(notice);
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
    public void batchCreateScreeningNotice(Integer createUserId, Integer linkId, List<Integer> toUserIds, Byte type, String title, String content, Date startTime, Date endTime) {
        baseMapper.batchCreateScreeningNotice(createUserId, linkId, toUserIds,
                type, title, content, startTime, endTime);
    }

    /**
     * 发送导出成功通知
     *
     * @param createUserId 创建用户ID
     * @param noticeUserId 通知用户ID
     * @param keyContent 关键内容
     * @param fileId 导出文件ID
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
     * @param keyContent 关键内容
     * @return void
     **/
    public void sendExportFailNotice(Integer createUserId, Integer noticeUserId, String keyContent) {
        String fullContent = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_FAILURE, keyContent);
        createExportNotice(createUserId, noticeUserId, fullContent, fullContent, null, CommonConst.NOTICE_STATION_LETTER);
    }
}
