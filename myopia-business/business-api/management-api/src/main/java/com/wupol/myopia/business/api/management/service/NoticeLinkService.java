package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.common.utils.domain.dto.LinkNoticeQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知关联
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class NoticeLinkService {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private NoticeLinkBizService noticeLinkBizService;

    /**
     * 迁移学生数据
     */
    public void migratingStudentData() {
        List<Object> queueList = redisUtil.lGetAll(RedisConstant.NOTICE_LINK_LIST);
        if (CollectionUtils.isEmpty(queueList)) {
            log.info("暂无需要关联的学生");
            return;
        }
        for (int i = 0; i < queueList.size(); i++) {
            log.info("一共需要关联:{}个计划，当前第:{}个", queueList.size(), i + 1);
            LinkNoticeQueue linkNoticeQueue = (LinkNoticeQueue) queueList.get(i);
            try {
                noticeLinkBizService.noticeLinkStudentMigrating(linkNoticeQueue);
            } catch (Exception e) {
                log.info("关联通知-迁移学生存在异常:{},关联通知数据:{}", e, JSON.toJSONString(linkNoticeQueue));
                redisUtil.lSet(RedisConstant.NOTICE_LINK_ERROR_LIST, JSON.toJSONString(linkNoticeQueue));
            }
        }
        redisUtil.del(RedisConstant.NOTICE_LINK_LIST);
    }

    /**
     * 处理关联存在异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleErrorLinkList() {
        List<Object> queueList = redisUtil.lGetAll(RedisConstant.NOTICE_LINK_ERROR_LIST);
        queueList.forEach(q -> {
            LinkNoticeQueue linkNoticeQueue = (LinkNoticeQueue) q;
            noticeLinkBizService.noticeLinkStudentMigrating(linkNoticeQueue);
        });
        redisUtil.del(RedisConstant.NOTICE_LINK_ERROR_LIST);
    }
}
