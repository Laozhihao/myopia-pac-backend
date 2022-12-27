package com.wupol.myopia.business.api.management.service.report.refactor;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.base.util.MapUtils;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.PrimarySchoolVisionReportDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.RefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.VisionCorrectionSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.WarningSituationDTO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 中小学视力报告
 *
 * @author Simple4H
 */
@Service
public class PrimarySchoolVisionReportService {

    private final static String TOTAL_DESC = "总体情况";

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    public PrimarySchoolVisionReportDTO primarySchoolVisionReport(Integer planId, Integer schoolId) {
        List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId).stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).filter(s -> !Objects.equals(SchoolAge.KINDERGARTEN.getCode(), s.getSchoolAge())).sorted(Comparator.comparing(s -> Integer.valueOf(s.getSchoolGradeCode()))).collect(Collectors.toList());

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planId, schoolId);
        // 获取年级
        List<SchoolGrade> gradeList = schoolGradeService.getByIds(planSchoolStudents.stream()
                .map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));

        List<String> gradeCodes = gradeList.stream()
                .filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getGradeCode())).
                sorted(Comparator.comparing(s -> Integer.valueOf(s.getGradeCode())))
                .map(SchoolGrade::getGradeCode).collect(Collectors.toList());
        Map<Integer, String> gradeMap = gradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getGradeCode));

        // 获取班级
        List<SchoolClass> classList = schoolClassService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList()));
        Map<String, List<SchoolClass>> classMap = classList.stream().collect(Collectors.groupingBy(s -> gradeMap.get(s.getGradeId())));

        PrimarySchoolVisionReportDTO reportDTO = new PrimarySchoolVisionReportDTO();

        reportDTO.setVisionCorrectionSituationDTO(generateVisionCorrectionSituationDTO(statConclusions, gradeCodes, classMap));
        reportDTO.setRefractiveSituationDTO(generateRefractiveSituationDTO(statConclusions, gradeCodes, classMap));
        reportDTO.setWarningSituationDTO(generateWarningSituationDTO(statConclusions, gradeCodes));
        return reportDTO;
    }

    /**
     * 生成视力矫正情况
     *
     * @return VisionCorrectionSituationDTO
     */
    private VisionCorrectionSituationDTO generateVisionCorrectionSituationDTO(List<StatConclusion> statConclusions, List<String> gradeCodes, Map<String, List<SchoolClass>> classMap) {
        VisionCorrectionSituationDTO visionCorrectionSituationDTO = new VisionCorrectionSituationDTO();
        long screeningTotal = statConclusions.size();
        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, List<StatConclusion>> statConclusionClassMap = statConclusions.stream().collect(Collectors.groupingBy(s -> s.getSchoolGradeCode() + s.getSchoolClassName()));

        VisionCorrectionSituationDTO.VisionCorrectionSituationInfo visionCorrectionSituationInfo = new VisionCorrectionSituationDTO.VisionCorrectionSituationInfo();
        visionCorrectionSituationInfo.setScreeningStudentNum(screeningTotal);
        visionCorrectionSituationInfo.setWearingGlassesNum(statConclusions.stream().filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.getCode())).count());
        visionCorrectionSituationInfo.setWearingGlassesRatio(BigDecimalUtil.divideRadio(visionCorrectionSituationInfo.getWearingGlassesNum(), visionCorrectionSituationInfo.getScreeningStudentNum()));
        visionCorrectionSituationDTO.setVisionCorrectionSituationInfo(visionCorrectionSituationInfo);

        VisionCorrectionSituationDTO.WearingGlasses wearingGlasses = new VisionCorrectionSituationDTO.WearingGlasses();
        VisionCorrectionSituationDTO.WearingGlassesItem wearingGlassesItem = new VisionCorrectionSituationDTO.WearingGlassesItem();
        wearingGlassesItem.setScreeningStudentNum(screeningTotal);
        wearingGlassesItem.setNotWearingNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.NOT_WEARING.getCode()));
        wearingGlassesItem.setNotWearingRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getNotWearingNum(), screeningTotal));
        wearingGlassesItem.setFrameGlassesNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.FRAME_GLASSES.getCode()));
        wearingGlassesItem.setFrameGlassesRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getFrameGlassesNum(), screeningTotal));
        wearingGlassesItem.setContactLensNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.CONTACT_LENS.getCode()));
        wearingGlassesItem.setContactLensRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getContactLensNum(), screeningTotal));
        wearingGlassesItem.setOrthokeratologyNum(wearingGlassesCount(statConclusions, GlassesTypeEnum.ORTHOKERATOLOGY.getCode()));
        wearingGlassesItem.setOrthokeratologyRatio(BigDecimalUtil.divideRadio(wearingGlassesItem.getOrthokeratologyNum(), screeningTotal));
        wearingGlasses.setWearingGlassesItem(wearingGlassesItem);
        wearingGlasses.setTable(null);
        visionCorrectionSituationDTO.setWearingGlasses(wearingGlasses);

        VisionCorrectionSituationDTO.CorrectionSituation correctionSituation = new VisionCorrectionSituationDTO.CorrectionSituation();
        correctionSituation.setUnderCorrectedAndUncorrected(getUnderCorrectedAndUncorrected(statConclusions, new VisionCorrectionSituationDTO.UnderCorrectedAndUncorrected()));
        correctionSituation.setTable(null);
        visionCorrectionSituationDTO.setCorrectionSituation(correctionSituation);

        VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrected gradeUnderCorrectedAndUncorrected = new VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrected();
        List<VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem> gradeUnderCorrectedAndUncorrectedItems = gradeCodes.stream().map(s -> {
            List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
            VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem correctedAndUncorrectedItem = new VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrectedItem();
            correctedAndUncorrectedItem.setGradeName(GradeCodeEnum.getDesc(s));
            return getUnderCorrectedAndUncorrected(gradeStatConclusion, correctedAndUncorrectedItem);
        }).collect(Collectors.toList());
        gradeUnderCorrectedAndUncorrected.setItems(gradeUnderCorrectedAndUncorrectedItems);
        gradeUnderCorrectedAndUncorrected.setTable(null);
        visionCorrectionSituationDTO.setGradeUnderCorrectedAndUncorrected(gradeUnderCorrectedAndUncorrected);

        VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrected classUnderCorrectedAndUncorrected = new VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrected();
        List<VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem> items = new ArrayList<>();
        gradeCodes.forEach(s -> {
            List<SchoolClass> schoolClasses = classMap.get(s);
            AtomicBoolean isFirst = new AtomicBoolean(true);
            schoolClasses.forEach(schoolClass -> {
                List<StatConclusion> classStatConclusion = statConclusionClassMap.getOrDefault(s + schoolClass.getName(), new ArrayList<>());
                VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem classUnderCorrectedAndUncorrectedItem = new VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrectedItem();
                classUnderCorrectedAndUncorrectedItem.setGradeName(GradeCodeEnum.getDesc(s));
                classUnderCorrectedAndUncorrectedItem.setClassName(schoolClass.getName());
                classUnderCorrectedAndUncorrectedItem.setRowSpan(isFirst, schoolClasses.size());
                items.add(getUnderCorrectedAndUncorrected(classStatConclusion, classUnderCorrectedAndUncorrectedItem));
            });
        });
        classUnderCorrectedAndUncorrected.setItems(items);
        visionCorrectionSituationDTO.setClassUnderCorrectedAndUncorrected(classUnderCorrectedAndUncorrected);
        return visionCorrectionSituationDTO;
    }

    /**
     * 屈光情况
     *
     * @return RefractiveSituationDTO
     */
    private RefractiveSituationDTO generateRefractiveSituationDTO(List<StatConclusion> statConclusions, List<String> gradeCodes, Map<String, List<SchoolClass>> classMap) {
        RefractiveSituationDTO refractiveSituationDTO = new RefractiveSituationDTO();
        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, List<StatConclusion>> statConclusionClassMap = statConclusions.stream().collect(Collectors.groupingBy(s -> s.getSchoolGradeCode() + s.getSchoolClassName()));

        RefractiveSituationDTO.RefractiveSituationInfo refractiveSituationInfo = new RefractiveSituationDTO.RefractiveSituationInfo();
        refractiveSituationInfo.setRefractiveErrorNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count());
        refractiveSituationDTO.setRefractiveSituationInfo(getRefractiveSituation(statConclusions, refractiveSituationInfo));

        RefractiveSituationDTO.GenderRefractiveSituation genderRefractiveSituation = new RefractiveSituationDTO.GenderRefractiveSituation();
        Map<Integer, List<StatConclusion>> genderStatConclusion = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<RefractiveSituationDTO.RefractiveSituationItem> genderList = GenderEnum.genderList().stream().map(s -> {
            List<StatConclusion> genderStatConclusionList = genderStatConclusion.getOrDefault(s.type, new ArrayList<>());
            RefractiveSituationDTO.RefractiveSituationItem refractiveSituation = new RefractiveSituationDTO.RefractiveSituationItem();
            refractiveSituation.setGenderName(s.desc);
            getRefractiveSituation(genderStatConclusionList, refractiveSituation);
            return refractiveSituation;
        }).collect(Collectors.toList());
        RefractiveSituationDTO.RefractiveSituationItem totalRefractiveSituationItem = new RefractiveSituationDTO.RefractiveSituationItem();
        totalRefractiveSituationItem.setGenderName(TOTAL_DESC);
        genderList.add(getRefractiveSituation(statConclusions, totalRefractiveSituationItem));
        genderRefractiveSituation.setItems(genderList);
        genderRefractiveSituation.setTable1(null);
        genderRefractiveSituation.setTable2(null);
        genderRefractiveSituation.setTable3(null);
        refractiveSituationDTO.setGenderRefractiveSituation(genderRefractiveSituation);

        RefractiveSituationDTO.GradeRefractiveSituation gradeRefractiveSituation = new RefractiveSituationDTO.GradeRefractiveSituation();
        gradeRefractiveSituation.setItems(gradeCodes.stream().map(s -> {
            List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
            RefractiveSituationDTO.GradeRefractiveSituationItem gradeRefractiveSituationItem = new RefractiveSituationDTO.GradeRefractiveSituationItem();
            gradeRefractiveSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
            return getRefractiveSituation(gradeStatConclusion, gradeRefractiveSituationItem);
        }).collect(Collectors.toList()));
        gradeRefractiveSituation.setTable1(null);
        gradeRefractiveSituation.setTable2(null);
        gradeRefractiveSituation.setTable3(null);
        RefractiveSituationDTO.RefractiveSituationSummary LowMyopiaSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getLowMyopiaRatio, "lowMyopia");
        RefractiveSituationDTO.RefractiveSituationSummary highMyopiaSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getHighMyopiaRatio, "highMyopia");
        RefractiveSituationDTO.RefractiveSituationSummary astigmatismSummary = getGradeRefractiveSituationSummary(gradeRefractiveSituation.getItems(), RefractiveSituationDTO.RefractiveSituation::getAstigmatismRatio, "astigmatism");
        gradeRefractiveSituation.setSummary(Lists.newArrayList(LowMyopiaSummary, highMyopiaSummary, astigmatismSummary));
        refractiveSituationDTO.setGradeRefractiveSituation(gradeRefractiveSituation);

        List<RefractiveSituationDTO.ClassRefractiveSituationItem> items = new ArrayList<>();
        gradeCodes.forEach(s -> {
            List<SchoolClass> schoolClasses = classMap.get(s);
            AtomicBoolean isFirst = new AtomicBoolean(true);
            schoolClasses.forEach(schoolClass -> {
                List<StatConclusion> classStatConclusion = statConclusionClassMap.getOrDefault(s + schoolClass.getName(), new ArrayList<>());
                RefractiveSituationDTO.ClassRefractiveSituationItem classRefractiveSituationItem = new RefractiveSituationDTO.ClassRefractiveSituationItem();
                classRefractiveSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
                classRefractiveSituationItem.setClassName(schoolClass.getName());
                classRefractiveSituationItem.setRowSpan(isFirst, schoolClasses.size());
                items.add(getRefractiveSituation(classStatConclusion, classRefractiveSituationItem));
            });
        });
        RefractiveSituationDTO.ClassRefractiveSituation classRefractiveSituation = new RefractiveSituationDTO.ClassRefractiveSituation();
        classRefractiveSituation.setItems(items);
        refractiveSituationDTO.setClassRefractiveSituation(classRefractiveSituation);

        return refractiveSituationDTO;
    }

    /**
     * 预警情况
     *
     * @return WarningSituationDTO
     */
    private WarningSituationDTO generateWarningSituationDTO(List<StatConclusion> statConclusions, List<String> gradeCodes) {
        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        WarningSituationDTO warningSituationDTO = new WarningSituationDTO();
        WarningSituationDTO.GradeWarningSituation gradeWarningSituation = new WarningSituationDTO.GradeWarningSituation();
        List<WarningSituationDTO.GradeWarningSituationItem> items = gradeCodes.stream().map(s -> {
            List<StatConclusion> gradeStatConclusion = statConclusionGradeMap.getOrDefault(s, new ArrayList<>());
            WarningSituationDTO.GradeWarningSituationItem gradeWarningSituationItem = new WarningSituationDTO.GradeWarningSituationItem();
            gradeWarningSituationItem.setGradeName(GradeCodeEnum.getDesc(s));
            return getWarningSituation(gradeStatConclusion, gradeWarningSituationItem);
        }).collect(Collectors.toList());
        gradeWarningSituation.setItems(items);
        gradeWarningSituation.setTable(null);
        warningSituationDTO.setGradeWarningSituation(gradeWarningSituation);
        return warningSituationDTO;
    }


    /**
     * 总结
     *
     * @return RefractiveSituationDTO.GradeRefractiveSituationSummary
     */
    private RefractiveSituationDTO.RefractiveSituationSummary getGradeRefractiveSituationSummary(List<RefractiveSituationDTO.GradeRefractiveSituationItem> gradeRefractiveSituationItems,
                                                                                                 Function<RefractiveSituationDTO.GradeRefractiveSituationItem, String> myopiaLevelFunction,
                                                                                                 String keyName) {
        RefractiveSituationDTO.RefractiveSituationSummary refractiveSituationSummary = new RefractiveSituationDTO.RefractiveSituationSummary();
        Map<String, List<RefractiveSituationDTO.GradeRefractiveSituationItem>> sortMap = MapUtils.sortMap(gradeRefractiveSituationItems.stream().collect(Collectors.groupingBy(myopiaLevelFunction)));
        String firstKey = MapUtils.getFirstKey(sortMap);
        Map.Entry<String, List<RefractiveSituationDTO.GradeRefractiveSituationItem>> tail = MapUtils.getLastKey(sortMap);
        refractiveSituationSummary.setHighName(sortMap.get(tail.getKey()).stream().map(RefractiveSituationDTO.GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
        refractiveSituationSummary.setHighRadio(tail.getKey());
        refractiveSituationSummary.setLowName(sortMap.get(firstKey).stream().map(RefractiveSituationDTO.GradeRefractiveSituationItem::getGradeName).collect(Collectors.toList()));
        refractiveSituationSummary.setLowRadio(firstKey);
        refractiveSituationSummary.setKeyName(keyName);
        return refractiveSituationSummary;
    }

    /**
     * 矫正统计
     *
     * @return T
     */
    private <T extends VisionCorrectionSituationDTO.UnderCorrectedAndUncorrected> T getUnderCorrectedAndUncorrected(List<StatConclusion> statConclusions, T t) {
        t.setScreeningStudentNum((long) statConclusions.size());
        t.setUncorrectedNum(underCorrectedAndUncorrectedCount(statConclusions, VisionCorrection.UNCORRECTED.getCode()));
        t.setUncorrectedRatio(BigDecimalUtil.divideRadio(t.getUncorrectedNum(), statConclusions.stream().filter(s -> Objects.equals(s.getIsMyopia(), Boolean.TRUE)).count()));
        t.setUnderCorrectedNum(underCorrectedAndUncorrectedCount(statConclusions, VisionCorrection.UNDER_CORRECTED.getCode()));
        t.setUnderCorrectedRatio(BigDecimalUtil.divideRadio(t.getUnderCorrectedNum(), statConclusions.stream().filter(s -> !Objects.equals(s.getGlassesType(), GlassesTypeEnum.NOT_WEARING.getCode())).count()));
        return t;
    }

    /**
     * 屈光情况
     *
     * @return T
     */
    private <T extends RefractiveSituationDTO.RefractiveSituation> T getRefractiveSituation(List<StatConclusion> statConclusions, T t) {
        long screeningTotal = statConclusions.size();
        t.setScreeningStudentNum(screeningTotal);
        t.setLowMyopiaNum(myopiaLevelCount(statConclusions, MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code));
        t.setLowMyopiaRatio(BigDecimalUtil.divideRadio(t.getLowMyopiaNum(), screeningTotal));
        t.setHighMyopiaNum(myopiaLevelCount(statConclusions, MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code));
        t.setHighMyopiaRatio(BigDecimalUtil.divideRadio(t.getHighMyopiaNum(), screeningTotal));
        t.setAstigmatismNum(statConclusions.stream().filter(s -> Objects.equals(s.getIsAstigmatism(), Boolean.TRUE)).count());
        t.setAstigmatismRatio(BigDecimalUtil.divideRadio(t.getAstigmatismNum(), screeningTotal));
        return t;
    }

    /**
     * 预警情况
     *
     * @return T
     */
    private <T extends WarningSituationDTO.WarningSituation> T getWarningSituation(List<StatConclusion> statConclusions, T t) {
        long screeningTotal = statConclusions.size();
        t.setScreeningStudentNum(screeningTotal);
        t.setZeroWarningNum(statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), WarningLevel.ONE.code) || Objects.equals(s.getWarningLevel(), WarningLevel.ZERO_SP.code)).count());
        t.setZeroWarningRatio(BigDecimalUtil.divideRadio(t.getZeroWarningNum(), screeningTotal));
        t.setOneWarningNum(warningSituationCount(statConclusions, WarningLevel.ONE.code));
        t.setOneWarningRatio(BigDecimalUtil.divideRadio(t.getOneWarningNum(), screeningTotal));
        t.setTwoWarningNum(warningSituationCount(statConclusions, WarningLevel.TWO.code));
        t.setTwoWarningRatio(BigDecimalUtil.divideRadio(t.getTwoWarningNum(), screeningTotal));
        t.setThreeWarningNum(warningSituationCount(statConclusions, WarningLevel.THREE.code));
        t.setThreeWarningRatio(BigDecimalUtil.divideRadio(t.getThreeWarningNum(), screeningTotal));
        return t;
    }

    /**
     * 戴镜统计
     *
     * @return Long
     */
    private Long wearingGlassesCount(List<StatConclusion> statConclusions, Integer glassesType) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getGlassesType(), glassesType)).count();
    }

    /**
     * 矫正统计
     *
     * @return Long
     */
    private Long underCorrectedAndUncorrectedCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getVisionCorrection(), type)).count();
    }

    /**
     * 近视
     *
     * @return Long
     */
    private Long myopiaLevelCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getMyopiaLevel(), type)).count();
    }

    /**
     * 预警情况统计
     *
     * @return Long
     */
    private Long warningSituationCount(List<StatConclusion> statConclusions, Integer type) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getWarningLevel(), type)).count();
    }
}