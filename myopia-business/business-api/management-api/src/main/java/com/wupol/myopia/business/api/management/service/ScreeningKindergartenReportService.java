package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.*;
import com.wupol.myopia.business.api.management.service.report.ScreeningReportTableService;
import com.wupol.myopia.business.api.management.service.report.CommonReportService;
import com.wupol.myopia.business.api.management.service.report.CountAndProportionService;
import com.wupol.myopia.business.api.management.service.report.HighLowProportionService;
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
    private ScreeningReportTableService screeningReportTableService;

    public KindergartenReportDTO generateReport(Integer planId, Integer schoolId, Integer noticeId) {

        KindergartenReportDTO kindergartenReportDTO = new KindergartenReportDTO();

        List<StatConclusion> allStatList = statConclusionService.getByPlanIdSchoolIdNoticeId(planId, schoolId, noticeId);
        List<StatConclusion> statConclusions = commonReportService.getKList(allStatList);
        School school = schoolService.getBySchoolId(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(planId);

        kindergartenReportDTO.setInfo(commonReportService.generateInfo(school));
        kindergartenReportDTO.setOutline(generateKindergartenSchoolOutline(statConclusions, school, plan));
        kindergartenReportDTO.setGeneralVision(generateGeneralVision(statConclusions));
        kindergartenReportDTO.setClassScreeningData(commonReportService.generateClassScreeningData(statConclusions, school, true));
        return kindergartenReportDTO;
    }

    private KindergartenSchoolOutline generateKindergartenSchoolOutline(List<StatConclusion> statConclusions, School school, ScreeningPlan plan) {
        KindergartenSchoolOutline schoolOutline = new KindergartenSchoolOutline();
        schoolOutline.setOutline(commonReportService.generateOutline(statConclusions, school, plan));
        schoolOutline.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        schoolOutline.setRefractiveAbnormalities(commonReportService.getRefractiveAbnormalities(statConclusions));

        schoolOutline.setVisionWarningSituation(commonReportService.getVisionWarningSituation(statConclusions));
        schoolOutline.setHistoryRefractiveInfo(commonReportService.getHistoryRefractive(statConclusions));
        return schoolOutline;
    }

    private GeneralVision generateGeneralVision(List<StatConclusion> statConclusions) {
        GeneralVision generalVision = new GeneralVision();

        // 不同性别视力低常情况
        generalVision.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        SexLowVision sexLowVision = new SexLowVision();
        SexLowVision.Info info = new SexLowVision.Info();
        info.setLowVisionProportion(countAndProportionService.lowVision(statConclusions).getProportion());
        info.setAvgVision(commonReportService.getAvgVision(statConclusions));
        info.setMLowVisionProportion(countAndProportionService.mLowVision(statConclusions).getProportion());
        info.setLLowVisionProportion(countAndProportionService.fLowVision(statConclusions).getProportion());
        sexLowVision.setInfo(info);
        sexLowVision.setTables(screeningReportTableService.sexLowVisionTable(statConclusions));
        generalVision.setSexLowVision(sexLowVision);

        // 不同年级班级视力情况
        GradeLowVision gradeLowVision = new GradeLowVision();
        List<GradeLowVision.Table> gradeLowVisionTable = screeningReportTableService.gradeLowVisionTable(statConclusions);
        gradeLowVision.setTables(Lists.newArrayList(gradeLowVisionTable));
        gradeLowVision.setInfo(getGradeLowVisionInfo(statConclusions, gradeLowVisionTable));
        generalVision.setGradeLowVision(gradeLowVision);

        // 屈光情况
        generalVision.setRefractiveAbnormalities(commonReportService.getRefractiveAbnormalities(statConclusions));

        // 不同性别屈光情况
        generalVision.setSexRefractive(commonReportService.kSexRefractive(statConclusions));

        // 不同年级班级屈光情况
        GradeRefractive gradeRefractive = new GradeRefractive();
        List<GradeRefractive.Table> gradeTables = screeningReportTableService.gradeRefractiveTables(statConclusions);
        gradeRefractive.setTables(Lists.newArrayList(gradeTables));
        gradeRefractive.setInfo(getGradeRefractiveInfo(statConclusions, gradeTables));
        generalVision.setGradeRefractive(gradeRefractive);
        // 视力预警情况
        generalVision.setVisionWarningSituation(commonReportService.getVisionWarningSituation(statConclusions));
        generalVision.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions));

        // 不同年级班级视力预警情况
        GradeWarning gradeWarning = new GradeWarning();
        List<GradeWarning.Table> gradeWarningTable = screeningReportTableService.gradeWarningTable(statConclusions);
        gradeWarning.setTables(Lists.newArrayList(gradeWarningTable));
        gradeWarning.setInfo(getGradeWarning(statConclusions, gradeWarningTable));
        generalVision.setGradeWarning(gradeWarning);
        return generalVision;
    }

    private GradeLowVision.Info getGradeLowVisionInfo(List<StatConclusion> statConclusions, List<GradeLowVision.Table> tables) {

        Map<String, List<GradeLowVision.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeLowVision.Table::getName));

        GradeLowVision.Info info = new GradeLowVision.Info();

        info.setOne(highLowProportionService.getVisionMaxMinProportion(
                countAndProportionService.lowVision(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion(),
                gradeMap.get(GradeCodeEnum.ONE_KINDERGARTEN.getName()),
                s -> Float.valueOf(s.getLowVisionProportion())));

        info.setTwo(highLowProportionService.getVisionMaxMinProportion(
                countAndProportionService.lowVision(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion(),
                gradeMap.get(GradeCodeEnum.TWO_KINDERGARTEN.getName()),
                s -> Float.valueOf(s.getLowVisionProportion())));

        info.setThree(highLowProportionService.getVisionMaxMinProportion(
                countAndProportionService.lowVision(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion(),
                gradeMap.get(GradeCodeEnum.THREE_KINDERGARTEN.getName()),
                s -> Float.valueOf(s.getLowVisionProportion())));
        return info;
    }

    private GradeRefractive.Info getGradeRefractiveInfo(List<StatConclusion> statConclusions, List<GradeRefractive.Table> tables) {
        GradeRefractive.Info info = new GradeRefractive.Info();
        Map<String, List<GradeRefractive.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeRefractive.Table::getName));

        info.setOne(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.ONE_KINDERGARTEN.getName())));
        info.setTwo(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.TWO_KINDERGARTEN.getName())));
        info.setThree(getGradeRefractiveDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.THREE_KINDERGARTEN.getName())));
        return info;
    }

    private GradeRefractive.Detail getGradeRefractiveDetail(List<StatConclusion> statConclusions, List<GradeRefractive.Table> tables) {
        if (CollectionUtils.isEmpty(statConclusions) || CollectionUtils.isEmpty(tables)) {
            return null;
        }
        GradeRefractive.Detail detail = new GradeRefractive.Detail();
        detail.setInsufficient(highLowProportionService.getMaxMinProportion(countAndProportionService.insufficient(statConclusions).getProportion(), tables, s -> Float.valueOf(s.getInsufficientProportion())));
        detail.setRefractiveError(highLowProportionService.getMaxMinProportion(countAndProportionService.refractiveError(statConclusions).getProportion(), tables, s -> Float.valueOf(s.getRefractiveErrorProportion())));
        detail.setAnisometropia(highLowProportionService.getMaxMinProportion(countAndProportionService.anisometropia(statConclusions).getProportion(), tables, s -> Float.valueOf(s.getAnisometropiaProportion())));
        return detail;
    }

    private GradeWarning.Info getGradeWarning(List<StatConclusion> statConclusions, List<GradeWarning.Table> tables) {
        GradeWarning.Info info = new GradeWarning.Info();
        Map<String, List<GradeWarning.Table>> gradeMap = tables.stream()
                .filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .collect(Collectors.groupingBy(GradeWarning.Table::getName));
        info.setOne(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.ONE_KINDERGARTEN.getName())));
        info.setTwo(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.TWO_KINDERGARTEN.getName())));
        info.setThree(getGradeWarningDetail(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), gradeMap.get(GradeCodeEnum.THREE_KINDERGARTEN.getName())));
        return info;
    }

    private GradeWarning.Detail getGradeWarningDetail(List<StatConclusion> statConclusions, List<GradeWarning.Table> tables) {
        if (CollectionUtils.isEmpty(statConclusions) || CollectionUtils.isEmpty(tables)) {
            return null;
        }
        GradeWarning.Detail detail = new GradeWarning.Detail();
        detail.setWarningProportion(highLowProportionService.getWarningMaxMinProportion(countAndProportionService.warning(statConclusions).getProportion(), tables, s -> Float.valueOf(s.getWarningProportion())));
        detail.setRecommendDoctor(highLowProportionService.getWarningMaxMinProportion(commonReportService.recommendDoctorRefractive(statConclusions).getProportion(), tables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
        detail.setZeroWarning(countAndProportionService.zeroWarning(statConclusions).getProportion());
        detail.setOneWarning(countAndProportionService.oneWarning(statConclusions).getProportion());
        detail.setTwoWarning(countAndProportionService.twoWarning(statConclusions).getProportion());
        detail.setThreeWarning(countAndProportionService.threeWarning(statConclusions).getProportion());
        return detail;
    }
}
