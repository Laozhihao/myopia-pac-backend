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


    private static List<Integer> str2List(String string) {
        return Arrays.stream(string.split(",")).map(Integer::valueOf).collect(Collectors.toList());
    }

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
    public Boolean readNotice(String ids) {
        return baseMapper.batchUpdateStatus(str2List(ids), CommonConst.READ_NOTICE) > 0;
    }

    /**
     * 批量删除信息
     *
     * @param ids 通知ID
     * @return 是否删除成功
     */
    public Object deletedNotice(String ids) {
        return baseMapper.batchUpdateStatus(str2List(ids), CommonConst.DELETED_NOTICE) > 0;
    }
}
