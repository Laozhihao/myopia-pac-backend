package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.PrimaryLowVisionInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.*;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.AgeRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.AreaRefraction;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.KindergartenInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.SchoolAgeRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.*;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.AreaHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import com.wupol.myopia.business.api.management.service.report.*;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 筛查报告-区域
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ScreeningAreaReportService {

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private DistrictService districtService;

    @Resource
    private CommonReportService commonReportService;

    @Resource
    private ScreeningReportTableService screeningReportTableService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private ScreeningPrimaryReportService screeningPrimaryReportService;

    @Resource
    private HighLowProportionService highLowProportionService;

    @Resource
    private CountAndProportionService countAndProportionService;

    @Resource
    private HorizontalChartService horizontalChartService;

    @Resource
    private PortraitChartService portraitChartService;

    @Resource
    private PieChartService pieChartService;

    @Resource
    private ScreeningNoticeService screeningNoticeService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    public ScreeningAreaReportDTO generateReport(Integer noticeId, Integer districtId) {
        List<Integer> childDistrictIds;
        childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        childDistrictIds.add(districtId);
        List<StatConclusion> statConclusions = statConclusionService.getByNoticeIdDistrictIds(noticeId, childDistrictIds);
        if (CollectionUtils.isEmpty(statConclusions)) {
            return new ScreeningAreaReportDTO();
        }
        ScreeningAreaReportDTO reportDTO = new ScreeningAreaReportDTO();

        CompletableFuture<AreaReportInfo> c1 = CompletableFuture.supplyAsync(() -> {
            AreaReportInfo info = generateAreaReportInfo(noticeId, districtId);
            reportDTO.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<AreaOutline> c2 = CompletableFuture.supplyAsync(() -> {
            AreaOutline areaOutline = generateAreaOutline(statConclusions, districtId);
            reportDTO.setAreaOutline(areaOutline);
            return areaOutline;
        }, executor);

        CompletableFuture<AreaGeneralVision> c3 = CompletableFuture.supplyAsync(() -> {
            AreaGeneralVision areaGeneralVision = generateAreaGeneralVision(statConclusions, noticeId, districtId);
            reportDTO.setAreaGeneralVision(areaGeneralVision);
            return areaGeneralVision;
        }, executor);

        CompletableFuture<SchoolScreeningInfo> c4 = CompletableFuture.supplyAsync(() -> {
            SchoolScreeningInfo schoolScreeningInfo = schoolScreeningInfo(statConclusions);
            reportDTO.setSchoolScreeningInfo(schoolScreeningInfo);
            return schoolScreeningInfo;
        }, executor);

        try {
            CompletableFuture.allOf(c1, c2, c3, c4).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("生成区域视力筛查报告异常,noticeId:{},districtId:{}", noticeId, districtId);
            throw new BusinessException("生成区域视力筛查报告异常");
        }
        return reportDTO;
    }

    /**
     * 生成概览信息
     *
     * @param districtId 区域
     */
    private AreaReportInfo generateAreaReportInfo(Integer noticeId, Integer districtId) {
        AreaReportInfo info = new AreaReportInfo();
        info.setArea(districtService.getDistrictNameByDistrictId(districtId));

        // 设置时间
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        Integer maxYear = DateUtil.getYear(screeningNotice.getStartTime());
        Integer minYear = DateUtil.getYear(screeningNotice.getEndTime());
        if (maxYear.equals(minYear)) {
            info.setDate(String.valueOf(maxYear));
        } else {
            info.setDate(maxYear + "-" + minYear);
        }
        return info;
    }

    /**
     * 概述信息
     *
     * @param statConclusions 结论
     */
    private AreaOutline generateAreaOutline(List<StatConclusion> statConclusions, Integer districtId) {
        AreaOutline areaOutline = new AreaOutline();
        long total = statConclusions.size();
        areaOutline.setInfo(areaOutlineInfo(districtId, statConclusions));
        areaOutline.setTables(screeningReportTableService.areaOutlineTable(statConclusions));

        areaOutline.setKindergarten(areaOutlineKindergarten(commonReportService.getKList(statConclusions)));

        List<StatConclusion> pList = commonReportService.getPList(statConclusions);
        areaOutline.setPrimary(areaOutlinePrimary(pList, (long) pList.size()));

        areaOutline.setVisionWarningSituation(commonReportService.getVisionWarningSituation(statConclusions, total));
        areaOutline.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions, total));
        return areaOutline;
    }

    /**
     * 概述
     */
    private AreaOutline.Info areaOutlineInfo(Integer districtId, List<StatConclusion> statConclusions) {
        AreaOutline.Info info = new AreaOutline.Info();
        info.setArea(districtService.getDistrictNameByDistrictId(districtId));

        List<Integer> schoolAges = statConclusions.stream().map(StatConclusion::getSchoolAge).distinct().collect(Collectors.toList());
        List<String> schoolAgeName = SchoolAge.batchNameByCode(SchoolAge.sortList(schoolAges));
        if (commonReportService.isHaveSenior(statConclusions)) {
            schoolAgeName.add(schoolAgeName.indexOf(SchoolAge.HIGH.desc) + 1, "高中");
        }

        info.setSchoolAge(String.join("、", SchoolAge.sortByNameList(schoolAgeName)));

        info.setSchoolCount(commonReportService.getSchoolCount(statConclusions));

        List<Integer> noticeIds = statConclusions.stream().map(StatConclusion::getSrcScreeningNoticeId).distinct().collect(Collectors.toList());
        List<Integer> schoolIds = statConclusions.stream().map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
        int expectedStudentCount = screeningPlanSchoolStudentService.getByNoticeIdsAndSchoolIds(noticeIds, schoolIds).size();
        info.setStudentTotal(expectedStudentCount);
        info.setValidTotal(statConclusions.stream().filter(StatConclusion::getIsValid).count());
        info.setUnScreening(expectedStudentCount - info.getValidTotal());
        return info;

    }

    /**
     * 幼儿园概览
     */
    private Kindergarten areaOutlineKindergarten(List<StatConclusion> statConclusions) {
        Kindergarten kindergarten = new Kindergarten();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return null;
        }
        long validCount = statConclusions.stream().filter(StatConclusion::getIsValid).count();

        // 视力情况
        kindergarten.setVisionSituation(commonReportService.getVisionSituation(statConclusions));

        // 远视储备不足
        long insufficientCount = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.code)).count();
        kindergarten.setInsufficientFarsightednessReserve(new CountAndProportion(insufficientCount, BigDecimalUtil.divide(insufficientCount, validCount)));

        // 是否屈光参差
        long anisometropiaCount = statConclusions.stream().filter(StatConclusion::getIsAnisometropia).count();
        kindergarten.setAnisometropia(new CountAndProportion(anisometropiaCount, BigDecimalUtil.divide(anisometropiaCount, validCount)));

        // 屈光不正
        long RefractiveErrorCount = statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        kindergarten.setRefractiveError(new CountAndProportion(RefractiveErrorCount, BigDecimalUtil.divide(RefractiveErrorCount, validCount)));
        return kindergarten;
    }

    /**
     * 小学及以上概览
     */
    private Primary areaOutlinePrimary(List<StatConclusion> statConclusions, Long total) {
        Primary primary = new Primary();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return null;
        }
        primary.setVisionSituation(commonReportService.getVisionSituation(statConclusions));
        primary.setMyopia(countAndProportionService.myopia(statConclusions, total));

        // 近视详情(小学阶段、初中阶段、高中阶段)
        primary.setPrimaryProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.privateSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList()), total));
        primary.setJuniorProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.juniorSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList()), total));
        primary.setHighProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.highSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList()), total));
        primary.setSeniorProportion(countAndProportionService.myopia(commonReportService.getSeniorList(statConclusions), total));
        primary.setVocationalHighProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.vocationalHighSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList()), total));

        primary.setEarly(countAndProportionService.earlyMyopia(statConclusions, total));
        primary.setLowMyopia(countAndProportionService.lightMyopia(statConclusions, total));
        primary.setHighMyopia(countAndProportionService.highMyopia(statConclusions, total));
        primary.setFootOrthosis(countAndProportionService.enough(statConclusions, total));
        primary.setUncorrected(countAndProportionService.uncorrected(statConclusions, total));
        primary.setUndercorrection(countAndProportionService.under(statConclusions, total));
        return primary;

    }

    /**
     * 视力总体情况
     *
     * @return AreaGeneralVision
     */
    private AreaGeneralVision generateAreaGeneralVision(List<StatConclusion> statConclusions, Integer noticeId, Integer districtId) {
        AreaGeneralVision areaGeneralVision = new AreaGeneralVision();
        List<StatConclusion> validList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        long total = validList.size();
        areaGeneralVision.setAreaLowVision(generateAreaLowVision(validList, total, noticeId, commonReportService.getHistoryData(districtId, null)));
        areaGeneralVision.setAreaRefraction(generateAreaRefraction(validList, noticeId, districtId));
        areaGeneralVision.setWarningSituation(commonReportService.getWarningSituation(statConclusions, true, total));
        return areaGeneralVision;
    }

    /**
     * 视力低下
     *
     * @return AreaLowVision
     */
    private AreaLowVision generateAreaLowVision(List<StatConclusion> statConclusions, Long total, Integer noticeId, List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples) {
        AreaLowVision areaLowVision = new AreaLowVision();
        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        AreaLowVision.Info info = new AreaLowVision.Info();

        boolean haveK = !CollectionUtils.isEmpty(kList);
        boolean haveP = !CollectionUtils.isEmpty(pList);
        if (haveK) {
            // 幼儿园
            AreaLowVision.Kindergarten kindergarten = new AreaLowVision.Kindergarten();
            kindergarten.setVisionSituation(commonReportService.getVisionSituation(kList));
            info.setKindergarten(kindergarten);
        }

        if (haveP) {
            // 小学及以上
            PrimaryLowVisionInfo primary = new PrimaryLowVisionInfo();
            primary.setVisionSituation(commonReportService.getVisionSituation(pList));
            long pCount = pList.size();
            primary.setLightLowVision(countAndProportionService.lightLowVision(pList, pCount));
            primary.setMiddleLowVision(countAndProportionService.middleLowVision(pList, pCount));
            primary.setHighLowVision(countAndProportionService.highLowVision(pList, pCount));
            info.setPrimary(primary);
        }
        areaLowVision.setInfo(info);

        areaLowVision.setGenderSexLowVision(generateGenderSexLowVision(statConclusions, total));
        areaLowVision.setSchoolAgeLowVision(generateSchoolAgeLowVision(statConclusions, total));
        areaLowVision.setAgeLowVision(generateAgeLowVision(statConclusions, total));

        LowVisionHistory lowVisionHistory = getLowVisionHistory(tuples, noticeId, haveK, haveP);
        areaLowVision.setLowVisionHistory(lowVisionHistory);
        return areaLowVision;
    }

    private LowVisionHistory getLowVisionHistory(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples, Integer noticeId,
                                                 boolean haveK, boolean haveP) {
        LowVisionHistory lowVisionHistory = new LowVisionHistory();
        if (CollectionUtils.isEmpty(tuples)) {
            return lowVisionHistory;
        }
        List<AreaHistoryLowVisionTable> tables = screeningReportTableService.historySchoolAgeLowVisionTables(tuples, noticeId);

        if (!CollectionUtils.isEmpty(tables)) {
            if (haveK) {
                lowVisionHistory.setKProportion(commonReportService.getConvertRatio(tables, AreaHistoryLowVisionTable::getKLowVisionProportion));
            }
            if (haveP) {
                lowVisionHistory.setPProportion(commonReportService.getConvertRatio(tables, AreaHistoryLowVisionTable::getLowVisionProportion));
            }
        }
        lowVisionHistory.setTables(Lists.newArrayList(tables));
        lowVisionHistory.setHistoryLowVisionChart(horizontalChartService.areaHistoryLowVisionChart(tables));
        return lowVisionHistory;
    }

    /**
     * 不同性别视力低下
     *
     * @param statConclusions 结果
     *
     * @return GenderSexLowVision
     */
    private GenderSexLowVision generateGenderSexLowVision(List<StatConclusion> statConclusions, Long total) {
        GenderSexLowVision genderSexLowVision = new GenderSexLowVision();
        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);
        GenderSexLowVision.Info info = new GenderSexLowVision.Info();

        // 幼儿园
        if (!CollectionUtils.isEmpty(kList)) {
            GenderSexLowVision.Kindergarten kindergarten = new GenderSexLowVision.Kindergarten();
            kindergarten.setAvgVision(commonReportService.getAvgVision(kList));
            kindergarten.setLowVisionProportion(countAndProportionService.lowVision(kList, total).getProportion());
            kindergarten.setMAvgVision(countAndProportionService.lowVision(commonReportService.getMList(kList), total).getProportion());
            kindergarten.setFAvgVision(countAndProportionService.lowVision(commonReportService.getFList(kList), total).getProportion());
            genderSexLowVision.setKindergartenGenderPieChart(pieChartService.areaGenderPieChart(kList, total));
            info.setKindergarten(kindergarten);
        }

        if (!CollectionUtils.isEmpty(pList)) {
            // 小学及以上
            GenderSexLowVision.Primary primary = new GenderSexLowVision.Primary();
            primary.setAvgVision(commonReportService.getAvgVision(pList));
            primary.setLowVisionProportion(countAndProportionService.lowVision(pList, total).getProportion());
            primary.setMAvgVision(countAndProportionService.lowVision(commonReportService.getMList(pList), total).getProportion());
            primary.setFAvgVision(countAndProportionService.lowVision(commonReportService.getFList(pList), total).getProportion());
            info.setPrimary(primary);
            genderSexLowVision.setInfo(info);
            genderSexLowVision.setPrimaryGenderPieChart(pieChartService.areaGenderPieChart(pList, total));
        }
        genderSexLowVision.setTables(screeningReportTableService.genderSexLowVisionTable(statConclusions, total));
        return genderSexLowVision;
    }

    /**
     * 不同学龄段不同程度视力情况
     *
     * @return SchoolAgeLowVision
     */
    private SchoolAgeLowVision generateSchoolAgeLowVision(List<StatConclusion> statConclusions, Long total) {
        SchoolAgeLowVision schoolAgeLowVision = new SchoolAgeLowVision();
        SchoolAgeLowVision.Info info = new SchoolAgeLowVision.Info();

        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        if (!CollectionUtils.isEmpty(kList)) {
            info.setKindergartenLowVisionProportion(countAndProportionService.lowVision(kList, total).getProportion());
        }
        if (!CollectionUtils.isEmpty(pList)) {
            info.setPrimaryLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.PRIMARY.code, total).getProportion());
            info.setJuniorLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.JUNIOR.code, total).getProportion());
            info.setHighLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.HIGH.code, total).getProportion());
            info.setSeniorLowVisionProportion(countAndProportionService.seniorAgeLowVision(pList, total).getProportion());
            info.setVocationalLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.VOCATIONAL_HIGH.code, total).getProportion());
        }

        List<CommonLowVisionTable> tables = screeningReportTableService.gradeSchoolAgeLowVisionTable(statConclusions, total);
        schoolAgeLowVision.setTables(Lists.newArrayList(tables));
        List<CommonLowVisionTable> tableChart = tables.stream().filter(s -> commonReportService.schoolAgeList().contains(s.getName())).collect(Collectors.toList());
        List<CommonLowVisionTable> filterTable = tables.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList());
        schoolAgeLowVision.setLowVisionChart(horizontalChartService.areaLowVision(tableChart, true));
        if (commonReportService.isShowInfo(filterTable, false)) {
            schoolAgeLowVision.setLowVisionLevelChart(horizontalChartService.lowVisionChart(tableChart, false));
            info.setLight(highLowProportionService.schoolAgeLowVisionTableHP(filterTable, s -> Float.valueOf(s.getLightLowVisionProportion())));
            info.setMiddle(highLowProportionService.schoolAgeLowVisionTableHP(filterTable, s -> Float.valueOf(s.getMiddleLowVisionProportion())));
            info.setHigh(highLowProportionService.schoolAgeLowVisionTableHP(filterTable, s -> Float.valueOf(s.getHighLowVisionProportion())));
        }
        schoolAgeLowVision.setInfo(info);
        return schoolAgeLowVision;
    }

    /**
     * 不同年龄不同程度视力情况
     *
     * @return AgeLowVision
     */
    private AgeLowVision generateAgeLowVision(List<StatConclusion> statConclusions, Long total) {
        AgeLowVision ageLowVision = new AgeLowVision();
        ageLowVision.setAgeRange(commonReportService.getAgeRange(statConclusions));

        List<CommonLowVisionTable> tables = screeningReportTableService.schoolAgeLowVisionTables(statConclusions, total);
        ageLowVision.setTables(Lists.newArrayList(tables));
        if (commonReportService.isShowInfo(tables, true)) {
            List<CommonLowVisionTable> tableList = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
            ageLowVision.setAgeLowVisionChart(horizontalChartService.areaLowVision(tableList, true));
            ageLowVision.setAgeLowVisionLevelChart(horizontalChartService.lowVisionChart(tableList, true));
            AgeLowVision.Info info = new AgeLowVision.Info();
            info.setLow(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getLowVisionProportion())));
            info.setLight(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getLightLowVisionProportion())));
            info.setMiddle(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getMiddleLowVisionProportion())));
            info.setHigh(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getHighLowVisionProportion())));
            ageLowVision.setInfo(info);
        }
        return ageLowVision;
    }

    /**
     * 屈光整体情况
     *
     * @return AreaRefraction
     */
    private AreaRefraction generateAreaRefraction(List<StatConclusion> statConclusions, Integer noticeId, Integer districtId) {
        AreaRefraction areaRefraction = new AreaRefraction();

        AreaRefraction.Info info = new AreaRefraction.Info();
        info.setCount(statConclusions.size());

        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        long kCount = kList.size();
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);
        long pCount = pList.size();

        if (!CollectionUtils.isEmpty(kList)) {
            AreaRefraction.Kindergarten kindergarten = new AreaRefraction.Kindergarten();
            kindergarten.setCount(kList.size());
            kindergarten.setInsufficientFarsightednessReserve(countAndProportionService.insufficient(kList, kCount));
            kindergarten.setRefractiveError(countAndProportionService.refractiveError(kList, kCount));
            kindergarten.setAnisometropia(countAndProportionService.anisometropia(kList, kCount));
            info.setKindergarten(kindergarten);
            areaRefraction.setKindergartenInfo(generateKindergartenRefraction(kList, noticeId, commonReportService.getHistoryData(districtId, true)));
        }

        if (!CollectionUtils.isEmpty(pList)) {
            AreaRefraction.Primary primary = new AreaRefraction.Primary();
            primary.setCount(pList.size());
            primary.setMyopia(countAndProportionService.myopia(pList, pCount));
            primary.setEarly(countAndProportionService.earlyMyopia(pList, pCount));
            primary.setLowMyopia(countAndProportionService.lightMyopia(pList, pCount));
            primary.setHighMyopia(countAndProportionService.highMyopia(pList, pCount));
            primary.setNightWearing(countAndProportionService.nightWearing(pList, pCount));
            primary.setAstigmatism(countAndProportionService.astigmatism(pList, pCount));
            primary.setAllGlasses(countAndProportionService.allGlassesType(pList, pCount));
            primary.setRecommendDoctor(countAndProportionService.getRecommendDoctor(pList, pCount));
            primary.setFootOrthosis(countAndProportionService.enough(pList, pCount));
            primary.setUncorrected(countAndProportionService.uncorrected(pList, pCount));
            primary.setUndercorrection(countAndProportionService.under(pList, pCount));
            info.setPrimary(primary);
            areaRefraction.setPrimaryInfo(generatePrimaryRefraction(pList, pCount, noticeId, commonReportService.getHistoryData(districtId, false)));
        }
        areaRefraction.setInfo(info);
        return areaRefraction;
    }

    /**
     * 幼儿园屈光
     */
    private KindergartenInfo generateKindergartenRefraction(List<StatConclusion> statConclusions, Integer noticeId, List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples) {
        KindergartenInfo kindergartenInfo = new KindergartenInfo();
        if (CollectionUtils.isEmpty(statConclusions) || statConclusions.size() == 0) {
            return null;
        }
        long total = statConclusions.size();

        kindergartenInfo.setSexRefractiveInfo(commonReportService.kSexRefractive(statConclusions, total));
        SchoolAgeRefractive schoolAgeRefractive = new SchoolAgeRefractive();

        List<RefractiveTable> ageRefractiveTable = screeningReportTableService.schoolAgeRefractiveTable(statConclusions, total);
        schoolAgeRefractive.setTables(Lists.newArrayList(ageRefractiveTable));
        if (!CollectionUtils.isEmpty(ageRefractiveTable)) {
            SchoolAgeRefractive.Info info = new SchoolAgeRefractive.Info();

            // TODO： 抽出来
            if (!CollectionUtils.isEmpty(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()))) {
                SchoolAgeRefractive.Detail detail = new SchoolAgeRefractive.Detail();
                detail.setInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                info.setOne(detail);
            }
            if (!CollectionUtils.isEmpty(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()))) {
                SchoolAgeRefractive.Detail detail = new SchoolAgeRefractive.Detail();
                detail.setInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                info.setTwo(detail);
            }
            if (!CollectionUtils.isEmpty(statConclusions.stream().filter(s -> StringUtils.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()))) {
                SchoolAgeRefractive.Detail detail = new SchoolAgeRefractive.Detail();
                detail.setInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                detail.setAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList()), total).getProportion());
                info.setThree(detail);
            }
            schoolAgeRefractive.setInfo(info);
            schoolAgeRefractive.setAgeRefractiveChart(horizontalChartService.refractiveChart(ageRefractiveTable.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
        }
        kindergartenInfo.setSchoolAgeRefractiveInfo(schoolAgeRefractive);


        AgeRefractive ageRefractive = new AgeRefractive();
        ageRefractive.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<RefractiveTable> tables = screeningReportTableService.ageRefractiveTable(statConclusions, total);
        ageRefractive.setTables(Lists.newArrayList(tables));
        if (commonReportService.isShowInfo(tables, true)) {
            List<RefractiveTable> filterTables = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
            AgeRefractive.Info info = new AgeRefractive.Info();
            info.setAgeRefractiveChart(horizontalChartService.refractiveChart(filterTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
            info.setInsufficientInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getInsufficientProportion())));
            info.setRefractiveErrorInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getRefractiveErrorProportion())));
            info.setAnisometropiaInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getAnisometropiaProportion())));
            info.setRecommendDoctorInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
            ageRefractive.setInfo(info);
        }
        kindergartenInfo.setAgeRefractiveInfo(ageRefractive);
        kindergartenInfo.setHistoryRefractiveInfo(commonReportService.getAreaKindergartenHistoryRefractive(tuples, noticeId));
        return kindergartenInfo;
    }

    /**
     * 小学及以上屈光
     */
    private PrimaryInfo generatePrimaryRefraction(List<StatConclusion> statConclusions, Long total, Integer noticeId, List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples) {
        PrimaryInfo primaryInfo = new PrimaryInfo();
        if (CollectionUtils.isEmpty(statConclusions) || statConclusions.size() == 0) {
            return null;
        }
        primaryInfo.setGenderRefraction(getPrimaryGenderRefraction(statConclusions, total));
        primaryInfo.setSchoolAgeRefraction(getPrimarySchoolAgeRefraction(statConclusions, total));
        primaryInfo.setAgeRefraction(getPrimaryAgeRefraction(statConclusions, total));
        primaryInfo.setGenderWearingGlasses(screeningPrimaryReportService.getGenderWearingGlasses(statConclusions, total));
        primaryInfo.setSchoolAgeWearingGlasses(getPrimarySchoolAgeWearingGlasses(statConclusions, total));
        primaryInfo.setAgeWearingGlasses(getPrimaryAgeWearingGlasses(statConclusions, total));
        primaryInfo.setHistoryRefraction(getHistoryRefraction(tuples, noticeId));
        return primaryInfo;
    }

    private AgeWearingGlasses getPrimaryAgeWearingGlasses(List<StatConclusion> statConclusions, Long total) {
        AgeWearingGlasses ageWearingGlasses = new AgeWearingGlasses();
        ageWearingGlasses.setAgeInfo(commonReportService.getAgeRange(statConclusions));
        List<AgeWearingTable> ageWearingTables = screeningReportTableService.agePrimaryWearingTable(statConclusions, total);
        ageWearingGlasses.setTables(Lists.newArrayList(ageWearingTables));
        if (commonReportService.isShowInfo(ageWearingTables, true)) {
            List<AgeWearingTable> filterAgeWearingTables = ageWearingTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
            ageWearingGlasses.setAgeWearingGlassesChart(horizontalChartService.primaryWearingGlassesChart(filterAgeWearingTables, true));
            ageWearingGlasses.setAgeVisionCorrectionChart(horizontalChartService.primaryGenderVisionCorrectionChart(filterAgeWearingTables));
            AgeWearingGlasses.Info info = new AgeWearingGlasses.Info();
            info.setNotWearing(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getNotWearingProportion())));
            info.setGlasses(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getGlassesProportion())));
            info.setContact(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getWearingContactProportion())));
            info.setNight(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getNightWearingProportion())));
            info.setEnough(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getEnoughProportion())));
            info.setUncorrected(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getUncorrectedProportion())));
            info.setUnder(highLowProportionService.ageWearingTableHP("", filterAgeWearingTables, s -> Float.valueOf(s.getUnderProportion())));
            ageWearingGlasses.setInfo(info);
        }
        return ageWearingGlasses;
    }

    private SchoolAgeWearingGlasses getPrimarySchoolAgeWearingGlasses(List<StatConclusion> statConclusions, Long total) {
        SchoolAgeWearingGlasses schoolAgeWearingGlasses = new SchoolAgeWearingGlasses();
        List<AgeWearingTable> ageWearingTableList = screeningReportTableService.gradeWearingTable(statConclusions, total);
        schoolAgeWearingGlasses.setTables(Lists.newArrayList(ageWearingTableList));
        if (commonReportService.isShowInfo(ageWearingTableList.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList()), false)) {
            schoolAgeWearingGlasses.setAgeWearingGlassesChart(horizontalChartService.primaryWearingGlassesChart(ageWearingTableList.stream().filter(s -> commonReportService.schoolAgeList().contains(s.getName())).collect(Collectors.toList()), false));
            schoolAgeWearingGlasses.setAgeVisionCorrectionChart(horizontalChartService.primaryGenderVisionCorrectionChart(ageWearingTableList.stream().filter(s -> commonReportService.schoolAgeList().contains(s.getName())).collect(Collectors.toList())));
            schoolAgeWearingGlasses.setInfo(screeningPrimaryReportService.primaryWearingInfo(statConclusions, ageWearingTableList, total));
        }
        return schoolAgeWearingGlasses;
    }

    private AgeRefraction getPrimaryAgeRefraction(List<StatConclusion> statConclusions, Long total) {
        AgeRefraction ageRefraction = new AgeRefraction();
        List<AstigmatismTable> primaryAgeTable = screeningReportTableService.ageAstigmatismTables(statConclusions, total);
        ageRefraction.setTables(Lists.newArrayList(primaryAgeTable));
        ageRefraction.setAgeRange(commonReportService.getAgeRange(statConclusions));
        if (commonReportService.isShowInfo(primaryAgeTable, true)) {
            List<AstigmatismTable> filterPrimaryAgeTable = primaryAgeTable.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
            ageRefraction.setAgeRefractionChart(horizontalChartService.astigmatismMyopiaChart(filterPrimaryAgeTable));
            ageRefraction.setLevelAgeRefractionChart(horizontalChartService.astigmatismMyopiaLevelChart(filterPrimaryAgeTable));
            AgeRefraction.Info info = new AgeRefraction.Info();
            info.setEarlyMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
            info.setMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getMyopiaProportion())));
            info.setAstigmatism(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getAstigmatismProportion())));
            info.setLightMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getLightMyopiaProportion())));
            info.setHighMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getHighMyopiaProportion())));
            ageRefraction.setInfo(info);
        }
        return ageRefraction;
    }

    private SchoolAgeRefraction getPrimarySchoolAgeRefraction(List<StatConclusion> statConclusions, Long total) {
        SchoolAgeRefraction schoolAgeRefraction = new SchoolAgeRefraction();
        List<AstigmatismTable> tables = screeningReportTableService.schoolPrimaryRefractiveTable(statConclusions, total);
        schoolAgeRefraction.setTables(Lists.newArrayList(tables));
        List<AstigmatismTable> filterTables = tables.stream().filter(s -> !commonReportService.filterList().contains(s.getName())).collect(Collectors.toList());
        if (commonReportService.isShowInfo(filterTables, false)) {
            schoolAgeRefraction.setSchoolAgeRefractionChart(portraitChartService.refractionChart(tables.stream().filter(s -> commonReportService.schoolAgeList().contains(s.getName())).collect(Collectors.toList())));

            SchoolAgeRefraction.Info info = new SchoolAgeRefraction.Info();
            info.setMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getMyopiaProportion())));
            info.setAstigmatism(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getAstigmatismProportion())));
            info.setEarlyMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
            info.setLightMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getLightMyopiaProportion())));
            info.setHighMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getHighMyopiaProportion())));
            schoolAgeRefraction.setInfo(info);
        }
        return schoolAgeRefraction;
    }

    private GenderRefraction getPrimaryGenderRefraction(List<StatConclusion> statConclusions, Long total) {
        GenderRefraction genderRefraction = new GenderRefraction();
        genderRefraction.setMyopia(commonReportService.myopiaRefractive(statConclusions, total));
        genderRefraction.setAstigmatism(commonReportService.astigmatismRefractive(statConclusions, total));
        genderRefraction.setEarlyMyopia(commonReportService.earlyMyopiaRefractive(statConclusions, total));
        genderRefraction.setLightMyopia(commonReportService.lightMyopiaRefractive(statConclusions, total));
        genderRefraction.setHighMyopia(commonReportService.highMyopiaRefractive(statConclusions, total));
        List<AstigmatismTable> genderPrimaryRefractiveTable = screeningReportTableService.genderPrimaryRefractiveTable(statConclusions, total);
        genderRefraction.setTables(Lists.newArrayList(genderPrimaryRefractiveTable));
        genderRefraction.setGenderRefractionChart(horizontalChartService.schoolAgeAstigmatismChart(genderPrimaryRefractiveTable.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList())));
        return genderRefraction;
    }

    private HistoryRefraction getHistoryRefraction(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples, Integer noticeId) {
        HistoryRefraction historyRefraction = new HistoryRefraction();
        HistoryRefraction.Info info = new HistoryRefraction.Info();
        List<AstigmatismTable> pHistoryAstigmatismTable = screeningReportTableService.pHistoryAstigmatismTable(tuples, noticeId);
        historyRefraction.setTables(Lists.newArrayList(pHistoryAstigmatismTable));
        if (commonReportService.isShowInfo(pHistoryAstigmatismTable, false)) {
            info.setMyopiaProportion(commonReportService.getConvertRatio(pHistoryAstigmatismTable, AstigmatismTable::getMyopiaProportion));
            info.setEarlyMyopiaProportion(commonReportService.getConvertRatio(pHistoryAstigmatismTable, AstigmatismTable::getEarlyMyopiaProportion));
            info.setLightMyopiaProportion(commonReportService.getConvertRatio(pHistoryAstigmatismTable, AstigmatismTable::getLightMyopiaProportion));
            info.setHighMyopiaProportion(commonReportService.getConvertRatio(pHistoryAstigmatismTable, AstigmatismTable::getHighMyopiaProportion));
            historyRefraction.setInfo(info);
            historyRefraction.setPrimaryHistoryRefraction(horizontalChartService.primaryHistoryRefraction(pHistoryAstigmatismTable));
        }
        return historyRefraction;
    }


    /**
     * 各学校整体情况
     */
    private SchoolScreeningInfo schoolScreeningInfo(List<StatConclusion> statConclusions) {

        SchoolScreeningInfo schoolScreeningInfo = new SchoolScreeningInfo();
        long total = statConclusions.size();
        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        if (!CollectionUtils.isEmpty(kList)) {
            SchoolScreeningInfo.Kindergarten kindergarten = new SchoolScreeningInfo.Kindergarten();
            List<KindergartenScreeningInfoTable> kTables = screeningReportTableService.kindergartenScreeningInfoTables(kList, total);
            kindergarten.setTables(kTables);
            kindergarten.setLowVision(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getLowVisionProportion())));
            kindergarten.setInsufficient(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getInsufficientProportion())));
            kindergarten.setRefractiveError(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getRefractiveErrorProportion())));
            kindergarten.setAnisometropia(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getAnisometropiaProportion())));
            kindergarten.setRecommendDoctor(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
            schoolScreeningInfo.setKindergarten(kindergarten);
        }
        if (!CollectionUtils.isEmpty(pList)) {
            schoolScreeningInfo.setPrimary(commonReportService.getPrimaryOverall(screeningReportTableService.schoolScreeningInfoTables(pList), pList, (long) pList.size()));
        }
        return schoolScreeningInfo;
    }

}
