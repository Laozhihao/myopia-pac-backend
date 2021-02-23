package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.UnreadNoticeResponse;
import com.wupol.myopia.business.management.domain.mapper.NoticeMapper;
import com.wupol.myopia.business.management.domain.model.Notice;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知Service
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class NoticeService extends BaseService<NoticeMapper, Notice> {

    /**
     * 获取通知列表
     *
     * @param pageRequest 分页入参
     * @param currentUser 当前登录用户
     * @return {@link IPage} List<Notice>
     */
    public IPage<Notice> getLists(PageRequest pageRequest, CurrentUser currentUser) {
        return baseMapper.getByNoticeUserId(pageRequest.toPage(), currentUser.getId());
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

        // 通过类型分组
        Map<Byte, List<Notice>> noticeMaps = notices.stream()
                .collect(Collectors.groupingBy(Notice::getType));

        // 站内信
        List<Notice> stationLetters = noticeMaps.get(CommonConst.NOTICE_STATION_LETTER);
        if (!CollectionUtils.isEmpty(stationLetters)) {
            response.setStationLetter(stationLetters);
        }

        // 筛查通知
        List<Notice> screeningNotices = noticeMaps.get(CommonConst.NOTICE_SCREENING_NOTICE);
        if (!CollectionUtils.isEmpty(screeningNotices)) {
            response.setScreeningNotice(screeningNotices);
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
     * @param title        标题
     * @param content      内容
     * @param downloadUrl  文件url
     */
    @Transactional(rollbackFor = Exception.class)
    public void createExportNotice(Integer createUserId, String title, String content, String downloadUrl) {
        Notice notice = new Notice();
        notice.setCreateUserId(createUserId);
        notice.setNoticeUserId(createUserId);
        notice.setType(CommonConst.NOTICE_STATION_LETTER);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setDownloadUrl(downloadUrl);
        baseMapper.insert(notice);
    }

    /**
     * 批量创建筛查通知
     *  @param createUserId 创建人
     * @param linkId       关联ID(比如：筛查通知的表ID)
     * @param toUserIds    需要通知的用户
     * @param title        标题
     * @param content      内容
     * @param startTime
     * @param endTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateScreeningNotice(Integer createUserId, Integer linkId, List<Integer> toUserIds, String title, String content, Date startTime, Date endTime) {
        baseMapper.batchCreateScreeningNotice(createUserId, linkId, toUserIds,
                CommonConst.NOTICE_SCREENING_NOTICE, title, content, startTime, endTime);
    }
}
