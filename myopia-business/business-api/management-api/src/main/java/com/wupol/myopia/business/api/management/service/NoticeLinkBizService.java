package com.wupol.myopia.business.api.management.service;

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
import org.springframework.transaction.support.TransactionTemplate;

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
public class NoticeLinkBizService {

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
    private StatisticScheduledTaskService statisticScheduledTaskService;
    @Resource
    private TransactionTemplate transactionTemplate;


    /**
     * 关联学生
     *
     * @param linkNoticeQueue 关联通知
     */
    public void noticeLinkStudentMigrating(LinkNoticeQueue linkNoticeQueue) {

        Integer planId = linkNoticeQueue.getPlanId();
        Integer screeningTaskId = linkNoticeQueue.getScreeningTaskId();
        Integer screeningNoticeId = linkNoticeQueue.getScreeningNoticeId();

        transactionTemplate.execute(transactionStatus -> {
            try {
                handleStudentList(planId, screeningTaskId, screeningNoticeId);
            } catch (Exception e) {
                transactionStatus.setRollbackOnly();
            }
            return null;
        });
        statisticScheduledTaskService.statistic(null, null, false, null, screeningNoticeId);
    }

    /**
     * 处理学生信息
     */
    private void handleStudentList(Integer planId, Integer screeningTaskId, Integer screeningNoticeId) {
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
    }
}
