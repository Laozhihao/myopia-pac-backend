package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.mapper.NoticeMapper;
import com.wupol.myopia.business.management.domain.model.Notice;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    /**
     * 获取通知列表
     *
     * @param pageRequest 分页入参
     * @param currentUser 当前登录用户
     * @return {@link IPage} List<Notice>
     */
    public IPage<Notice> getLists(PageRequest pageRequest, CurrentUser currentUser) {
        return baseMapper.getByUserId(pageRequest.toPage(), currentUser.getId());
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
    public Object deletedNotice(List<Integer> ids) {
        return baseMapper.batchUpdateStatus(ids, CommonConst.STATUS_NOTICE_DELETED) > 0;
    }

    /**
     * 获取用户未读个数
     *
     * @param currentUser 当前用户
     * @return 未读个数
     */
    public Integer unreadCount(CurrentUser currentUser) {
        return baseMapper.unreadCount(CommonConst.STATUS_NOTICE_UNREAD, currentUser.getId());
    }
}
