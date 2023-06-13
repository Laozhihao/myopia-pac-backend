package com.wupol.myopia.business.api.management.service.report.plan;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.PlanReportResponseDTO;
import com.wupol.myopia.business.api.management.service.report.plan.myopia.MyopiaPlanReportService;
import com.wupol.myopia.business.api.management.service.report.plan.overview.OverviewPlanReportService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 计划报告
 *
 * @author Simple4H
 */
@Service
public class PlanReportService {


    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private OverviewPlanReportService overviewPlanReportService;

    @Resource
    private MyopiaPlanReportService myopiaPlanReportService;


    /**
     * 获取计划报告
     *
     * @param planId 计划Id
     * @return PlanReportResponseDTO
     */
    public PlanReportResponseDTO getPlanReport(Integer planId) {
        ScreeningPlan plan = screeningPlanService.getById(planId);
        if (Objects.isNull(plan)) {
            throw new BusinessException("筛查计划存在异常");
        }
        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanId(planId);
        List<StatConclusion> statConclusions = statConclusionService.getByPlanId(planId);
        List<ScreeningPlanSchool> planSchools = screeningPlanSchoolService.getByPlanId(planId);
        if (CollectionUtils.isEmpty(planSchoolStudents) || CollectionUtils.isEmpty(statConclusions) ||
                CollectionUtils.isEmpty(planSchools)) {
            throw new BusinessException("小胡，您好。暂无数据，请有筛查数据后重试");
        }


        PlanReportResponseDTO planReportResponseDTO = new PlanReportResponseDTO();
        planReportResponseDTO.setOverview(overviewPlanReportService.getOverview(plan, planSchoolStudents, statConclusions, planSchools));


        List<StatConclusion> validStatConclusions = statConclusions.stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).collect(Collectors.toList());
        planReportResponseDTO.setMyopiaSituation(myopiaPlanReportService.getMyopiaSituation(validStatConclusions));
//        planReportResponseDTO.setVisionSituation();
//        planReportResponseDTO.setCorrectSituation();
//        planReportResponseDTO.setRefractionSituation();

        return planReportResponseDTO;
    }

}
