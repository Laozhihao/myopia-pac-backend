package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.date.DateTime;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.*;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.ScreeningDataReportTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.HistoryRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningSituation;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.*;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.ClassScreeningData;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.RefractiveAbnormalities;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.SexRefractive;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报告公共
 *
 * @author Simple4H
 */
@Service
public class CommonReportService {

    @Resource
    private ScreeningReportTableService screeningReportTableService;

    @Resource
    private DistrictService districtService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private HighLowProportionService highLowProportionService;

    @Resource
    private CountAndProportionService countAndProportionService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private StudentService studentService;


    public static final String TOTAL_NAME = "合计";

    /**
     * 视力低下
     *
     * @param statConclusions 统计结果
     *
     * @return VisionSituation
     */
    public VisionSituation getVisionSituation(List<StatConclusion> statConclusions) {
        VisionSituation visionSituation = new VisionSituation();
        visionSituation.setCount(statConclusions.stream().filter(s -> Objects.nonNull(s.getIsLowVision())).filter(StatConclusion::getIsLowVision).count());
        visionSituation.setProportion(BigDecimalUtil.divide(statConclusions.stream().filter(s -> Objects.nonNull(s.getIsLowVision())).filter(StatConclusion::getIsLowVision).count(), statConclusions.stream().filter(StatConclusion::getIsValid).count()));
        visionSituation.setAvgVision(getAvgVision(statConclusions));
        return visionSituation;
    }

    /**
     * 获取平均视力
     *
     * @param statConclusions 统计结果
     *
     * @return 平均视力
     */
    public String getAvgVision(List<StatConclusion> statConclusions) {
        BigDecimal total = new BigDecimal(0);
        for (StatConclusion statConclusion : statConclusions) {
            total = total.add(Objects.nonNull(statConclusion.getVisionR()) ? statConclusion.getVisionR() : new BigDecimal("0")).add(Objects.nonNull(statConclusion.getVisionL()) ? statConclusion.getVisionL() : new BigDecimal("0"));
        }
        return BigDecimalUtil.divide(total, new BigDecimal(statConclusions.size()).multiply(new BigDecimal("2")), 2).toString();
    }

    /**
     * 视力监测预警
     *
     * @param statConclusions 统计结果
     *
     * @return VisionWarningSituation
     */
    public VisionWarningSituation getVisionWarningSituation(List<StatConclusion> statConclusions) {
        VisionWarningSituation warningSituation = new VisionWarningSituation();
        List<StatConclusion> statValidList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());

        long count = statValidList.size();

        long zeroWarningCount = statValidList.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO.code)).count();
        warningSituation.setZeroWarning(new CountAndProportion(zeroWarningCount, BigDecimalUtil.divide(zeroWarningCount, count)));

        long oneWarningCount = statValidList.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ONE.code)).count();
        warningSituation.setOneWarning(new CountAndProportion(oneWarningCount, BigDecimalUtil.divide(oneWarningCount, count)));

        long twoWarningCount = statValidList.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.TWO.code)).count();
        warningSituation.setTwoWarning(new CountAndProportion(twoWarningCount, BigDecimalUtil.divide(twoWarningCount, count)));

        long threeWarningCount = statValidList.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.THREE.code)).count();
        warningSituation.setThreeWarning(new CountAndProportion(threeWarningCount, BigDecimalUtil.divide(threeWarningCount, count)));
        return warningSituation;
    }

    /**
     * 获取幼儿园数据
     *
     * @param statConclusions 统计
     *
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getKList(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList());
    }

    /**
     * 获取中小学
     *
     * @param statConclusions 统计
     *
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getPList(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList());
    }

    /**
     * 获取男生数据
     *
     * @param statConclusions 统计
     *
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getMList(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).collect(Collectors.toList());
    }

    /**
     * 获取女生数据
     *
     * @param statConclusions 统计
     *
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getFList(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).collect(Collectors.toList());
    }

    /**
     * 获取有效人数
     *
     * @param statConclusions 统计
     *
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getValidList(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
    }

    /**
     * 近视-屈光
     */
    public GenderProportion myopiaRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.myopia(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.nonNull(s.getIsMyopia())).filter(StatConclusion::getIsMyopia).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 远视储备不足-屈光
     */
    public GenderProportion insufficientRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.insufficient(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 屈光不正-屈光
     */
    public GenderProportion refractiveErrorRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.refractiveError(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getIsRefractiveError(), Boolean.TRUE)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 屈光参差-屈光
     */
    public GenderProportion anisometropiaRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.anisometropia(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getIsAnisometropia(), Boolean.TRUE)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 建议就诊-屈光
     */
    public GenderProportion recommendDoctorRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.astigmatism(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getIsRecommendVisit(), Boolean.TRUE)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 近视前期-屈光
     */
    public GenderProportion earlyMyopiaRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.earlyMyopia(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getMyopiaWarningLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 散光-屈光
     */
    public GenderProportion astigmatismRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.getRecommendDoctor(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(StatConclusion::getIsAstigmatism).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 低度近视-屈光
     */
    public GenderProportion lightMyopiaRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.lightMyopia(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getMyopiaWarningLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 高度近视-屈光
     */
    public GenderProportion highMyopiaRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.highMyopia(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getMyopiaWarningLevel(), MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 不戴镜-屈光
     */
    public GenderProportion notWearingRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.notWearing(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 框架眼镜-屈光
     */
    public GenderProportion glassesRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.glasses(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.FRAME_GLASSES.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 隐形眼镜-屈光
     */
    public GenderProportion contactRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.contact(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.CONTACT_LENS.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 夜戴角膜塑形镜-屈光
     */
    public GenderProportion nightRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.night(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), GlassesTypeEnum.ORTHOKERATOLOGY.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 足矫-屈光
     */
    public GenderProportion enoughRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.enough(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.ENOUGH_CORRECTED.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 欠矫-屈光
     */
    public GenderProportion underRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.under(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNDER_CORRECTED.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    /**
     * 未矫-屈光
     */
    public GenderProportion uncorrectedRefractive(List<StatConclusion> statConclusions) {
        GenderProportion genderProportion = new GenderProportion();
        genderProportion.setProportion(countAndProportionService.uncorrected(statConclusions).getProportion());

        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), VisionCorrection.UNCORRECTED.code)).collect(Collectors.groupingBy(StatConclusion::getGender));

        getGenderPercentage(statConclusions, genderProportion, genderMap);
        return genderProportion;
    }

    private void getGenderPercentage(List<StatConclusion> statConclusions, GenderProportion genderProportion, Map<Integer, List<StatConclusion>> genderMap) {
        genderMap.forEach((k, v) -> {
            if (Objects.equals(GenderEnum.MALE.type, k)) {
                genderProportion.setMPercentage(BigDecimalUtil.divide((long) v.size(), (long) statConclusions.size()));
            }
            if (Objects.equals(GenderEnum.FEMALE.type, k)) {
                genderProportion.setFPercentage(BigDecimalUtil.divide((long) v.size(), (long) statConclusions.size()));
            }
        });
    }


    public List<ChainRatio> getHistoryDate(List<StatConclusion> statConclusions) {
        Set<Date> collect = statConclusions.stream().sorted(Comparator.comparing(StatConclusion::getCreateTime)).map(StatConclusion::getCreateTime).collect(Collectors.toSet());

        List<ChainRatio> chainRatioList = new ArrayList<>();

        for (Date s : collect) {
            Integer year = DateUtil.getYear(s);
            DateTime start = DateUtil.parseDate(year + "-01-01");
            DateTime mid = DateUtil.parseDate(year + "-07-31");
            DateTime end = DateUtil.parseDate(year + "-12-31");
            if (DateUtil.isBetweenDate(start, mid, s)) {
                chainRatioList.add(new ChainRatio(year + "年上半年", start, mid));
            }
            if (DateUtil.isBetweenDate(mid, end, s)) {
                chainRatioList.add(new ChainRatio(year + "年下半年", mid, end));
            }
        }
        return chainRatioList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ChainRatio::getStartDate))), ArrayList::new)).stream().sorted(Comparator.comparing(ChainRatio::getStartDate)).collect(Collectors.toList());
    }


    /**
     * 环比百分比
     */
    public String getChainRatioProportion(List<String> proportions) {
        if (CollectionUtils.isEmpty(proportions) || proportions.size() <= 1) {
            return StringUtils.EMPTY;
        }
        return new BigDecimal(proportions.get(proportions.size() - 1)).subtract(new BigDecimal(proportions.get(proportions.size() - 2))).setScale(2, RoundingMode.HALF_UP).toString();
    }

    public Integer getSchoolCount(List<StatConclusion> statConclusions) {
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        List<String> primaryAbove = GradeCodeEnum.primaryAbove();
        List<String> kindergarten = GradeCodeEnum.kindergartenSchoolCode();

        AtomicInteger atomicInteger = new AtomicInteger(0);
        collect.forEach((k, v) -> {
            List<String> gradeCode = v.stream().map(StatConclusion::getSchoolGradeCode).distinct().collect(Collectors.toList());
            if (!org.apache.commons.collections4.CollectionUtils.intersection(gradeCode, primaryAbove).isEmpty() && !org.apache.commons.collections4.CollectionUtils.intersection(gradeCode, kindergarten).isEmpty()) {
                atomicInteger.addAndGet(2);
            } else {
                atomicInteger.addAndGet(1);
            }
        });
        return atomicInteger.get();
    }

    /**
     * 获取年龄段
     */
    public String getAgeRange(List<StatConclusion> statConclusions) {
        Integer max = statConclusions.stream().map(StatConclusion::getAge).max(Comparator.comparing(Integer::valueOf)).orElse(null);
        Integer min = statConclusions.stream().map(StatConclusion::getAge).min(Comparator.comparing(Integer::valueOf)).orElse(null);
        return StrUtil.spliceChar("-", String.valueOf(min), String.valueOf(max));
    }

    public WarningSituation getWarningSituation(List<StatConclusion> statConclusions) {
        WarningSituation warningSituation = new WarningSituation();
        warningSituation.setVisionWarningSituation(getVisionWarningSituation(statConclusions));
        warningSituation.setRecommendDoctor(countAndProportionService.getRecommendDoctor(statConclusions));
        List<WarningTable> tables = screeningReportTableService.warningTable(statConclusions);

        List<WarningTable> t5Filter = tables.stream().filter(s -> !StringUtils.equals(s.getName(), CommonReportService.TOTAL_NAME)).filter(s -> !SchoolAge.getAllDesc().contains(s.getName())).collect(Collectors.toList());

        WarningSituation.GradeWarningInfo gradeWarningInfo = new WarningSituation.GradeWarningInfo();
        gradeWarningInfo.setTables(Lists.newArrayList(tables));
        gradeWarningInfo.setZero(highLowProportionService.warningTableHP(t5Filter, s -> Float.valueOf(s.getZeroWarningProportion())));
        gradeWarningInfo.setOne(highLowProportionService.warningTableHP(t5Filter, s -> Float.valueOf(s.getOneWarningProportion())));
        gradeWarningInfo.setTwo(highLowProportionService.warningTableHP(t5Filter, s -> Float.valueOf(s.getTwoWarningProportion())));
        gradeWarningInfo.setThree(highLowProportionService.warningTableHP(t5Filter, s -> Float.valueOf(s.getThreeWarningProportion())));
        warningSituation.setGradeWarningInfo(gradeWarningInfo);
        return warningSituation;
    }

    /**
     * 小学及以上整体情况
     */
    public PrimaryOverall getPrimaryOverall(List<PrimaryScreeningInfoTable> tables, List<StatConclusion> statConclusions) {


        PrimaryOverall primary = new PrimaryOverall();
        primary.setLowVisionProportion(countAndProportionService.lowVision(statConclusions).getProportion());
        primary.setMyopiaProportion(countAndProportionService.myopia(statConclusions).getProportion());
        primary.setEarlyMyopiaProportion(countAndProportionService.earlyMyopia(statConclusions).getProportion());
        primary.setLightMyopiaProportion(countAndProportionService.lightMyopia(statConclusions).getProportion());
        primary.setHighMyopiaProportion(countAndProportionService.highMyopia(statConclusions).getProportion());
        primary.setRecommendDoctorProportion(countAndProportionService.getRecommendDoctor(statConclusions).getProportion());
        primary.setLowVision(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getLowVisionProportion())));
        primary.setMyopia(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getMyopiaProportion())));
        primary.setEarlyMyopia(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getEarlyMyopiaProportion())));
        primary.setLightMyopia(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getLightMyopiaProportion())));
        primary.setHighMyopia(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getHighMyopiaProportion())));
        primary.setRecommendDoctor(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getRecommendDoctorProportion())));
        primary.setOwe(highLowProportionService.pScreeningInfoTableHP(tables, s -> Float.valueOf(s.getOweProportion())));
        primary.setTables(tables);
        return primary;
    }

    /**
     * 标题信息
     */
    public SchoolReportInfo generateInfo(School school) {
        SchoolReportInfo info = new SchoolReportInfo();
        info.setSchoolName(school.getName());
        info.setDate(new Date());
        return info;
    }

    /**
     * 学校概览
     */
    public Outline generateOutline(List<StatConclusion> statConclusions, School school, ScreeningPlan plan) {
        Outline outline = new Outline();
        outline.setSchoolName(school.getName());
        // TODO: 详细地址
        outline.setAddress(districtService.getById(school.getDistrictId()).getName());
        outline.setStartDate(plan.getStartTime());
        outline.setEndDate(plan.getEndTime());
        outline.setGradeTotal(statConclusions.stream().map(StatConclusion::getSchoolGradeCode).distinct().count());
        // TODO: 班级
        outline.setClassTotal(statConclusions.stream().map(StatConclusion::getSchoolClassName).distinct().count());
        outline.setStudentTotal(screeningPlanSchoolStudentService.getByScreeningPlanIdAndSchoolId(plan.getId(), school.getId()).size());
        outline.setUnScreening(outline.getStudentTotal() - statConclusions.size());
        outline.setInvalidTotal(statConclusions.stream().filter(s -> !s.getIsValid()).count());
        List<StatConclusion> validList = statConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        outline.setValidTotal((long) validList.size());
        outline.setMValidTotal(validList.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count());
        outline.setFValidTotal(validList.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).count());
        return outline;
    }

    /**
     * 屈光情况
     */
    public RefractiveAbnormalities getRefractiveAbnormalities(List<StatConclusion> statConclusions) {
        RefractiveAbnormalities refractiveAbnormalities = new RefractiveAbnormalities();
        refractiveAbnormalities.setRefractiveError(countAndProportionService.refractiveError(statConclusions));
        refractiveAbnormalities.setAnisometropia(countAndProportionService.anisometropia(statConclusions));
        refractiveAbnormalities.setInsufficient(countAndProportionService.insufficient(statConclusions));
        return refractiveAbnormalities;
    }

    /**
     * 幼儿园-不同性别屈光情况
     */
    public SexRefractive kSexRefractive(List<StatConclusion> statConclusions) {
        SexRefractive sexRefractive = new SexRefractive();
        sexRefractive.setInsufficientInfo(insufficientRefractive(statConclusions));
        sexRefractive.setRefractiveErrorInfo(refractiveErrorRefractive(statConclusions));
        sexRefractive.setAnisometropiaInfo(anisometropiaRefractive(statConclusions));
        sexRefractive.setRecommendDoctorInfo(recommendDoctorRefractive(statConclusions));
        sexRefractive.setTables(screeningReportTableService.genderRefractiveTable(statConclusions));
        return sexRefractive;
    }

    /**
     * 幼儿园历史
     */
    public HistoryRefractive getHistoryRefractive(List<StatConclusion> statConclusions) {
        List<RefractiveTable> kHistoryRefractiveTable = screeningReportTableService.kHistoryRefractiveTable(statConclusions);
        HistoryRefractive historyRefractive = new HistoryRefractive();
        historyRefractive.setTables(Lists.newArrayList(kHistoryRefractiveTable));
        historyRefractive.setLowVisionProportion(getChainRatioProportion(kHistoryRefractiveTable.stream().map(RefractiveTable::getLowVisionProportion).collect(Collectors.toList())));
        historyRefractive.setInsufficientProportion(getChainRatioProportion(kHistoryRefractiveTable.stream().map(RefractiveTable::getInsufficientProportion).collect(Collectors.toList())));
        historyRefractive.setRefractiveErrorProportion(getChainRatioProportion(kHistoryRefractiveTable.stream().map(RefractiveTable::getRefractiveErrorProportion).collect(Collectors.toList())));
        historyRefractive.setAnisometropiaProportion(getChainRatioProportion(kHistoryRefractiveTable.stream().map(RefractiveTable::getAnisometropiaProportion).collect(Collectors.toList())));
        return historyRefractive;
    }

    public TwoTuple<Integer, Integer> getStatAgeRange(List<StatConclusion> statConclusions) {
        List<Integer> collect = statConclusions.stream().sorted(Comparator.comparing(StatConclusion::getAge)).map(StatConclusion::getAge).collect(Collectors.toList());
        if (collect.size() == 1) {
            return new TwoTuple<>(collect.get(0), collect.get(0));
        }
        return new TwoTuple<>(collect.get(0), collect.get(collect.size() - 1));
    }

    /**
     * 各班筛查数据
     */
    public List<ClassScreeningData> generateClassScreeningData(List<StatConclusion> statConclusions, School school, Boolean isk) {
        List<ClassScreeningData> dataList = new ArrayList<>();
        List<Integer> resultIds = statConclusions.stream().map(StatConclusion::getResultId).collect(Collectors.toList());
        List<Integer> studentIds = statConclusions.stream().map(StatConclusion::getStudentId).collect(Collectors.toList());

        List<VisionScreeningResult> resultList = visionScreeningResultService.getByIds(resultIds);
        Map<Integer, VisionScreeningResult> resultMap = resultList.stream().collect(Collectors.toMap(VisionScreeningResult::getId, Function.identity()));
        Map<Integer, String> studentNameMap = studentService.getByIds(studentIds).stream().collect(Collectors.toMap(Student::getId, Student::getName));

        Map<String, Map<String, List<StatConclusion>>> statMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, Collectors.groupingBy(StatConclusion::getSchoolClassName, Collectors.toList())));

        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = screeningPlanSchoolStudentService.getByIds(resultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList())).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));

        List<SchoolGrade> gradeList = schoolGradeService.getBySchoolId(school.getId());
        Map<Integer, List<SchoolClassExportDTO>> classMap = schoolClassService.getByGradeIds(gradeList.stream().map(SchoolGrade::getId).collect(Collectors.toList())).stream().collect(Collectors.groupingBy(SchoolClassExportDTO::getGradeId));

        gradeList.forEach(grade -> {
            Map<String, List<StatConclusion>> gradeStatMap = statMap.get(grade.getGradeCode());
            if (CollectionUtils.isEmpty(gradeStatMap)) {
                return;
            }
            List<SchoolClassExportDTO> schoolClassList = classMap.get(grade.getId());
            schoolClassList.forEach(schoolClass -> {
                List<StatConclusion> classStatList = gradeStatMap.get(schoolClass.getName());
                if (CollectionUtils.isEmpty(classStatList)) {
                    return;
                }
                ClassScreeningData classScreeningData = new ClassScreeningData();
                classScreeningData.setGradeName(grade.getName());
                classScreeningData.setClassName(schoolClass.getName());
                List<ScreeningDataReportTable> dataReportTableList = new ArrayList<>();
                classStatList.forEach(sourceData -> {
                    VisionScreeningResult result = resultMap.get(sourceData.getResultId());
                    dataReportTableList.add(getReportDate(sourceData, result, studentNameMap.get(sourceData.getStudentId()), planStudentMap.get(result.getScreeningPlanSchoolStudentId()), isk));
                });
                classScreeningData.setTables(dataReportTableList);
                dataList.add(classScreeningData);
            });
        });
        return dataList;
    }

    private ScreeningDataReportTable getReportDate(StatConclusion statConclusion, VisionScreeningResult result, String studentName, ScreeningPlanSchoolStudent planStudent, Boolean isk) {

        ScreeningDataReportTable table = new ScreeningDataReportTable();
        table.setName(studentName);
        table.setGender(GenderEnum.getName(statConclusion.getGender()));
        table.setGlassesType(GlassesTypeEnum.getDescByCode(statConclusion.getGlassesType()));
        table.setNakedVision(ScreeningDataFormatUtils.singleEyeDateFormat(EyeDataUtil.rightNakedVision(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormat(EyeDataUtil.leftNakedVision(result)));
        table.setCorrectedVision(ScreeningDataFormatUtils.singleEyeDateFormat(EyeDataUtil.rightCorrectedVision(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormat(EyeDataUtil.leftCorrectedVision(result)));
        table.setSph(ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.rightSph(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.leftSph(result)));
        table.setCyl(ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.rightCyl(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.leftCyl(result)));
        table.setAxsi(ScreeningDataFormatUtils.singleEyeDateFormatZero(EyeDataUtil.rightAxial(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormatZero(EyeDataUtil.leftAxial(result)));
        table.setSe(ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.rightSE(result)) + "/" + ScreeningDataFormatUtils.singleEyeDateFormatTwo(EyeDataUtil.leftSE(result)));
        table.setVisionInfo(statConclusion.getIsLowVision());
        if (isk) {
            table.setRefractiveInfo(kVisionAnalyze(statConclusion.getIsRefractiveError(), statConclusion.getWarningLevel()));
        } else {
            table.setRefractiveInfo(pVisionAnalyze(statConclusion.getIsMyopia(), statConclusion.getIsAstigmatism(), statConclusion.getIsHyperopia()));
        }
        table.setMyopiaCorrection(VisionCorrection.getDesc(statConclusion.getVisionCorrection()));
        table.setVisionWarning(WarningLevel.getDesc(statConclusion.getWarningLevel()));
        table.setIsRecommendDoctor(statConclusion.getIsRecommendVisit());

        table.setRemark(getRemark(statConclusion.getIsValid(), planStudent.getState()));
        return table;
    }

    private String pVisionAnalyze(Boolean isMyopia, Boolean isAstigmatism, Boolean isHyperopia) {
        List<String> result = new ArrayList<>();
        if (Objects.equals(isMyopia, Boolean.TRUE)) {
            result.add("近视");
        }
        if (Objects.equals(isAstigmatism, Boolean.TRUE)) {
            result.add("散光");
        }
        if (Objects.equals(isHyperopia, Boolean.TRUE)) {
            result.add("远视");
        }
        return result.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }

    private String kVisionAnalyze(Boolean isRefractiveError, Integer warningLevel) {

        boolean isZeroSp = WarningLevel.ZERO_SP.code.equals(warningLevel);
        if (Objects.equals(isRefractiveError, Boolean.FALSE) && !isZeroSp) {
            return "正常";
        }

        if (Objects.equals(isRefractiveError, Boolean.TRUE) && !isZeroSp) {
            return "屈光异常（远视储备不足）";
        }

        if (Objects.equals(isRefractiveError, Boolean.FALSE) && isZeroSp) {
            return "屈光异常（可能导致弱视）";
        }

        if (Objects.equals(isRefractiveError, Boolean.TRUE) && isZeroSp) {
            return "屈光异常（远视储备不足/可能导致弱视）";
        }
        return StringUtils.EMPTY;
    }

    private String getRemark(Boolean isValid,Integer state) {
        if (Objects.equals(isValid, Boolean.TRUE)) {
            return "数据缺失【不满足初筛完整数据判断】";
        }
        if (state == 1) {
            return "请假";
        }
        if (state == 2) {
            return "转学";
        }
        if (state == 3) {
            return "其他";
        }
        return StringUtils.EMPTY;
    }
}
