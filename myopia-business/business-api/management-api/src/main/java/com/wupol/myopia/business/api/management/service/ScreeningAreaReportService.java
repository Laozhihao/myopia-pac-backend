package com.wupol.myopia.business.api.management.service;

import com.google.common.collect.Lists;
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
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GenderWearingGlasses;
import com.wupol.myopia.business.api.management.service.report.ScreeningReportTableService;
import com.wupol.myopia.business.api.management.service.report.CommonReportService;
import com.wupol.myopia.business.api.management.service.report.CountAndProportionService;
import com.wupol.myopia.business.api.management.service.report.HighLowProportionService;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查报告-区域
 *
 * @author Simple4H
 */
@Service
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

    public ScreeningAreaReportDTO generateReport(Integer noticeId, Integer planId, Integer districtId) {
        Set<Integer> childDistrictIds;
        try {
            childDistrictIds = districtService.getChildDistrictIdsByDistrictId(districtId);
            childDistrictIds.add(districtId);
        } catch (IOException e) {
            throw new BusinessException("获取区域异常");
        }
        List<StatConclusion> statConclusions = statConclusionService.getByNoticePlanDistrict(noticeId, planId, childDistrictIds);
        if (CollectionUtils.isEmpty(statConclusions)) {
            return new ScreeningAreaReportDTO();
        }
        ScreeningAreaReportDTO reportDTO = new ScreeningAreaReportDTO();

        // 标题信息
        reportDTO.setInfo(generateAreaReportInfo(statConclusions, districtId));

        // 概述
        reportDTO.setAreaOutline(generateAreaOutline(statConclusions, districtId));

        // 视力总体情况
        reportDTO.setAreaGeneralVision(generateAreaGeneralVision(statConclusions));

        // 各学校整体情况
        reportDTO.setSchoolScreeningInfo(schoolScreeningInfo(statConclusions));

        return reportDTO;
    }

    /**
     * 生成概览信息
     *
     * @param statConclusions 结论
     * @param districtId      区域
     */
    private AreaReportInfo generateAreaReportInfo(List<StatConclusion> statConclusions, Integer districtId) {
        AreaReportInfo info = new AreaReportInfo();
        info.setArea(districtService.getDistrictNameByDistrictId(districtId));

        // 设置时间
        List<Date> dateList = statConclusions.stream().map(StatConclusion::getCreateTime).collect(Collectors.toList());
        Integer maxYear = DateUtil.getYear(Collections.max(dateList));
        Integer minYear = DateUtil.getYear(Collections.min(dateList));
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
        areaOutline.setInfo(areaOutlineInfo(districtId, statConclusions));
        areaOutline.setTables(screeningReportTableService.areaOutlineTable(statConclusions));

        List<String> kindergartenGradeCode = GradeCodeEnum.kindergartenSchoolCode();
        areaOutline.setKindergarten(areaOutlineKindergarten(statConclusions.stream().filter(StatConclusion::getIsValid).filter(grade -> kindergartenGradeCode.contains(grade.getSchoolGradeCode())).collect(Collectors.toList())));

        areaOutline.setPrimary(areaOutlinePrimary(statConclusions.stream().filter(StatConclusion::getIsValid).filter(grade -> !kindergartenGradeCode.contains(grade.getSchoolGradeCode())).collect(Collectors.toList())));

        areaOutline.setVisionWarningSituation(commonReportService.getVisionWarningSituation(statConclusions));

        areaOutline.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions));
        return areaOutline;
    }

    /**
     * 概述
     */
    private AreaOutline.Info areaOutlineInfo(Integer districtId, List<StatConclusion> statConclusions) {
        AreaOutline.Info info = new AreaOutline.Info();
        info.setArea(districtService.getById(districtId).getName());

        List<Integer> schoolAges = statConclusions.stream().map(StatConclusion::getSchoolAge).distinct().collect(Collectors.toList());
        info.setSchoolAge(String.join("、", SchoolAge.batchNameByCode(schoolAges)));

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
    private Primary areaOutlinePrimary(List<StatConclusion> statConclusions) {
        Primary primary = new Primary();

        primary.setVisionSituation(commonReportService.getVisionSituation(statConclusions));

        // 近视数
        primary.setMyopia(countAndProportionService.myopia(statConclusions));

        // 近视详情(小学阶段、初中阶段、高中阶段)
        primary.setPrimaryProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.privateSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList())));
        primary.setJuniorProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.juniorSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList())));
        primary.setHighProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.highSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList())));
        primary.setVocationalHighProportion(countAndProportionService.myopia(statConclusions.stream().filter(s -> GradeCodeEnum.vocationalHighSchoolCodes().contains(s.getSchoolGradeCode())).collect(Collectors.toList())));

        // 近视前期数
        primary.setEarly(countAndProportionService.earlyMyopia(statConclusions));

        // 低度近视数
        primary.setLowMyopia(countAndProportionService.lightMyopia(statConclusions));

        // 高度近视数
        primary.setHighMyopia(countAndProportionService.highMyopia(statConclusions));

        // 近视足矫数
        primary.setFootOrthosis(countAndProportionService.enough(statConclusions));

        // 近视未矫数
        primary.setUncorrected(countAndProportionService.uncorrected(statConclusions));

        // 近视欠矫数
        primary.setUndercorrection(countAndProportionService.under(statConclusions));
        return primary;

    }

    /**
     * 视力总体情况
     *
     * @return AreaGeneralVision
     */
    private AreaGeneralVision generateAreaGeneralVision(List<StatConclusion> statConclusions) {
        AreaGeneralVision areaGeneralVision = new AreaGeneralVision();
        List<StatConclusion> validList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        areaGeneralVision.setAreaLowVision(generateAreaLowVision(validList));
        areaGeneralVision.setAreaRefraction(generateAreaRefraction(validList));
        areaGeneralVision.setWarningSituation(commonReportService.getWarningSituation(statConclusions));
        return areaGeneralVision;
    }

    /**
     * 视力低下
     *
     * @return AreaLowVision
     */
    private AreaLowVision generateAreaLowVision(List<StatConclusion> statConclusions) {
        AreaLowVision areaLowVision = new AreaLowVision();
        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        AreaLowVision.Info info = new AreaLowVision.Info();

        // 幼儿园
        AreaLowVision.Kindergarten kindergarten = new AreaLowVision.Kindergarten();

        kindergarten.setVisionSituation(commonReportService.getVisionSituation(kList));
        info.setKindergarten(kindergarten);

        // 小学及以上
        PrimaryLowVisionInfo primary = new PrimaryLowVisionInfo();
        primary.setVisionSituation(commonReportService.getVisionSituation(pList));
        primary.setLightLowVision(countAndProportionService.lightLowVision(pList));
        primary.setMiddleLowVision(countAndProportionService.middleLowVision(pList));
        primary.setHighLowVision(countAndProportionService.highLowVision(pList));
        info.setPrimary(primary);
        areaLowVision.setInfo(info);

        areaLowVision.setGenderSexLowVision(generateGenderSexLowVision(statConclusions));
        areaLowVision.setSchoolAgeLowVision(generateSchoolAgeLowVision(statConclusions));
        areaLowVision.setAgeLowVision(generateAgeLowVision(statConclusions));
        LowVisionHistory lowVisionHistory = new LowVisionHistory();
        List<SchoolHistoryLowVisionTable> tables = screeningReportTableService.historySchoolAgeLowVisionTables(statConclusions);
        lowVisionHistory.setKProportion(commonReportService.getChainRatioProportion(tables.stream().map(SchoolHistoryLowVisionTable::getKLowVisionProportion).collect(Collectors.toList())));
        lowVisionHistory.setPProportion(commonReportService.getChainRatioProportion(tables.stream().map(SchoolHistoryLowVisionTable::getLowVisionProportion).collect(Collectors.toList())));
        lowVisionHistory.setTables(Lists.newArrayList(tables));

        areaLowVision.setLowVisionHistory(lowVisionHistory);
        return areaLowVision;
    }

    /**
     * 不同性别视力低下
     *
     * @param statConclusions 结果
     *
     * @return GenderSexLowVision
     */
    private GenderSexLowVision generateGenderSexLowVision(List<StatConclusion> statConclusions) {
        GenderSexLowVision genderSexLowVision = new GenderSexLowVision();
        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        // 幼儿园
        GenderSexLowVision.Info info = new GenderSexLowVision.Info();
        GenderSexLowVision.Kindergarten kindergarten = new GenderSexLowVision.Kindergarten();
        kindergarten.setAvgVision(commonReportService.getAvgVision(kList));
        kindergarten.setLowVisionProportion(countAndProportionService.lowVision(kList).getProportion());
        kindergarten.setMAvgVision(countAndProportionService.lowVision(commonReportService.getMList(kList)).getProportion());
        kindergarten.setFAvgVision(countAndProportionService.lowVision(commonReportService.getFList(kList)).getProportion());
        info.setKindergarten(kindergarten);

        // 小学及以上
        GenderSexLowVision.Primary primary = new GenderSexLowVision.Primary();
        primary.setAvgVision(commonReportService.getAvgVision(pList));
        primary.setLowVisionProportion(countAndProportionService.lowVision(pList).getProportion());
        primary.setMAvgVision(countAndProportionService.lowVision(commonReportService.getMList(pList)).getProportion());
        primary.setFAvgVision(countAndProportionService.lowVision(commonReportService.getFList(pList)).getProportion());
        info.setPrimary(primary);
        genderSexLowVision.setInfo(info);
        genderSexLowVision.setTables(screeningReportTableService.genderSexLowVisionTable(statConclusions));
        return genderSexLowVision;
    }

    /**
     * 不同学龄段不同程度视力情况
     *
     * @return SchoolAgeLowVision
     */
    private SchoolAgeLowVision generateSchoolAgeLowVision(List<StatConclusion> statConclusions) {
        SchoolAgeLowVision schoolAgeLowVision = new SchoolAgeLowVision();
        SchoolAgeLowVision.Info info = new SchoolAgeLowVision.Info();

        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        info.setKindergartenLowVisionProportion(countAndProportionService.lowVision(kList).getProportion());
        info.setPrimaryLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.PRIMARY.code).getProportion());
        info.setJuniorLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.JUNIOR.code).getProportion());
        info.setHighLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.HIGH.code).getProportion());
        info.setVocationalLowVisionProportion(countAndProportionService.schoolAgeLowVision(pList, SchoolAge.VOCATIONAL_HIGH.code).getProportion());

        info.setLight(highLowProportionService.levelSchoolAgeLowVision(statConclusions, LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT.code));
        info.setMiddle(highLowProportionService.levelSchoolAgeLowVision(statConclusions, LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE.code));
        info.setHigh(highLowProportionService.levelSchoolAgeLowVision(statConclusions, LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH.code));
        schoolAgeLowVision.setInfo(info);

        schoolAgeLowVision.setTables(screeningReportTableService.gradeSchoolAgeLowVisionTable(statConclusions));
        return schoolAgeLowVision;
    }

    /**
     * 不同年龄不同程度视力情况
     *
     * @return AgeLowVision
     */
    private AgeLowVision generateAgeLowVision(List<StatConclusion> statConclusions) {
        AgeLowVision ageLowVision = new AgeLowVision();
        ageLowVision.setAgeRange(commonReportService.getAgeRange(statConclusions));

        List<SchoolAgeLowVisionTable> tables = screeningReportTableService.schoolAgeLowVisionTables(statConclusions);
        ageLowVision.setTables(Lists.newArrayList(tables));
        List<SchoolAgeLowVisionTable> tableList = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        ageLowVision.setLow(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getLowVisionProportion())));
        ageLowVision.setLight(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getLightLowVisionProportion())));
        ageLowVision.setMiddle(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getMiddleLowVisionProportion())));
        ageLowVision.setHigh(highLowProportionService.ageLowVisionTableHP(tableList, s -> Float.valueOf(s.getHighLowVisionProportion())));
        return ageLowVision;
    }

    /**
     * 屈光整体情况
     *
     * @return AreaRefraction
     */
    private AreaRefraction generateAreaRefraction(List<StatConclusion> statConclusions) {
        AreaRefraction areaRefraction = new AreaRefraction();

        AreaRefraction.Info info = new AreaRefraction.Info();
        info.setCount(statConclusions.size());

        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        AreaRefraction.Kindergarten kindergarten = new AreaRefraction.Kindergarten();
        kindergarten.setCount(kList.size());
        kindergarten.setInsufficientFarsightednessReserve(countAndProportionService.insufficient(kList));
        kindergarten.setRefractiveError(countAndProportionService.refractiveError(kList));
        kindergarten.setAnisometropia(countAndProportionService.anisometropia(kList));
        info.setKindergarten(kindergarten);

        AreaRefraction.Primary primary = new AreaRefraction.Primary();
        primary.setCount(pList.size());
        primary.setMyopia(countAndProportionService.myopia(pList));
        primary.setEarly(countAndProportionService.earlyMyopia(pList));
        primary.setLowMyopia(countAndProportionService.lightMyopia(pList));
        primary.setHighMyopia(countAndProportionService.highMyopia(pList));
        primary.setNightWearing(countAndProportionService.nightWearing(pList));
        primary.setAstigmatism(countAndProportionService.anisometropia(pList));
        primary.setAllGlasses(countAndProportionService.allGlassesType(pList));
        primary.setRecommendDoctor(countAndProportionService.getRecommendDoctor(pList));
        primary.setFootOrthosis(countAndProportionService.enough(pList));
        primary.setUncorrected(countAndProportionService.uncorrected(pList));
        primary.setUndercorrection(countAndProportionService.under(pList));
        info.setPrimary(primary);

        areaRefraction.setInfo(info);

        areaRefraction.setKindergartenInfo(generateKindergartenRefraction(kList));
        areaRefraction.setPrimaryInfo(generatePrimaryRefraction(pList));
        return areaRefraction;
    }

    /**
     * 幼儿园屈光
     */
    private KindergartenInfo generateKindergartenRefraction(List<StatConclusion> statConclusions) {
        KindergartenInfo kindergartenInfo = new KindergartenInfo();

        kindergartenInfo.setSexRefractiveInfo(commonReportService.kSexRefractive(statConclusions));

        SchoolAgeRefractive schoolAgeRefractive = new SchoolAgeRefractive();
        SchoolAgeRefractive.Info info = new SchoolAgeRefractive.Info();
        info.setOneInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setTwoInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setThreeInsufficient(countAndProportionService.insufficient(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setOneRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setTwoRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setThreeRefractiveError(countAndProportionService.refractiveError(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setOneAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setTwoAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.TWO_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        info.setThreeAnisometropia(countAndProportionService.anisometropia(statConclusions.stream().filter(s -> Objects.equals(s.getSchoolGradeCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode())).collect(Collectors.toList())).getProportion());
        schoolAgeRefractive.setInfo(info);
        schoolAgeRefractive.setTables(screeningReportTableService.schoolAgeRefractiveTable(statConclusions));
        kindergartenInfo.setSchoolAgeRefractiveInfo(schoolAgeRefractive);

        AgeRefractive ageRefractive = new AgeRefractive();
        ageRefractive.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<RefractiveTable> tables = screeningReportTableService.ageRefractiveTable(statConclusions);
        ageRefractive.setTables(Lists.newArrayList(tables));
        List<RefractiveTable> filterTables = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        ageRefractive.setInsufficientInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getInsufficientProportion())));
        ageRefractive.setRefractiveErrorInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getInsufficientProportion())));
        ageRefractive.setAnisometropiaInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getInsufficientProportion())));
        ageRefractive.setRecommendDoctorInfo(highLowProportionService.ageRefractiveTableHP(filterTables, s -> Float.valueOf(s.getInsufficientProportion())));
        kindergartenInfo.setAgeRefractiveInfo(ageRefractive);

        kindergartenInfo.setHistoryRefractiveInfo(commonReportService.getHistoryRefractive(statConclusions));
        return kindergartenInfo;
    }

    /**
     * 小学及以上屈光
     */
    private PrimaryInfo generatePrimaryRefraction(List<StatConclusion> statConclusions) {
        PrimaryInfo primaryInfo = new PrimaryInfo();

        GenderRefraction genderRefraction = new GenderRefraction();
        genderRefraction.setMyopia(commonReportService.myopiaRefractive(statConclusions));
        genderRefraction.setAstigmatism(commonReportService.astigmatismRefractive(statConclusions));
        genderRefraction.setEarlyMyopia(commonReportService.earlyMyopiaRefractive(statConclusions));
        genderRefraction.setLightMyopia(commonReportService.lightMyopiaRefractive(statConclusions));
        genderRefraction.setHighMyopia(commonReportService.highMyopiaRefractive(statConclusions));
        genderRefraction.setTables(screeningReportTableService.genderPrimaryRefractiveTable(statConclusions));
        primaryInfo.setGenderRefraction(genderRefraction);

        SchoolAgeRefraction schoolAgeRefraction = new SchoolAgeRefraction();
        List<AstigmatismTable> tables = screeningReportTableService.schoolPrimaryRefractiveTable(statConclusions);
        schoolAgeRefraction.setTables(Lists.newArrayList(tables));
        List<AstigmatismTable> filterTables = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME))
                .filter(s -> !SchoolAge.getAllDesc().contains(s.getName())).collect(Collectors.toList());
        schoolAgeRefraction.setMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getMyopiaProportion())));
        schoolAgeRefraction.setAstigmatism(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getAstigmatismProportion())));
        schoolAgeRefraction.setEarlyMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
        schoolAgeRefraction.setLightMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getLightMyopiaProportion())));
        schoolAgeRefraction.setHighMyopia(highLowProportionService.ageAstigmatismTableHP(filterTables, s -> Float.valueOf(s.getHighMyopiaProportion())));
        primaryInfo.setSchoolAgeRefraction(schoolAgeRefraction);

        AgeRefraction ageRefraction = new AgeRefraction();
        List<AstigmatismTable> primaryAgeTable = screeningReportTableService.ageAstigmatismTables(statConclusions);
        ageRefraction.setTables(Lists.newArrayList(primaryAgeTable));
        ageRefraction.setAgeRange(commonReportService.getAgeRange(statConclusions));
        List<AstigmatismTable> filterPrimaryAgeTable = primaryAgeTable.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        ageRefraction.setEarlyMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
        ageRefraction.setMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getMyopiaProportion())));
        ageRefraction.setAstigmatism(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getAstigmatismProportion())));
        ageRefraction.setLightMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getLightMyopiaProportion())));
        ageRefraction.setHighMyopia(highLowProportionService.ageAstigmatismTableHP(filterPrimaryAgeTable, s -> Float.valueOf(s.getHighMyopiaProportion())));
        primaryInfo.setAgeRefraction(ageRefraction);

        GenderWearingGlasses genderWearingGlasses = new GenderWearingGlasses();
        genderWearingGlasses.setInfo(screeningPrimaryReportService.generateGenderWearingGlasses(statConclusions, commonReportService));
        genderWearingGlasses.setTables(screeningReportTableService.genderWearingTable(statConclusions));
        primaryInfo.setGenderWearingGlasses(genderWearingGlasses);

        SchoolAgeWearingGlasses schoolAgeWearingGlasses = new SchoolAgeWearingGlasses();
        List<AgeWearingTable> t1 = screeningReportTableService.gradeWearingTable(statConclusions);
        schoolAgeWearingGlasses.setTables(Lists.newArrayList(t1));
        schoolAgeWearingGlasses.setPrimaryWearingInfo(screeningPrimaryReportService.primaryWearingInfo(t1));
        primaryInfo.setSchoolAgeWearingGlasses(schoolAgeWearingGlasses);

        AgeWearingGlasses ageWearingGlasses = new AgeWearingGlasses();
        ageWearingGlasses.setAgeInfo(commonReportService.getAgeRange(statConclusions));
        List<AgeWearingTable> ageWearingTables = screeningReportTableService.agePrimaryWearingTable(statConclusions);
        ageWearingGlasses.setTables(Lists.newArrayList(ageWearingTables));
        List<AgeWearingTable> filterAgeWearingTables = ageWearingTables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).collect(Collectors.toList());
        ageWearingGlasses.setNotWearing(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getNotWearingProportion())));
        ageWearingGlasses.setGlasses(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getGlassesProportion())));
        ageWearingGlasses.setWearingContact(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getWearingContactProportion())));
        ageWearingGlasses.setNightWearing(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getNightWearingProportion())));
        ageWearingGlasses.setEnough(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getEnoughProportion())));
        ageWearingGlasses.setUncorrected(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getUncorrectedProportion())));
        ageWearingGlasses.setUnder(highLowProportionService.ageWearingTableHP(filterAgeWearingTables, s -> Float.valueOf(s.getUnderProportion())));
        primaryInfo.setAgeWearingGlasses(ageWearingGlasses);

        HistoryRefraction historyRefraction = new HistoryRefraction();
        HistoryRefraction.Info info = new HistoryRefraction.Info();
        List<AstigmatismTable> pHistoryAstigmatismTable = screeningReportTableService.pHistoryAstigmatismTable(statConclusions);
        historyRefraction.setTables(Lists.newArrayList(pHistoryAstigmatismTable));
        info.setMyopiaProportion(commonReportService.getChainRatioProportion(pHistoryAstigmatismTable.stream().map(AstigmatismTable::getMyopiaProportion).collect(Collectors.toList())));
        info.setEarlyMyopiaProportion(commonReportService.getChainRatioProportion(pHistoryAstigmatismTable.stream().map(AstigmatismTable::getEarlyMyopiaProportion).collect(Collectors.toList())));
        info.setLightMyopiaProportion(commonReportService.getChainRatioProportion(pHistoryAstigmatismTable.stream().map(AstigmatismTable::getLightMyopiaProportion).collect(Collectors.toList())));
        info.setHighMyopiaProportion(commonReportService.getChainRatioProportion(pHistoryAstigmatismTable.stream().map(AstigmatismTable::getHighMyopiaProportion).collect(Collectors.toList())));
        historyRefraction.setInfo(info);
        primaryInfo.setHistoryRefraction(historyRefraction);

        return primaryInfo;
    }


    /**
     * 各学校整体情况
     */
    private SchoolScreeningInfo schoolScreeningInfo(List<StatConclusion> statConclusions) {

        SchoolScreeningInfo schoolScreeningInfo = new SchoolScreeningInfo();

        List<StatConclusion> kList = commonReportService.getKList(statConclusions);
        List<StatConclusion> pList = commonReportService.getPList(statConclusions);

        SchoolScreeningInfo.Kindergarten kindergarten = new SchoolScreeningInfo.Kindergarten();
        List<KindergartenScreeningInfoTable> kTables = screeningReportTableService.kindergartenScreeningInfoTables(kList);
        kindergarten.setTables(kTables);
        kindergarten.setLowVision(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getLowVisionProportion())));
        kindergarten.setInsufficient(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getInsufficientProportion())));
        kindergarten.setRefractiveError(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getRefractiveErrorProportion())));
        kindergarten.setAnisometropia(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getAnisometropiaProportion())));
        kindergarten.setRecommendDoctor(highLowProportionService.kScreeningInfoTableHP(kTables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
        schoolScreeningInfo.setKindergarten(kindergarten);
        schoolScreeningInfo.setPrimary(commonReportService.getPrimaryOverall(screeningReportTableService.schoolScreeningInfoTables(statConclusions), pList));
        return schoolScreeningInfo;
    }

}
