package com.wupol.myopia.business.aggregation.screening.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanLinkNoticeRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知
 *
 * @author Simple4H
 */
@Service
public class ScreeningNoticeBizFacadeService {

    @Resource
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Resource
    private ScreeningTaskService screeningTaskService;
    @Resource
    private ScreeningNoticeService screeningNoticeService;
    @Resource
    private GovDeptService govDeptService;

    @Resource
    private ScreeningPlanService screeningPlanService;

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

    public List<ScreeningNoticeDTO> getCanLinkNotice(Integer orgId, Integer type) {
        List<ScreeningNoticeDTO> notices = screeningNoticeDeptOrgService.getCanLinkNotice(orgId, type);
        if (CollectionUtils.isEmpty(notices)) {
            return notices;
        }

        // 通过TaskId查询最原始的通知
        List<ScreeningTask> tasks = screeningTaskService.listByIds(notices.stream().map(ScreeningNotice::getScreeningTaskId).collect(Collectors.toList()));
        Map<Integer, Integer> taskMap = tasks.stream().collect(Collectors.toMap(ScreeningTask::getId, ScreeningTask::getScreeningNoticeId));

        List<ScreeningNotice> sourceNotices = screeningNoticeService.getByIds(tasks.stream().map(ScreeningTask::getScreeningNoticeId).collect(Collectors.toList()));
        Map<Integer, Integer> sourceNoticeMap = sourceNotices.stream().collect(Collectors.toMap(ScreeningNotice::getId, ScreeningNotice::getGovDeptId));

        // 获取行政部门
        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(sourceNotices.stream().map(ScreeningNotice::getGovDeptId).collect(Collectors.toList())));
        Map<Integer, String> govDeptMap = govDeptList.stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        notices.forEach(notice -> notice.setGovDeptName(govDeptMap.get(sourceNoticeMap.get(taskMap.get(notice.getScreeningTaskId())))));
        return notices;
    }


    /**
     * 关联通知
     *
     * @param requestDTO requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer linkNotice(PlanLinkNoticeRequestDTO requestDTO) {
        Integer planId = requestDTO.getPlanId();
        Integer screeningNoticeDeptOrgId = requestDTO.getScreeningNoticeDeptOrgId();
        Integer screeningTaskId = requestDTO.getScreeningTaskId();


        ScreeningPlan plan = screeningPlanService.getById(planId);
        if (Objects.isNull(plan)) {
            throw new BusinessException("通过计划Id查询不到计划" + planId);
        }

        ScreeningNoticeDeptOrg deptOrg = screeningNoticeDeptOrgService.getById(screeningNoticeDeptOrgId);
        if (Objects.isNull(deptOrg)) {
            throw new BusinessException("通知机构信息异常" + screeningNoticeDeptOrgId);
        }

        ScreeningTask task = screeningTaskService.getById(screeningTaskId);
        if (Objects.isNull(task)) {
            throw new BusinessException("任务信息异常" + screeningNoticeDeptOrgId);
        }

        Integer districtId = task.getDistrictId();

        Integer screeningNoticeId = task.getScreeningNoticeId();
        plan.setSrcScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId);
        screeningPlanService.updateById(plan);


        deptOrg.setOperationStatus(CommonConst.STATUS_NOTICE_CREATED).setScreeningTaskPlanId(planId);
        screeningNoticeDeptOrgService.updateById(deptOrg);

        // 计划学生
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByScreeningPlanId(planId);
        planStudents.forEach(planStudent -> {
            planStudent.setPlanDistrictId(districtId)
                    .setScreeningTaskId(screeningTaskId)
                    .setSrcScreeningNoticeId(screeningNoticeId);
        });
        screeningPlanSchoolStudentService.updateBatchById(planStudents);

        // 筛查结果
        List<VisionScreeningResult> visionResults = visionScreeningResultService.getByPlanId(planId);
        visionResults.forEach(result -> {
            result.setTaskId(screeningTaskId)
                    .setDistrictId(districtId);
        });
        visionScreeningResultService.updateBatchById(visionResults);

        // 统计结果
        List<StatConclusion> statConclusions = statConclusionService.getByPlanId(planId);
        statConclusions.forEach(statConclusion -> {
            statConclusion.setSrcScreeningNoticeId(screeningNoticeId)
                    .setTaskId(screeningTaskId)
                    .setDistrictId(districtId);
        });
        statConclusionService.updateBatchById(statConclusions);

        // 复测统计表
        List<StatRescreen> statRescreens = statRescreenService.getByPlanId(planId);
        statRescreens.forEach(statRescreen -> {
            statRescreen.setSrcScreeningNoticeId(screeningNoticeId).setTaskId(screeningTaskId);
        });
        statRescreenService.updateBatchById(statRescreens);

        // 删除统计数据
        schoolMonitorStatisticService.deleteByPlanId(planId);
        schoolVisionStatisticService.deleteByPlanId(planId);
        screeningResultStatisticService.deleteByPlanId(planId);
        return screeningNoticeId;
    }
}
