package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.PrimaryLowVisionInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.SchoolReportInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.ClassScreeningData;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.LowMyopiaInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.ClassOverall;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.*;
import com.wupol.myopia.business.api.management.service.report.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 筛查报告-中小学及以上
 *
 * @author Simple4H
 */
@Service
@Log4j2
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

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private HorizontalChartService horizontalChartService;

    @Resource
    private PortraitChartService portraitChartService;

    @Resource
    private PieChartService pieChartService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    public PrimaryReportDTO generateReport(Integer planId, Integer schoolId, Integer noticeId) {
        PrimaryReportDTO reportDTO = new PrimaryReportDTO();

        List<StatConclusion> allStatList = statConclusionService.getByPlanIdSchoolIdNoticeId(planId, schoolId, noticeId);
        if (CollectionUtils.isEmpty(allStatList)) {
            return reportDTO;
        }
        List<StatConclusion> statConclusions = commonReportService.getPList(allStatList);
        School school = schoolService.getBySchoolId(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(planId);

        CompletableFuture<SchoolReportInfo> c1 = CompletableFuture.supplyAsync(() -> {
            SchoolReportInfo info = commonReportService.generateInfo(school, plan);
            reportDTO.setInfo(info);
            return info;
        }, executor);
        CompletableFuture<PrimarySchoolOutline> c2 = CompletableFuture.supplyAsync(() -> {
            PrimarySchoolOutline outline = generateOutline(statConclusions, school, plan);
            reportDTO.setOutline(outline);
            return outline;
        }, executor);
        CompletableFuture<PrimaryGeneralVision> c3 = CompletableFuture.supplyAsync(() -> {
            PrimaryGeneralVision primaryGeneralVision = generatePrimaryGeneralVision(statConclusions);
            reportDTO.setPrimaryGeneralVision(primaryGeneralVision);
            return primaryGeneralVision;
        }, executor);
        CompletableFuture<List<ClassOverall>> c4 = CompletableFuture.supplyAsync(() -> {
            List<ClassOverall> classOveralls = generateOverall(statConclusions, schoolId);
            reportDTO.setOveralls(classOveralls);
            return classOveralls;
        }, executor);
        CompletableFuture<List<ClassScreeningData>> c5 = CompletableFuture.supplyAsync(() -> {
            List<ClassScreeningData> classScreeningData = commonReportService.generateClassScreeningData(school, plan, false);
            reportDTO.setClassScreeningData(classScreeningData);
            return classScreeningData;
        }, executor);


        try {
            CompletableFuture.allOf(c1, c2, c3, c4, c5).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("生成区域视力筛查报告异常,planId:{},schoolId:{},noticeId:{}", planId, schoolId, noticeId);
            e.printStackTrace();
            throw new BusinessException("生成区域视力筛查报告异常");
        }
        return reportDTO;
    }


    /**
     * 概述
     */
    private PrimarySchoolOutline generateOutline(List<StatConclusion> statConclusions, School school, ScreeningPlan plan) {
        PrimarySchoolOutline schoolOutline = new PrimarySchoolOutline();
        schoolOutline.setOutline(commonReportService.generateOutline(statConclusions, school, plan, false));
        List<StatConclusion> validList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        long total = validList.size();
        schoolOutline.setVisionSituation(commonReportService.getVisionSituation(validList));

        RefractionSituation refractionSituation = new RefractionSituation();
        refractionSituation.setMyopia(countAndProportionService.myopia(validList, total));
        refractionSituation.setNight(countAndProportionService.night(validList, total));
        refractionSituation.setEarly(countAndProportionService.earlyMyopia(validList, total));
        refractionSituation.setLight(countAndProportionService.lightMyopia(validList, total));
        refractionSituation.setHigh(countAndProportionService.highMyopia(validList, total));
        refractionSituation.setAstigmatism(countAndProportionService.astigmatism(validList, total));
        refractionSituation.setEnough(countAndProportionService.enough(validList, total));
        refractionSituation.setUncorrected(countAndProportionService.uncorrected(validList, total));
        refractionSituation.setUnder(countAndProportionService.under(validList, total));
        schoolOutline.setRefractionSituation(refractionSituation);
        schoolOutline.setVisionWarningSituation(commonReportService.getVisionWarningSituation(validList, total));
        schoolOutline.setRecommendDoctor(countAndProportionService.getRecommendDoctor(validList, total));
        schoolOutline.setPrimaryHistoryVision(getPrimaryHistoryVision(school, plan));
        return schoolOutline;
    }

    private PrimaryHistoryVision getPrimaryHistoryVision(School school, ScreeningPlan plan) {
        PrimaryHistoryVision primaryHistoryVision = new PrimaryHistoryVision();

        List<MyopiaTable> tables = screeningReportTableService.historyMyopiaTables(commonReportService.getSchoolHistoryData(school.getId(), false), plan.getId());
        primaryHistoryVision.setTables(tables);
        PrimaryHistoryVision.Info info = new PrimaryHistoryVision.Info();
        if (!CollectionUtils.isEmpty(tables) && tables.size() > 1) {
            info.setLowVision(commonReportService.getConvertRatio(tables,MyopiaTable::getLowVisionProportion));
            info.setMyopia(commonReportService.getConvertRatio(tables,MyopiaTable::getMyopiaProportion));
            info.setEarly(commonReportService.getConvertRatio(tables,MyopiaTable::getEarlyProportion));
            info.setLightMyopia(commonReportService.getConvertRatio(tables,MyopiaTable::getLightProportion));
            info.setHighMyopia(commonReportService.getConvertRatio(tables,MyopiaTable::getHighProportion));
            primaryHistoryVision.setInfo(info);
            primaryHistoryVision.setPrimaryHistoryVisionChart(horizontalChartService.myopiaTableChart(tables));
            primaryHistoryVision.setPrimaryLevelHistoryVisionChart(horizontalChartService.myopiaLevelTableChart(tables));
        }
        return primaryHistoryVision;
    }

    /**
     * 视力总体情况
     */
    private PrimaryGeneralVision generatePrimaryGeneralVision(List<StatConclusion> statConclusions) {
        PrimaryGeneralVision primaryGeneralVision = new PrimaryGeneralVision();
        List<StatConclusion> validList = commonReportService.getPList(statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList()));
        long total = validList.size();
        if (total == 0L) {
            return primaryGeneralVision;
        }
        primaryGeneralVision.setLowMyopiaInfo(generateLowMyopiaInfo(validList, total));
        primaryGeneralVision.setAstigmatismInfo(generateAstigmatismInfo(validList, total));
        primaryGeneralVision.setWearingGlassesInfo(generateWearingGlassesInfo(validList, total));
        primaryGeneralVision.setWarningSituation(commonReportService.getWarningSituation(statConclusions, false, total));
        return primaryGeneralVision;
    }

    /**
     * 视力低下情况
     */
    public LowMyopiaInfo generateLowMyopiaInfo(List<StatConclusion> statConclusions, Long total) {
        LowMyopiaInfo info = new LowMyopiaInfo();
        PrimaryLowVisionInfo primaryLowVisionInfo = new PrimaryLowVisionInfo();
        primaryLowVisionInfo.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        primaryLowVisionInfo.setLightLowVision(countAndProportionService.lightLowVision(statConclusions, total));
        primaryLowVisionInfo.setMiddleLowVision(countAndProportionService.middleLowVision(statConclusions, total));
        primaryLowVisionInfo.setHighLowVision(countAndProportionService.highLowVision(statConclusions, total));
        info.setInfo(primaryLowVisionInfo);


        GenderLowVision genderLowVision = new GenderLowVision();
        GenderLowVision.Info genderInfo = new GenderLowVision.Info();
        genderInfo.setAvgVision(commonReportService.getAvgVision(statConclusions));
        genderInfo.setLowVisionProportion(countAndProportionService.lowVision(statConclusions, total).getProportion());
        genderInfo.setLightLowVision(countAndProportionService.lightLowVision(statConclusions, total).getProportion());
        genderInfo.setMiddleLowVision(countAndProportionService.middleLowVision(statConclusions, total).getProportion());
        genderInfo.setHighLowVision(countAndProportionService.highLowVision(statConclusions, total).getProportion());
        genderInfo.setMLowVision(countAndProportionService.mLowVision(statConclusions).getProportion());
        genderInfo.setFLowVision(countAndProportionService.fLowVision(statConclusions).getProportion());
        genderLowVision.setInfo(genderInfo);
        genderLowVision.setGenderLowVisionChart(pieChartService.genderLowVisionChart(statConclusions, total));
        genderLowVision.setLowVisionChart(pieChartService.levelLowVisionChart(statConclusions, total));
        genderLowVision.setTables(screeningReportTableService.lowVisionTables(statConclusions, total));
        info.setGenderLowVision(genderLowVision);

        GradeLowVision gradeLowVision = new GradeLowVision();
        List<CommonLowVisionTable> gradeTables = screeningReportTableService.gradeLowVision(statConclusions, total);
        gradeLowVision.setTables(Lists.newArrayList(gradeTables));
        if (commonReportService.isShowInfo(gradeTables, true)) {
            gradeLowVision.setInfo(getLowVisionInfo(gradeTables));
            gradeLowVision.setGradeLowVisionChart(portraitChartService.lowVisionChart(gradeTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
        }
        info.setGradeLowVision(gradeLowVision);

        AgeLowVision ageLowVision = new AgeLowVision();
        ageLowVision.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<CommonLowVisionTable> ageTables = screeningReportTableService.ageLowTable(statConclusions, total);
        ageLowVision.setTables(Lists.newArrayList(ageTables));

        if (commonReportService.isShowInfo(ageTables, true)) {
            ageLowVision.setAgeLowVisionChart(horizontalChartService.lowVisionChart(ageTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList()), true));
            ageLowVision.setInfo(getLowVisionInfo(ageTables));
        }
        info.setAgeLowVision(ageLowVision);

        return info;
    }

    private LowVisionInfo getLowVisionInfo(List<CommonLowVisionTable> tables) {
        List<CommonLowVisionTable> collect = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        LowVisionInfo info = new LowVisionInfo();
        info.setLowVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getLowVisionProportion())));
        info.setLightVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getLightLowVisionProportion())));
        info.setMiddleVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getMiddleLowVisionProportion())));
        info.setHighVision(highLowProportionService.lowVisionTableHP(collect, s -> Float.valueOf(s.getHighLowVisionProportion())));
        return info;
    }

    /**
     * 散光情况
     */
    public AstigmatismInfo generateAstigmatismInfo(List<StatConclusion> statConclusions, Long total) {
        AstigmatismInfo astigmatismInfo = new AstigmatismInfo();
        AstigmatismInfo.Info info = new AstigmatismInfo.Info();
        info.setMyopia(countAndProportionService.myopia(statConclusions, total));
        info.setEarlyMyopia(countAndProportionService.earlyMyopia(statConclusions, total));
        info.setLightMyopia(countAndProportionService.lightMyopia(statConclusions, total));
        info.setHighMyopia(countAndProportionService.highMyopia(statConclusions, total));
        info.setNightWearing(countAndProportionService.nightWearing(statConclusions, total));
        info.setAstigmatism(countAndProportionService.astigmatism(statConclusions, total));
        astigmatismInfo.setInfo(info);

        GenderAstigmatism genderAstigmatism = new GenderAstigmatism();
        GenderAstigmatism.Info genderInfo = new GenderAstigmatism.Info();
        genderInfo.setMyopia(commonReportService.myopiaRefractive(statConclusions, total));
        genderInfo.setEarlyMyopia(commonReportService.earlyMyopiaRefractive(statConclusions, total));
        genderInfo.setLightMyopia(commonReportService.lightMyopiaRefractive(statConclusions, total));
        genderInfo.setHighMyopia(commonReportService.highMyopiaRefractive(statConclusions, total));
        genderInfo.setAstigmatism(commonReportService.astigmatismRefractive(statConclusions, total));
        genderAstigmatism.setInfo(genderInfo);
        List<AstigmatismTable> astigmatismTables = screeningReportTableService.genderPrimaryRefractiveTable(statConclusions, total);
        genderAstigmatism.setTables(astigmatismTables);
        genderAstigmatism.setGenderAstigmatismChart(horizontalChartService.genderAstigmatismChart(astigmatismTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
        astigmatismInfo.setGenderAstigmatism(genderAstigmatism);

        GradeAstigmatism gradeAstigmatism = new GradeAstigmatism();
        List<AstigmatismTable> gradeTables = screeningReportTableService.gradePrimaryRefractiveTable(statConclusions, total);
        gradeAstigmatism.setTables(Lists.newArrayList(gradeTables));
        if (commonReportService.isShowInfo(gradeTables, true)) {
            gradeAstigmatism.setGradeAstigmatismChart(portraitChartService.gradeRefractionChart(gradeTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            gradeAstigmatism.setInfo(primaryAstigmatismInfo(gradeTables));
        }
        astigmatismInfo.setGradeAstigmatism(gradeAstigmatism);

        AgeAstigmatism ageAstigmatism = new AgeAstigmatism();
        ageAstigmatism.setAgeInfo(commonReportService.getAgeRange(statConclusions));
        List<AstigmatismTable> ageTables = screeningReportTableService.ageAstigmatismTables(statConclusions, total);
        ageAstigmatism.setTables(Lists.newArrayList(ageTables));
        if (commonReportService.isShowInfo(ageTables, true)) {
            ageAstigmatism.setAgeAstigmatismChart(horizontalChartService.astigmatismMyopiaChart(ageTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            ageAstigmatism.setAgeLevelAstigmatismChart(horizontalChartService.astigmatismMyopiaLevelChart(ageTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            ageAstigmatism.setInfo(primaryAstigmatismInfo(ageTables));
        }
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

    public WearingGlassesInfo generateWearingGlassesInfo(List<StatConclusion> statConclusions, Long total) {
        WearingGlassesInfo wearingGlassesInfo = new WearingGlassesInfo();
        WearingGlassesInfo.Info info = new WearingGlassesInfo.Info();
        info.setNotWearing(countAndProportionService.notWearing(statConclusions, total));
        info.setWearingFrame(countAndProportionService.glasses(statConclusions, total));
        info.setWearingContact(countAndProportionService.contact(statConclusions, total));
        info.setNightWearing(countAndProportionService.nightWearing(statConclusions, total));
        info.setEnough(countAndProportionService.enough(statConclusions, total));
        info.setUncorrected(countAndProportionService.uncorrected(statConclusions, total));
        info.setUnder(countAndProportionService.under(statConclusions, total));
        wearingGlassesInfo.setInfo(info);

        GenderWearingGlasses genderWearingGlasses = getGenderWearingGlasses(statConclusions, total);
        wearingGlassesInfo.setGenderWearingGlasses(genderWearingGlasses);

        GradeWearingGlasses gradeWearingGlasses = new GradeWearingGlasses();
        List<AgeWearingTable> gradeTables = screeningReportTableService.gradePrimaryWearingTable(statConclusions, total);
        gradeWearingGlasses.setTables(Lists.newArrayList(gradeTables));
        if (commonReportService.isShowInfo(gradeTables, true)) {
            gradeWearingGlasses.setGradeWearingGlassesChart(portraitChartService.wearingGlassesWearingChartY(gradeTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            gradeWearingGlasses.setGradeVisionCorrectionChart(portraitChartService.visionCorrectionWearingChartY(gradeTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            gradeWearingGlasses.setInfo(primaryWearingInfo(statConclusions, gradeTables, total));
        }
        wearingGlassesInfo.setGradeWearingGlasses(gradeWearingGlasses);

        AgeWearingGlasses ageWearingGlasses = new AgeWearingGlasses();
        ageWearingGlasses.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<AgeWearingTable> ageTables = screeningReportTableService.agePrimaryWearingTable(statConclusions, total);
        ageWearingGlasses.setTables(Lists.newArrayList(ageTables));
        if (commonReportService.isShowInfo(gradeTables, true)) {
            ageWearingGlasses.setWearingGlassesChart(horizontalChartService.primaryWearingGlassesChart(ageTables.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList()), true));
            ageWearingGlasses.setVisionCorrectionChart(horizontalChartService.primaryGenderVisionCorrectionChart(ageTables.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList())));
            ageWearingGlasses.setInfo(primaryWearingInfo(statConclusions, ageTables, total));
        }
        wearingGlassesInfo.setAgeWearingGlasses(ageWearingGlasses);

        return wearingGlassesInfo;
    }

    public GenderWearingGlasses getGenderWearingGlasses(List<StatConclusion> statConclusions, Long total) {
        GenderWearingGlasses genderWearingGlasses = new GenderWearingGlasses();
        List<AgeWearingTable> genderTables = screeningReportTableService.genderWearingTable(statConclusions, total);
        genderWearingGlasses.setTables(Lists.newArrayList(genderTables));
        genderWearingGlasses.setGenderWearingGlassesChart(horizontalChartService.primaryWearingGlassesChart(genderTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList()),false));
        genderWearingGlasses.setGenderVisionCorrectionChart(horizontalChartService.primaryGenderVisionCorrectionChart(genderTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
        genderWearingGlasses.setInfo(generateGenderWearingGlasses(statConclusions, commonReportService, total));
        return genderWearingGlasses;
    }

    /**
     * 性别戴镜情况
     */
    public GenderWearingGlasses.Info generateGenderWearingGlasses(List<StatConclusion> statConclusions, CommonReportService commonReportService, Long total) {
        GenderWearingGlasses.Info genderInfo = new GenderWearingGlasses.Info();
        genderInfo.setNotWearing(commonReportService.notWearingRefractive(statConclusions, total));
        genderInfo.setGlasses(commonReportService.glassesRefractive(statConclusions, total));
        genderInfo.setContact(commonReportService.contactRefractive(statConclusions, total));
        genderInfo.setNight(commonReportService.nightRefractive(statConclusions, total));
        genderInfo.setEnough(commonReportService.enoughRefractive(statConclusions, total));
        genderInfo.setUnder(commonReportService.underRefractive(statConclusions, total));
        genderInfo.setUncorrected(commonReportService.uncorrectedRefractive(statConclusions, total));
        return genderInfo;
    }

    /**
     * 戴镜情况
     */
    public PrimaryWearingInfo primaryWearingInfo(List<StatConclusion> statConclusions, List<AgeWearingTable> tables, Long total) {
        List<AgeWearingTable> collect = tables.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList());
        PrimaryWearingInfo info = new PrimaryWearingInfo();
        info.setNotWearing(highLowProportionService.ageWearingTableHP(countAndProportionService.notWearing(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getNotWearingProportion())));
        info.setGlasses(highLowProportionService.ageWearingTableHP(countAndProportionService.glasses(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getGlassesProportion())));
        info.setContact(highLowProportionService.ageWearingTableHP(countAndProportionService.contact(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getWearingContactProportion())));
        info.setNight(highLowProportionService.ageWearingTableHP(countAndProportionService.nightWearing(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getNightWearingProportion())));
        info.setEnough(highLowProportionService.ageWearingTableHP(countAndProportionService.enough(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getEnoughProportion())));
        info.setUnder(highLowProportionService.ageWearingTableHP(countAndProportionService.under(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getUnderProportion())));
        info.setUncorrected(highLowProportionService.ageWearingTableHP(countAndProportionService.uncorrected(statConclusions, total).getProportion(), collect, s -> Float.valueOf(s.getUncorrectedProportion())));
        return info;
    }

    /**
     * 各班级整体情况
     */
    private List<ClassOverall> generateOverall(List<StatConclusion> statConclusions, Integer schoolId) {

        List<ClassOverall> list = new ArrayList<>();
        long total = statConclusions.size();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<SchoolGrade> gradeList = schoolGradeService.getBySchoolId(schoolId);
        gradeList.forEach(k -> {
            List<StatConclusion> v = collect.get(k.getGradeCode());
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            ClassOverall classOverall = new ClassOverall();
            classOverall.setName(GradeCodeEnum.getDesc(k.getGradeCode()));
            List<PrimaryScreeningInfoTable> tables = screeningReportTableService.primaryScreeningInfoTables(v, total);
            tables.sort(Comparator.comparing((PrimaryScreeningInfoTable s) -> Float.valueOf(s.getMyopiaProportion())).reversed());
            classOverall.setInfo(commonReportService.getPrimaryOverall(tables, v, total));
            list.add(classOverall);
        });
        return list;
    }


}
