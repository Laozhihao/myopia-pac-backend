package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.LinkNoticeQueue;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Resource
    private NoticeService noticeService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private ScreeningTaskService screeningTaskService;

    /**
     * 迁移学生数据
     */
    public void migratingStudentData() {
        List<Object> queueListObj = redisUtil.lGetAll(RedisConstant.NOTICE_LINK_LIST);

        List<LinkNoticeQueue> queueList = queueListObj.stream()
                .filter(LinkNoticeQueue.class::isInstance)
                .map(LinkNoticeQueue.class::cast)
                .collect(Collectors.toList());

        log.info("关联通知-迁移学生计划数据:{}", JSON.toJSONString(queueList));

        Map<Integer, String> planMap = screeningPlanService.getByIds(queueList.stream().map(LinkNoticeQueue::getPlanId).collect(Collectors.toList())).stream().collect(Collectors.toMap(ScreeningPlan::getId, ScreeningPlan::getTitle));
        Map<Integer, String> taskMap = screeningTaskService.listByIds(queueList.stream().map(LinkNoticeQueue::getScreeningTaskId).collect(Collectors.toList())).stream().collect(Collectors.toMap(ScreeningTask::getId, ScreeningTask::getTitle));

        if (CollectionUtils.isEmpty(queueList)) {
            log.info("暂无需要关联的学生");
            return;
        }
        for (int i = 0; i < queueList.size(); i++) {
            log.info("一共需要关联:{}个计划，当前第:{}个", queueList.size(), i + 1);
            LinkNoticeQueue linkNoticeQueue = queueList.get(i);
            Integer createUserId = linkNoticeQueue.getCreateUserId();
            try {
                noticeLinkBizService.noticeLinkStudentMigrating(linkNoticeQueue);
                sendNoticeLinkResult(createUserId, String.format(CommonConst.NOTICE_LINK_SUCCESS, planMap.get(linkNoticeQueue.getPlanId()), taskMap.get(linkNoticeQueue.getScreeningTaskId())));
            } catch (Exception e) {
                log.error("关联通知-迁移学生存在异常,关联通知数据:{}", JSON.toJSONString(linkNoticeQueue), e);
                redisUtil.lSet(RedisConstant.NOTICE_LINK_ERROR_LIST, JSON.toJSONString(linkNoticeQueue));
                sendNoticeLinkResult(createUserId, String.format(CommonConst.NOTICE_LINK_FAIL, planMap.get(linkNoticeQueue.getPlanId()), taskMap.get(linkNoticeQueue.getScreeningTaskId())));
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

    /**
     * 发送关联通知结果
     *
     * @param userId  用户Id
     * @param content 内容
     */
    private void sendNoticeLinkResult(Integer userId, String content) {
        noticeService.createNoticeLinkResult(userId, content);
    }
}
