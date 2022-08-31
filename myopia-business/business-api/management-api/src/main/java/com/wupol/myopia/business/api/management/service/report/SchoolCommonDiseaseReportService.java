package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 按学校常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
public class SchoolCommonDiseaseReportService {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private SchoolSaprodontiaMonitorService schoolSaprodontiaMonitorService;
    @Autowired
    private SchoolHeightAndWeightMonitorService schoolHeightAndWeightMonitorService;
    @Autowired
    private SchoolBloodPressureAndSpinalCurvatureMonitorService schoolBloodPressureAndSpinalCurvatureMonitorService;
    @Autowired
    private SchoolClassScreeningMonitorService schoolClassScreeningMonitorService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;


    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer schoolId, Integer planId) {
        SchoolCommonDiseaseReportVO schoolCommonDiseaseReportVO = new SchoolCommonDiseaseReportVO();

        //学校ID、计划ID
        List<StatConclusion> statConclusionList = getStatConclusionList(schoolId, planId, Boolean.FALSE);
        statConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getIsCooperative(),1)).collect(Collectors.toList());

        //全局变量
        getGlobalVariableVO(schoolId, planId, schoolCommonDiseaseReportVO);
        //筛查人数和实际筛查人数
        setNum(schoolId, planId, schoolCommonDiseaseReportVO);
        //视力分析
        getVisionAnalysisVO(statConclusionList, schoolCommonDiseaseReportVO);
        //常见病分析
        getSchoolCommonDiseasesAnalysisVO(statConclusionList, schoolCommonDiseaseReportVO);

        return schoolCommonDiseaseReportVO;
    }

    /**
     * 全局变量
     */
    private void getGlobalVariableVO(Integer schoolId, Integer planId, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        SchoolCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new SchoolCommonDiseaseReportVO.GlobalVariableVO();
        String format = "YYYY";

        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException(String.format("不存在筛查计划: planId=%s", planId));
        }

        School school = schoolService.getById(schoolId);
        if (Objects.isNull(school)) {
            throw new BusinessException(String.format("不存在学校: schoolId=%s", schoolId));
        }

        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(screeningPlan.getScreeningOrgId());
        if (Objects.isNull(screeningOrganization)) {
            throw new BusinessException(String.format("不存在筛查机构: screeningOrgId=%s", screeningPlan.getScreeningOrgId()));
        }

        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollUtil.isEmpty(screeningResults)) {
            throw new BusinessException("暂无筛查数据！");
        }
        globalVariableVO.setReportDate(new Date());
        Date endTime = screeningPlan.getEndTime();
        if (DateUtil.betweenDay(endTime, new Date()) > 0) {
            globalVariableVO.setReportDate(endTime);
        }

        Date startTime = screeningPlan.getStartTime();
        globalVariableVO.setDataYear(DateUtil.format(startTime, format));

        Set<String> years = Sets.newHashSet(DateUtil.format(startTime, format), DateUtil.format(endTime, format));
        List<String> yearPeriod;
        if (years.size() == 1) {
            yearPeriod = Lists.newArrayList(getDateStr(startTime), DateUtil.format(endTime, "MM月dd日"));
        } else {
            yearPeriod = Lists.newArrayList(getDateStr(startTime),getDateStr(endTime));
            VisionScreeningResult visionScreeningResult = screeningResults.get(0);
            globalVariableVO.setDataYear(DateUtil.format(visionScreeningResult.getCreateTime(), format));
        }
        String screeningTimePeriod = CollUtil.join(yearPeriod, StrUtil.DASHED);
        globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

        globalVariableVO.setSchoolName(school.getName());
        globalVariableVO.setTakeQuestionnaireNum(0);
        globalVariableVO.setScreeningOrgName(screeningOrganization.getName());

        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }

    private static String getDateStr(Date date){
        return DateUtil.format(date, DatePattern.CHINESE_DATE_PATTERN);
    }

    /**
     * 筛查人数和实际筛查人数
     */
    private void setNum(Integer schoolId, Integer planId, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIdAndSchoolId(planId, schoolId);
        List<Integer> planSchoolStudentIds=Lists.newArrayList();
        if (CollUtil.isNotEmpty(screeningPlanSchoolStudentList)) {
            List<Integer> collect = screeningPlanSchoolStudentList.stream().filter(sp->Objects.equals(sp.getGradeType(), SchoolAge.KINDERGARTEN.code)).map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList());
            planSchoolStudentIds.addAll(collect);
            screeningPlanSchoolStudentList = screeningPlanSchoolStudentList.stream().filter(sp->!Objects.equals(sp.getGradeType(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());
            districtCommonDiseaseReportVO.setScreeningStudentNum(screeningPlanSchoolStudentList.size());

        }else {
            districtCommonDiseaseReportVO.setScreeningStudentNum(0);
        }


        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollUtil.isEmpty(screeningResults)) {
            districtCommonDiseaseReportVO.setActualScreeningNum(0);
        } else {
            long count = screeningResults.stream()
                    .filter(sr -> Objects.equals(sr.getIsDoubleScreen(), Boolean.FALSE))
                    .filter(sr-> !planSchoolStudentIds.contains(sr.getScreeningPlanSchoolStudentId()) )
                    .count();
            districtCommonDiseaseReportVO.setActualScreeningNum((int)count);
        }

    }

    /**
     * 视力分析
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            statConclusionList = Collections.emptyList();
        }
        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream()
                .filter(sc -> Objects.equals(sc.getIsValid(),Boolean.TRUE))
                .filter(sc -> !Objects.equals(GradeCodeEnum.getByCode(sc.getSchoolGradeCode()).getType(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        SchoolCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new VisionAnalysisNum()
                .build(primaryAndAboveStatConclusionList)
                .ratioNotSymbol()
                .buildVisionAnalysisVO();
        if (Objects.nonNull(visionAnalysisVO.getValidScreeningNum())){
            visionAnalysisVO.setAvgVision(StatUtil.averageVision(primaryAndAboveStatConclusionList));
        }
        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }


    private List<StatConclusion> getStatConclusionList(Integer schoolId, Integer planId, Boolean isRescreen) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getPlanId, planId);
        queryWrapper.eq(StatConclusion::getSchoolId, schoolId);
        queryWrapper.eq(StatConclusion::getIsRescreen, isRescreen);
        return statConclusionService.list(queryWrapper);
    }

    /**
     * 常见病分析
     */
    private void getSchoolCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream()
                .filter(sc -> !Objects.equals(GradeCodeEnum.getByCode(sc.getSchoolGradeCode()).getType(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO = new SchoolCommonDiseasesAnalysisVO();
        //常见病分析变量
        getCommonDiseasesAnalysisVariableVO(primaryAndAboveStatConclusionList, schoolCommonDiseasesAnalysisVO);
        //龋齿监测结果
        schoolSaprodontiaMonitorService.getSchoolSaprodontiaMonitorVO(primaryAndAboveStatConclusionList, schoolCommonDiseasesAnalysisVO);
        //体重身高监测结果
        schoolHeightAndWeightMonitorService.getSchoolHeightAndWeightMonitorVO(primaryAndAboveStatConclusionList, schoolCommonDiseasesAnalysisVO);
        //血压与脊柱弯曲异常监测结果
        schoolBloodPressureAndSpinalCurvatureMonitorService.getSchoolBloodPressureAndSpinalCurvatureMonitorVO(primaryAndAboveStatConclusionList, schoolCommonDiseasesAnalysisVO);
        //各学校筛查情况
        schoolClassScreeningMonitorService.getSchoolClassScreeningMonitorVO(primaryAndAboveStatConclusionList, schoolCommonDiseasesAnalysisVO);

        districtCommonDiseaseReportVO.setSchoolCommonDiseasesAnalysisVO(schoolCommonDiseasesAnalysisVO);

    }


    /**
     * 常见病分析变量
     */
    private void getCommonDiseasesAnalysisVariableVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new CommonDiseasesNum()
                .build(statConclusionList)
                .ratioNotSymbol()
                .buidCommonDiseasesAnalysisVariableVO();
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);
    }

}
