package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.*;
import com.wupol.myopia.business.api.management.service.report.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 筛查报告-幼儿园
 *
 * @author Simple4H
 */
@Service
public class ScreeningKindergartenReportService {

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private CommonReportService commonReportService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private HighLowProportionService highLowProportionService;

    @Resource
    private CountAndProportionService countAndProportionService;

    @Resource
    private HorizontalChartService horizontalChartService;

    @Resource
    private PieChartService pieChartService;

    @Resource
    private KindergartenReportTableService kindergartenReportTableService;

    public KindergartenReportDTO generateReport(Integer planId, Integer schoolId) {

        KindergartenReportDTO kindergartenReportDTO = new KindergartenReportDTO();

        List<StatConclusion> allStatList = statConclusionService.getByPlanIdSchoolId(planId, schoolId);
        List<StatConclusion> statConclusions = commonReportService.getKList(allStatList);
        School school = schoolService.getBySchoolId(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(planId);

        kindergartenReportDTO.setInfo(commonReportService.generateInfo(school, plan));
        kindergartenReportDTO.setOutline(generateKindergartenSchoolOutline(statConclusions, school, plan));
        kindergartenReportDTO.setGeneralVision(generateGeneralVision(statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList())));
        kindergartenReportDTO.setClassScreeningData(commonReportService.generateClassScreeningData(school, plan, true));
        return kindergartenReportDTO;
    }

    private KindergartenSchoolOutline generateKindergartenSchoolOutline(List<StatConclusion> statConclusions, School school, ScreeningPlan plan) {
        KindergartenSchoolOutline schoolOutline = new KindergartenSchoolOutline();
        long total = statConclusions.size();

        schoolOutline.setOutline(commonReportService.generateOutline(statConclusions, school, plan, true));
        schoolOutline.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        schoolOutline.setRefractiveAbnormalities(commonReportService.getRefractiveAbnormalities(statConclusions, total));

        schoolOutline.setVisionWarningSituation(commonReportService.getKindergartenVisionWarningSituation(statConclusions, total));
        schoolOutline.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions, total));

        schoolOutline.setHistoryRefractiveInfo(commonReportService.getKindergartenHistoryRefractive(school, plan));
        return schoolOutline;
    }

    private GeneralVision generateGeneralVision(List<StatConclusion> statConclusions) {

        long total = statConclusions.size();

        GeneralVision generalVision = new GeneralVision();

        // 不同性别视力低常情况
        generalVision.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        SexLowVision sexLowVision = new SexLowVision();
        SexLowVision.Info info = new SexLowVision.Info();
        info.setLowVisionProportion(countAndProportionService.lowVision(statConclusions, total).getProportion());
        info.setAvgVision(commonReportService.getAvgVision(statConclusions));
        info.setMLowVisionProportion(countAndProportionService.mLowVision(statConclusions).getProportion());
        info.setLLowVisionProportion(countAndProportionService.fLowVision(statConclusions).getProportion());
        sexLowVision.setInfo(info);
        sexLowVision.setGenderLowVisionChart(pieChartService.genderLowVisionChart(statConclusions, total));
        sexLowVision.setTables(kindergartenReportTableService.sexLowVisionTable(statConclusions, total));
        generalVision.setSexLowVision(sexLowVision);

        // 不同年级班级视力情况
        GradeLowVision gradeLowVision = new GradeLowVision();
        List<GradeLowVision.Table> gradeLowVisionTable = kindergartenReportTableService.gradeLowVisionTable(statConclusions, total);
        gradeLowVision.setTables(Lists.newArrayList(gradeLowVisionTable));
        if (commonReportService.isShowInfo(gradeLowVisionTable, true)) {
            gradeLowVision.setGradeLowVisionChart(horizontalChartService.kLowVisionGenderChart(statConclusions));
            gradeLowVision.setInfo(getGradeLowVisionInfo(statConclusions, gradeLowVisionTable, total));
        }
        generalVision.setGradeLowVision(gradeLowVision);

        // 屈光情况
        generalVision.setRefractiveAbnormalities(commonReportService.getRefractiveAbnormalities(statConclusions, total));

        // 不同性别屈光情况
        generalVision.setSexRefractive(commonReportService.kSexRefractive(statConclusions, total));

        // 不同年级班级屈光情况
        GradeRefractive gradeRefractive = new GradeRefractive();
        List<GradeRefractive.Table> gradeTables = kindergartenReportTableService.gradeRefractiveTables(statConclusions, total);
        gradeRefractive.setTables(Lists.newArrayList(gradeTables));
        if (commonReportService.isShowInfo(gradeTables, true)) {
            gradeRefractive.setGradeRefractiveChart(horizontalChartService.kGradeRefractive(statConclusions));
            gradeRefractive.setInfo(getGradeRefractiveInfo(statConclusions, gradeTables, total));
        }

        generalVision.setGradeRefractive(gradeRefractive);
        // 视力预警情况
        generalVision.setVisionWarningSituation(commonReportService.getKindergartenVisionWarningSituation(statConclusions, total));
        generalVision.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions, total));

        // 不同年级班级视力预警情况
        GradeWarning gradeWarning = new GradeWarning();
        List<GradeWarning.Table> gradeWarningTable = kindergartenReportTableService.kindergartenGradeWarningTable(statConclusions, total);
        gradeWarning.setTables(Lists.newArrayList(gradeWarningTable));
        if (commonReportService.isShowInfo(gradeWarningTable, true)) {
            gradeWarning.setGradeWarningChart(horizontalChartService.kGradeWarning(statConclusions));
            gradeWarning.setInfo(getGradeWarning(statConclusions, gradeWarningTable, total));
        }
        generalVision.setGradeWarning(gradeWarning);
        return generalVision;
    }

    private GradeLowVision.Info getGradeLowVisionInfo(List<StatConclusion> statConclusions, List<GradeLowVision.Table> tables, Long total) {

        Map<String, List<GradeLowVision.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeLowVision.Table::getName));

        GradeLowVision.Info info = new GradeLowVision.Info();
        info.setOne(getGradeLowVisionInfo(statConclusions, gradeMap, GradeCodeEnum.ONE_KINDERGARTEN, total));
        info.setTwo(getGradeLowVisionInfo(statConclusions, gradeMap, GradeCodeEnum.TWO_KINDERGARTEN, total));
        info.setThree(getGradeLowVisionInfo(statConclusions, gradeMap, GradeCodeEnum.THREE_KINDERGARTEN, total));
        return info;
    }

    private MaxMinProportion getGradeLowVisionInfo(List<StatConclusion> statConclusions, Map<String, List<GradeLowVision.Table>> gradeMap,
                                                   GradeCodeEnum gradeCodeEnum, Long total) {
        highLowProportionService.getKindergartenMaxMin(
                countAndProportionService.lowVision(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), gradeCodeEnum.getCode())).collect(Collectors.toList()), total).getProportion(),
                gradeMap.get(gradeCodeEnum.getName()),
                s -> Float.valueOf(s.getLowVisionProportion()));
    }

    private GradeRefractive.Info getGradeRefractiveInfo(List<StatConclusion> statConclusions, List<GradeRefractive.Table> tables, Long total) {
        GradeRefractive.Info info = new GradeRefractive.Info();
        Map<String, List<GradeRefractive.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeRefractive.Table::getName));

        info.setOne(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.ONE_KINDERGARTEN.getName()), total));
        info.setTwo(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.TWO_KINDERGARTEN.getName()), total));
        info.setThree(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.THREE_KINDERGARTEN.getName()), total));
        return info;
    }

    private GradeRefractive.Detail getGradeRefractiveDetail(List<StatConclusion> statConclusions, List<GradeRefractive.Table> tables, Long total) {
        if (CollectionUtils.isEmpty(statConclusions) || CollectionUtils.isEmpty(tables)) {
            return null;
        }
        GradeRefractive.Detail detail = new GradeRefractive.Detail();
        detail.setInsufficient(highLowProportionService.getKindergartenMaxMin(countAndProportionService.insufficient(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getInsufficientProportion())));
        detail.setRefractiveError(highLowProportionService.getKindergartenMaxMin(countAndProportionService.refractiveError(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getRefractiveErrorProportion())));
        detail.setAnisometropia(highLowProportionService.getKindergartenMaxMin(countAndProportionService.anisometropia(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getAnisometropiaProportion())));
        return detail;
    }

    private GradeWarning.Info getGradeWarning(List<StatConclusion> statConclusions, List<GradeWarning.Table> tables, Long total) {
        GradeWarning.Info info = new GradeWarning.Info();
        Map<String, List<GradeWarning.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeWarning.Table::getName));
        info.setOne(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.ONE_KINDERGARTEN.getName()), total));
        info.setTwo(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.TWO_KINDERGARTEN.getName()), total));
        info.setThree(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.THREE_KINDERGARTEN.getName()), total));
        return info;
    }

    private GradeWarning.Detail getGradeWarningDetail(List<StatConclusion> statConclusions, List<GradeWarning.Table> tables, Long total) {
        if (CollectionUtils.isEmpty(statConclusions) || CollectionUtils.isEmpty(tables)) {
            return null;
        }
        GradeWarning.Detail detail = new GradeWarning.Detail();
        detail.setWarningProportion(highLowProportionService.getKindergartenMaxMin(countAndProportionService.warning(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getWarningProportion())));
        detail.setRecommendDoctor(highLowProportionService.getKindergartenMaxMin(countAndProportionService.getRecommendDoctor(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
        detail.setZeroWarning(countAndProportionService.zeroAndSPWarning(statConclusions, total).getProportion());
        detail.setOneWarning(countAndProportionService.oneWarning(statConclusions, total).getProportion());
        detail.setTwoWarning(countAndProportionService.twoWarning(statConclusions, total).getProportion());
        detail.setThreeWarning(countAndProportionService.threeWarning(statConclusions, total).getProportion());
        return detail;
    }
}
