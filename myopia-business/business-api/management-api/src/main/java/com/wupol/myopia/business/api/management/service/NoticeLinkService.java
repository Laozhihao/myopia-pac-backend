package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.api.management.schedule.StatisticScheduledTaskService;
import com.wupol.myopia.business.common.utils.domain.dto.LinkNoticeQueue;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
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
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private StatConclusionService statConclusionService;
    @Resource
    private StatRescreenService statRescreenService;
    @Resource
    private SchoolMonitorStatisticService schoolMonitorStatisticService;
    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;
    @Resource
    private ScreeningResultStatisticService screeningResultStatisticService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private StatisticScheduledTaskService statisticScheduledTaskService;

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
                noticeLinkStudentMigrating(linkNoticeQueue);
            } catch (Exception e) {
                log.info("关联通知-迁移学生存在异常", e);
                redisUtil.lSet(RedisConstant.NOTICE_LINK_ERROR_LIST, JSON.toJSONString(linkNoticeQueue));
            }
        }
        redisUtil.del(RedisConstant.NOTICE_LINK_LIST);
    }

    /**
     * 关联学生
     *
     * @param linkNoticeQueue 关联通知
     */
    private void noticeLinkStudentMigrating(LinkNoticeQueue linkNoticeQueue) {
        Integer planId = linkNoticeQueue.getPlanId();
        Integer screeningTaskId = linkNoticeQueue.getScreeningTaskId();
        Integer screeningNoticeId = linkNoticeQueue.getScreeningNoticeId();

        // 计划学生
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByScreeningPlanId(planId);
        planStudents.forEach(planStudent -> planStudent.setScreeningTaskId(screeningTaskId)
                .setSrcScreeningNoticeId(screeningNoticeId));
        screeningPlanSchoolStudentService.updateBatchById(planStudents);

        // 筛查结果
        List<VisionScreeningResult> visionResults = visionScreeningResultService.getByPlanId(planId);
        visionResults.forEach(result -> result.setTaskId(screeningTaskId)
                .setUpdateTime(new Date()));
        visionScreeningResultService.updateBatchById(visionResults);

        // 统计结果
        List<StatConclusion> statConclusions = statConclusionService.getByPlanId(planId);
        statConclusions.forEach(statConclusion -> statConclusion.setSrcScreeningNoticeId(screeningNoticeId)
                .setTaskId(screeningTaskId)
                .setUpdateTime(new Date()));
        statConclusionService.updateBatchById(statConclusions);

        // 复测统计表
        List<StatRescreen> statRescreens = statRescreenService.getByPlanId(planId);
        statRescreens.forEach(statRescreen -> statRescreen.setSrcScreeningNoticeId(screeningNoticeId).setTaskId(screeningTaskId));
        statRescreenService.updateBatchById(statRescreens);

        // 删除统计数据
        schoolMonitorStatisticService.deleteByPlanId(planId);
        schoolVisionStatisticService.deleteByPlanId(planId);
        screeningResultStatisticService.deleteByPlanId(planId);

        statisticScheduledTaskService.statistic(null, null, false, null, screeningNoticeId);
    }
}
