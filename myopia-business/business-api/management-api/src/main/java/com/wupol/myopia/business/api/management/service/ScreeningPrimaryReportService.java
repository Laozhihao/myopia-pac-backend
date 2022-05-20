package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.base.util.ScreeningDataFormatUtils;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.PrimaryLowVisionInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.ScreeningDataReportTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.ClassScreeningData;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.LowMyopiaInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.ClassOverall;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.*;
import com.wupol.myopia.business.api.management.service.report.CommonReportService;
import com.wupol.myopia.business.api.management.service.report.CountAndProportionService;
import com.wupol.myopia.business.api.management.service.report.HighLowProportionService;
import com.wupol.myopia.business.api.management.service.report.ScreeningReportTableService;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查报告-中小学及以上
 *
 * @author Simple4H
 */
@Service
public class ScreeningPrimaryReportService {

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private CommonReportService commonReportService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningReportTableService screeningReportTableService;

    @Resource
    private HighLowProportionService highLowProportionService;

    @Resource
    private CountAndProportionService countAndProportionService;

    public PrimaryReportDTO generateReport(Integer planId, Integer schoolId, Integer noticeId) {
        PrimaryReportDTO reportDTO = new PrimaryReportDTO();

        List<StatConclusion> allStatList = statConclusionService.getByPlanIdSchoolIdNoticeId(planId, schoolId, noticeId);
        List<StatConclusion> statConclusions = commonReportService.getPList(allStatList);
        School school = schoolService.getBySchoolId(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(planId);

        reportDTO.setInfo(commonReportService.generateInfo(school));
        reportDTO.setOutline(generateOutline(statConclusions, school, plan));
        reportDTO.setPrimaryGeneralVision(generatePrimaryGeneralVision(statConclusions));
        reportDTO.setOveralls(generateOverall(statConclusions));
        reportDTO.setClassScreeningData(commonReportService.generateClassScreeningData(statConclusions, school, false));
        return reportDTO;
    }


    /**
     * 概述
     */
    private PrimarySchoolOutline generateOutline(List<StatConclusion> statConclusions, School school, ScreeningPlan plan) {
        PrimarySchoolOutline schoolOutline = new PrimarySchoolOutline();

        schoolOutline.setOutline(commonReportService.generateOutline(statConclusions, school, plan));
        List<StatConclusion> validList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        schoolOutline.setVisionSituation(commonReportService.getVisionSituation(validList));

        RefractionSituation refractionSituation = new RefractionSituation();
        refractionSituation.setMyopia(countAndProportionService.myopia(validList));
        refractionSituation.setNight(countAndProportionService.night(validList));
        refractionSituation.setEarly(countAndProportionService.earlyMyopia(validList));
        refractionSituation.setLight(countAndProportionService.lightMyopia(validList));
        refractionSituation.setHigh(countAndProportionService.highMyopia(validList));
        refractionSituation.setAstigmatism(countAndProportionService.astigmatism(validList));
        refractionSituation.setEnough(countAndProportionService.enough(validList));
        refractionSituation.setUncorrected(countAndProportionService.uncorrected(validList));
        refractionSituation.setUnder(countAndProportionService.under(validList));
        schoolOutline.setRefractionSituation(refractionSituation);

        schoolOutline.setVisionWarningSituation(commonReportService.getVisionWarningSituation(validList));

        schoolOutline.setRecommendDoctor(countAndProportionService.getRecommendDoctor(validList));

        PrimaryHistoryVision primaryHistoryVision = new PrimaryHistoryVision();
        List<MyopiaTable> tables = screeningReportTableService.historyMyopiaTables(validList);
        primaryHistoryVision.setTables(tables);
        PrimaryHistoryVision.Info info = new PrimaryHistoryVision.Info();
        info.setLowVision(commonReportService.getChainRatioProportion(tables.stream().map(MyopiaTable::getLowVisionProportion).collect(Collectors.toList())));
        info.setMyopia(commonReportService.getChainRatioProportion(tables.stream().map(MyopiaTable::getMyopiaProportion).collect(Collectors.toList())));
        info.setEarly(commonReportService.getChainRatioProportion(tables.stream().map(MyopiaTable::getEarlyProportion).collect(Collectors.toList())));
        info.setLightMyopia(commonReportService.getChainRatioProportion(tables.stream().map(MyopiaTable::getLightProportion).collect(Collectors.toList())));
        info.setHighMyopia(commonReportService.getChainRatioProportion(tables.stream().map(MyopiaTable::getHighProportion).collect(Collectors.toList())));
        primaryHistoryVision.setInfo(info);
        schoolOutline.setPrimaryHistoryVision(primaryHistoryVision);
        return schoolOutline;
    }

    /**
     * 视力总体情况
     */
    private PrimaryGeneralVision generatePrimaryGeneralVision(List<StatConclusion> statConclusions) {
        PrimaryGeneralVision primaryGeneralVision = new PrimaryGeneralVision();
        List<StatConclusion> validList = commonReportService.getPList(statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList()));
        primaryGeneralVision.setLowMyopiaInfo(generateLowMyopiaInfo(validList));
        primaryGeneralVision.setAstigmatismInfo(generateAstigmatismInfo(validList));
        primaryGeneralVision.setWearingGlassesInfo(generateWearingGlassesInfo(validList));
        primaryGeneralVision.setWarningSituation(commonReportService.getWarningSituation(statConclusions));
        return primaryGeneralVision;
    }

    /**
     * 视力低下情况
     */
    public LowMyopiaInfo generateLowMyopiaInfo(List<StatConclusion> statConclusions) {
        LowMyopiaInfo info = new LowMyopiaInfo();
        PrimaryLowVisionInfo primaryLowVisionInfo = new PrimaryLowVisionInfo();
        primaryLowVisionInfo.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        primaryLowVisionInfo.setLightLowVision(countAndProportionService.lightLowVision(statConclusions));
        primaryLowVisionInfo.setMiddleLowVision(countAndProportionService.middleLowVision(statConclusions));
        primaryLowVisionInfo.setHighLowVision(countAndProportionService.highLowVision(statConclusions));
        info.setInfo(primaryLowVisionInfo);


        GenderLowVision genderLowVision = new GenderLowVision();
        GenderLowVision.Info genderInfo = new GenderLowVision.Info();
        genderInfo.setAvgVision(commonReportService.getAvgVision(statConclusions));
        genderInfo.setLowVisionProportion(countAndProportionService.lowVision(statConclusions).getProportion());
        genderInfo.setLightLowVision(countAndProportionService.lightLowVision(statConclusions).getProportion());
        genderInfo.setMiddleLowVision(countAndProportionService.middleLowVision(statConclusions).getProportion());
        genderInfo.setHighLowVision(countAndProportionService.highLowVision(statConclusions).getProportion());
        genderInfo.setMLowVision(countAndProportionService.mLowVision(statConclusions).getProportion());
        genderInfo.setFLowVision(countAndProportionService.fLowVision(statConclusions).getProportion());
        genderLowVision.setInfo(genderInfo);
        genderLowVision.setTables(screeningReportTableService.lowVisionTables(statConclusions));
        info.setGenderLowVision(genderLowVision);

        GradeLowVision gradeLowVision = new GradeLowVision();
        List<LowVisionTable> gradeTables = screeningReportTableService.gradeLowVision(statConclusions);
        gradeLowVision.setTables(Lists.newArrayList(gradeTables));
        gradeLowVision.setInfo(getLowVisionInfo(gradeTables));
        info.setGradeLowVision(gradeLowVision);

        AgeLowVision ageLowVision = new AgeLowVision();
        ageLowVision.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<LowVisionTable> ageTables = screeningReportTableService.ageLowTable(statConclusions);
        ageLowVision.setTables(Lists.newArrayList(ageTables));
        ageLowVision.setInfo(getLowVisionInfo(ageTables));
        info.setAgeLowVision(ageLowVision);

        return info;
    }

    private LowVisionInfo getLowVisionInfo(List<LowVisionTable> tables) {
        List<LowVisionTable> collect = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        LowVisionInfo info = new LowVisionInfo();
        info.setLowVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getLowVisionProportion())));
        info.setLightVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getLightVisionProportion())));
        info.setMiddleVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getMiddleVisionProportion())));
        info.setHighVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getHighVisionProportion())));
        return info;
    }

    /**
     * 散光情况
     */
    public AstigmatismInfo generateAstigmatismInfo(List<StatConclusion> statConclusions) {
        AstigmatismInfo astigmatismInfo = new AstigmatismInfo();
        AstigmatismInfo.Info info = new AstigmatismInfo.Info();
        info.setMyopia(countAndProportionService.myopia(statConclusions));
        info.setEarlyMyopia(countAndProportionService.earlyMyopia(statConclusions));
        info.setLightMyopia(countAndProportionService.lightMyopia(statConclusions));
        info.setHighMyopia(countAndProportionService.highMyopia(statConclusions));
        info.setNightWearing(countAndProportionService.nightWearing(statConclusions));
        info.setAstigmatism(countAndProportionService.astigmatism(statConclusions));
        astigmatismInfo.setInfo(info);

        GenderAstigmatism genderAstigmatism = new GenderAstigmatism();
        GenderAstigmatism.Info genderInfo = new GenderAstigmatism.Info();
        genderInfo.setMyopia(commonReportService.myopiaRefractive(statConclusions));
        genderInfo.setEarlyMyopia(commonReportService.earlyMyopiaRefractive(statConclusions));
        genderInfo.setLightMyopia(commonReportService.lightMyopiaRefractive(statConclusions));
        genderInfo.setHighMyopia(commonReportService.highMyopiaRefractive(statConclusions));
        genderInfo.setAstigmatism(commonReportService.astigmatismRefractive(statConclusions));
        genderAstigmatism.setInfo(genderInfo);
        genderAstigmatism.setTables(screeningReportTableService.genderPrimaryRefractiveTable(statConclusions));
        astigmatismInfo.setGenderAstigmatism(genderAstigmatism);

        GradeAstigmatism gradeAstigmatism = new GradeAstigmatism();
        List<AstigmatismTable> gradeTables = screeningReportTableService.gradePrimaryRefractiveTable(statConclusions);
        gradeAstigmatism.setTables(Lists.newArrayList(gradeTables));
        gradeAstigmatism.setInfo(primaryAstigmatismInfo(gradeTables));
        astigmatismInfo.setGradeAstigmatism(gradeAstigmatism);

        AgeAstigmatism ageAstigmatism = new AgeAstigmatism();
        ageAstigmatism.setAgeInfo(commonReportService.getAgeRange(statConclusions));
        List<AstigmatismTable> ageTables = screeningReportTableService.ageAstigmatismTables(statConclusions);
        ageAstigmatism.setTables(Lists.newArrayList(ageTables));
        ageAstigmatism.setInfo(primaryAstigmatismInfo(ageTables));
        astigmatismInfo.setAgeAstigmatism(ageAstigmatism);
        return astigmatismInfo;
    }

    private PrimaryAstigmatismInfo primaryAstigmatismInfo(List<AstigmatismTable> tables) {
        List<AstigmatismTable> tableList = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        PrimaryAstigmatismInfo info = new PrimaryAstigmatismInfo();
        info.setMyopia(highLowProportionService.ageAstigmatismTableHP(tableList, s -> Float.valueOf(s.getMyopiaProportion())));
        info.setEarlyMyopia(highLowProportionService.ageAstigmatismTableHP(tableList, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
        info.setLightMyopia(highLowProportionService.ageAstigmatismTableHP(tableList, s -> Float.valueOf(s.getLightMyopiaProportion())));
        info.setHighMyopia(highLowProportionService.ageAstigmatismTableHP(tableList, s -> Float.valueOf(s.getHighMyopiaProportion())));
        info.setAstigmatism(highLowProportionService.ageAstigmatismTableHP(tableList, s -> Float.valueOf(s.getAstigmatismProportion())));
        return info;
    }

    public WearingGlassesInfo generateWearingGlassesInfo(List<StatConclusion> statConclusions) {
        WearingGlassesInfo wearingGlassesInfo = new WearingGlassesInfo();
        WearingGlassesInfo.Info info = new WearingGlassesInfo.Info();
        info.setNotWearing(countAndProportionService.notWearing(statConclusions));
        info.setWearingFrame(countAndProportionService.glasses(statConclusions));
        info.setWearingContact(countAndProportionService.contact(statConclusions));
        info.setNightWearing(countAndProportionService.nightWearing(statConclusions));
        info.setEnough(countAndProportionService.enough(statConclusions));
        info.setUncorrected(countAndProportionService.uncorrected(statConclusions));
        info.setUnder(countAndProportionService.under(statConclusions));
        wearingGlassesInfo.setInfo(info);

        GenderWearingGlasses genderWearingGlasses = new GenderWearingGlasses();
        List<AgeWearingTable> genderTables = screeningReportTableService.genderWearingTable(statConclusions);
        genderWearingGlasses.setTables(Lists.newArrayList(genderTables));
        genderWearingGlasses.setInfo(generateGenderWearingGlasses(statConclusions, commonReportService));
        wearingGlassesInfo.setGenderWearingGlasses(genderWearingGlasses);

        GradeWearingGlasses gradeWearingGlasses = new GradeWearingGlasses();
        List<AgeWearingTable> gradeTables = screeningReportTableService.gradePrimaryWearingTable(statConclusions);
        gradeWearingGlasses.setTables(Lists.newArrayList(gradeTables));
        gradeWearingGlasses.setInfo(primaryWearingInfo(gradeTables));
        wearingGlassesInfo.setGradeWearingGlasses(gradeWearingGlasses);

        AgeWearingGlasses ageWearingGlasses = new AgeWearingGlasses();
        ageWearingGlasses.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<AgeWearingTable> ageTables = screeningReportTableService.agePrimaryWearingTable(statConclusions);
        ageWearingGlasses.setTables(ageTables);
        ageWearingGlasses.setInfo(primaryWearingInfo(ageTables));
        wearingGlassesInfo.setAgeWearingGlasses(ageWearingGlasses);

        return wearingGlassesInfo;
    }

    /**
     * 性别戴镜情况
     */
    public GenderWearingGlasses.Info generateGenderWearingGlasses(List<StatConclusion> statConclusions, CommonReportService commonReportService) {
        GenderWearingGlasses.Info genderInfo = new GenderWearingGlasses.Info();
        genderInfo.setNotWearing(commonReportService.notWearingRefractive(statConclusions));
        genderInfo.setGlasses(commonReportService.glassesRefractive(statConclusions));
        genderInfo.setContact(commonReportService.contactRefractive(statConclusions));
        genderInfo.setNight(commonReportService.nightRefractive(statConclusions));
        genderInfo.setEnough(commonReportService.enoughRefractive(statConclusions));
        genderInfo.setUnder(commonReportService.underRefractive(statConclusions));
        genderInfo.setUncorrected(commonReportService.uncorrectedRefractive(statConclusions));
        return genderInfo;
    }

    /**
     * 戴镜情况
     */
    public PrimaryWearingInfo primaryWearingInfo(List<AgeWearingTable> tables) {
        List<AgeWearingTable> collect = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .filter(s -> !SchoolAge.getAllDesc().contains(s.getName())).collect(Collectors.toList());
        PrimaryWearingInfo info = new PrimaryWearingInfo();
        info.setNotWearing(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getNotWearingProportion())));
        info.setGlasses(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getGlassesProportion())));
        info.setContact(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getWearingContactProportion())));
        info.setNight(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getNightWearingProportion())));
        info.setEnough(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getEnoughProportion())));
        info.setUnder(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getUnderProportion())));
        info.setUncorrected(highLowProportionService.ageWearingTableHP(collect, s -> Float.valueOf(s.getUncorrectedProportion())));
        return info;
    }

    /**
     * 各班级整体情况
     */
    private List<ClassOverall> generateOverall(List<StatConclusion> statConclusions) {
        List<ClassOverall> list = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        collect.forEach((k, v) -> {
            ClassOverall classOverall = new ClassOverall();
            classOverall.setName(GradeCodeEnum.getName(k));
            classOverall.setInfo(commonReportService.getPrimaryOverall(screeningReportTableService.primaryScreeningInfoTables(statConclusions), v));
            list.add(classOverall);
        });
        return list;
    }


}
