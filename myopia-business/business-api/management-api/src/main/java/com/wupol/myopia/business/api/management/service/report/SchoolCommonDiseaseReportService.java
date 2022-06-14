package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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


    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer schoolId, Integer planId) {
        SchoolCommonDiseaseReportVO schoolCommonDiseaseReportVO = new SchoolCommonDiseaseReportVO();


        List<StatConclusion> statConclusionList = getStatConclusionList(schoolId, planId, Boolean.TRUE, Boolean.FALSE);

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
        if (CollectionUtil.isEmpty(screeningResults)) {
            throw new BusinessException("暂无筛查数据！");
        }
        globalVariableVO.setReportDate(new Date());

        Date min = screeningResults.stream().map(VisionScreeningResult::getUpdateTime).min(Comparator.comparing(Date::getTime)).orElse(new Date());
        Date max = screeningResults.stream().map(VisionScreeningResult::getUpdateTime).max(Comparator.comparing(Date::getTime)).orElse(new Date());

        if (DateUtil.betweenDay(max, new Date()) > 3) {
            globalVariableVO.setReportDate(max);
        }

        String dataYear = DateUtil.format(min, format);
        globalVariableVO.setDataYear(dataYear);

        Set<String> years = Sets.newHashSet(DateUtil.format(min, format), DateUtil.format(max, format));

        if (years.size() == 1) {
            List<String> yearPeriod = Lists.newArrayList(DateUtil.format(min, DatePattern.CHINESE_DATE_PATTERN), DateUtil.format(max, "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
        } else {
            List<String> yearPeriod = Lists.newArrayList(DateUtil.format(min, DatePattern.CHINESE_DATE_PATTERN),DateUtil.format(max, DatePattern.CHINESE_DATE_PATTERN));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

        }

        globalVariableVO.setSchoolName(school.getName());
        globalVariableVO.setTakeQuestionnaireNum(0);
        globalVariableVO.setScreeningOrgName(screeningOrganization.getName());

        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
    }


    /**
     * 筛查人数和实际筛查人数
     */
    private void setNum(Integer schoolId, Integer planId, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException(String.format("不存在筛查计划: planId=%s", planId));
        }

        districtCommonDiseaseReportVO.setScreeningStudentNum(screeningPlan.getStudentNumbers());
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtil.isEmpty(screeningResults)) {
            districtCommonDiseaseReportVO.setActualScreeningNum(0);
        } else {
            long count = screeningResults.stream().filter(sr -> Objects.equals(sr.getIsDoubleScreen(), Boolean.FALSE)).count();
            districtCommonDiseaseReportVO.setActualScreeningNum((int)count);
        }

    }

    /**
     * 视力分析
     */
    private void getVisionAnalysisVO(List<StatConclusion> statConclusionList, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        TwoTuple<BigDecimal, BigDecimal> averageVisionTuple = StatUtil.calculateAverageVision(statConclusionList);
        BigDecimal add = averageVisionTuple.getFirst().add(averageVisionTuple.getSecond());
        BigDecimal averageVision = BigDecimalUtil.divide(add, new BigDecimal("2"), 1);

        SchoolCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new VisionAnalysisNum()
                .build(statConclusionList)
                .ratioNotSymbol()
                .buildVisionAnalysisVO();
        visionAnalysisVO.setAvgVision(averageVision);
        districtCommonDiseaseReportVO.setVisionAnalysisVO(visionAnalysisVO);
    }


    private List<StatConclusion> getStatConclusionList(Integer schoolId, Integer planId, Boolean isValid, Boolean isRescreen) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getPlanId, planId);
        queryWrapper.eq(StatConclusion::getSchoolId, schoolId);
        queryWrapper.eq(StatConclusion::getIsValid, isValid);
        queryWrapper.eq(StatConclusion::getIsRescreen, isRescreen);
        return statConclusionService.list(queryWrapper);
    }

    /**
     * 常见病分析
     */
    private void getSchoolCommonDiseasesAnalysisVO(List<StatConclusion> statConclusionList, SchoolCommonDiseaseReportVO districtCommonDiseaseReportVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<StatConclusion> primaryAndAboveStatConclusionList = statConclusionList.stream().filter(sc -> !Objects.equals(sc.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

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
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        CommonDiseasesAnalysisVariableVO commonDiseasesAnalysisVariableVO = new CommonDiseasesNum().build(statConclusionList).ratioNotSymbol().buidCommonDiseasesAnalysisVariableVO();
        districtCommonDiseasesAnalysisVO.setCommonDiseasesAnalysisVariableVO(commonDiseasesAnalysisVariableVO);
    }

}
