package com.wupol.myopia.business.api.management.service.report.refactor;

import com.google.common.collect.Lists;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.framework.domain.TwoTuple;
import com.wupol.myopia.base.util.DigitUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.MyopiaDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.*;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.VisionCorrection;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 中小学视力报告
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class PrimarySchoolVisionReportService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private VisionReportService visionReportService;

    public PrimarySchoolVisionReportDTO primarySchoolVisionReport(Integer planId, Integer schoolId) {

        List<StatConclusion> allConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId).stream().filter(s -> !Objects.equals(SchoolAge.KINDERGARTEN.getCode(), s.getSchoolAge())).collect(Collectors.toList());

        // 获取数据，并修复数据
        StatBaseDTO statBase = new StatBaseDTO(allConclusions);
        statBase.dataRepair(visionScreeningResultService.getMapByIds(statBase.getWaitingRepairResultIds()));

        List<StatConclusion> statConclusions = statBase.getValid().stream().sorted(Comparator.comparing(s -> Integer.valueOf(s.getSchoolGradeCode()))).collect(Collectors.toList());
        statBase.setValid(statConclusions);
        StatGenderDTO statGender = new StatGenderDTO(statBase.getValid());

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planId, schoolId);
        // 获取年级
        List<SchoolGrade> gradeList = schoolGradeService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));

        List<String> gradeCodes = gradeList.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getGradeCode())).sorted(Comparator.comparing(s -> Integer.valueOf(s.getGradeCode()))).map(SchoolGrade::getGradeCode).collect(Collectors.toList());
        Map<Integer, String> gradeMap = gradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getGradeCode));

        // 获取班级
        List<SchoolClass> classList = schoolClassService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList()));
        sortStatList(planId, schoolId, classList);
        Map<String, List<SchoolClass>> classMap = classList.stream().collect(Collectors.groupingBy(s -> gradeMap.get(s.getGradeId())));

        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, List<StatConclusion>> statConclusionClassMap = statConclusions.stream().collect(Collectors.groupingBy(s -> s.getSchoolGradeCode() + s.getSchoolClassName()));

        ScreeningPlan sp = screeningPlanService.getById(planId);
        School school = schoolService.getById(schoolId);

        // 通过筛查数据进行统计
        PrimarySchoolVisionReportDTO reportDTO = new PrimarySchoolVisionReportDTO();
        // 总述
        reportDTO.setSummary(getScreeningSummary(sp, school, statBase, statGender, planSchoolStudents.size(), statConclusionGradeMap.keySet().size(), statConclusionClassMap.keySet().size()));

        // 学生近视情况
        CompletableFuture<MyopiaInfoDTO> c1 = CompletableFuture.supplyAsync(() -> {
            MyopiaInfoDTO myopiaInfo = getMyopiaInfo(statGender, reportDTO.getSummary(), gradeCodes, statConclusionGradeMap, classMap, statConclusionClassMap);
            reportDTO.setStudentMyopia(myopiaInfo);
            return myopiaInfo;
        }, executor);
        // 学生视力情况
        CompletableFuture<VisionInfoDTO> c2 = CompletableFuture.supplyAsync(() -> {
            VisionInfoDTO visionInfo = getVisionInfo(statBase.getValid(), statGender, reportDTO.getSummary(), gradeCodes, statConclusionGradeMap, classMap, statConclusionClassMap);
            reportDTO.setStudentVision(visionInfo);
            return visionInfo;
        }, executor);
        // 学生视力矫正情况
        CompletableFuture<VisionCorrectionSituationDTO> c3 = CompletableFuture.supplyAsync(() -> {
            VisionCorrectionSituationDTO visionCorrectionSituationDTO = generateVisionCorrectionSituationDTO(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap);
            reportDTO.setVisionCorrectionSituation(visionCorrectionSituationDTO);
            return visionCorrectionSituationDTO;
        }, executor);
        // 学生屈光情况
        CompletableFuture<RefractiveSituationDTO> c4 = CompletableFuture.supplyAsync(() -> {
            RefractiveSituationDTO refractiveSituationDTO = generateRefractiveSituationDTO(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap);
            reportDTO.setRefractiveSituation(refractiveSituationDTO);
            return refractiveSituationDTO;
        }, executor);
        // 视力检测预警情况
        CompletableFuture<WarningSituationDTO> c5 = CompletableFuture.supplyAsync(() -> {
            WarningSituationDTO warningSituation = generateWarningSituationDTO(gradeCodes, statConclusionGradeMap, statConclusions);
            reportDTO.setWarningSituation(warningSituation);
            return warningSituation;
        }, executor);
        CompletableFuture.allOf(c1, c2, c3, c4, c5).join();
        return reportDTO;
    }

    /**
     * 排序
     */
    private static void sortStatList(Integer planId, Integer schoolId, List<SchoolClass> classList) {
        try {
            classList.sort(Comparator.comparing(s -> Integer.valueOf(s.getName().substring(0, s.getName().length() - 1))));
            return;
        } catch (Exception e) {
            log.error("中小学报告年级排序异常,planId:{},schoolId:{}", planId, schoolId);
        }
        try {
            classList.sort(Comparator.comparing(s -> DigitUtil.chineseNumToArabicNum(s.getName().substring(0, s.getName().length() - 1))));
        } catch (Exception e) {
            log.error("中小学报告年级排序异常,planId:{},schoolId:{}", planId, schoolId);
        }
    }

    /**
     * 判断学生类型
     *
     * @param planId 计划Id
     * @param schoolId 学校Id
     *
     * @return SchoolStudentResponseDTO
     */
    public SchoolStudentResponseDTO schoolStudentType(Integer planId, Integer schoolId) {
        SchoolStudentResponseDTO responseDTO = new SchoolStudentResponseDTO();

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtils.isEmpty(planSchoolStudents)){
            responseDTO.setIsHavePrimary(false);
            responseDTO.setIsHaveKindergarten(false);
            return responseDTO;
        }
        // 幼儿园
        responseDTO.setIsHaveKindergarten(planSchoolStudents.stream().anyMatch(s -> Objects.equals(s.getGradeType(), SchoolAge.KINDERGARTEN.getType())));

        // 小学及以上
        responseDTO.setIsHavePrimary(planSchoolStudents.stream().anyMatch(s -> SchoolAge.primaryAndAboveCode().contains(s.getGradeType())));
        return responseDTO;
    }

    /**
     * 生成视力矫正情况
     *
     * @return VisionCorrectionSituationDTO
     */
    private VisionCorrectionSituationDTO generateVisionCorrectionSituationDTO(List<StatConclusion> statConclusions, List<String> gradeCodes,
                                                                              Map<String, List<SchoolClass>> classMap,
                                                                              Map<String, List<StatConclusion>> statConclusionGradeMap,
                                                                              Map<String, List<StatConclusion>> statConclusionClassMap) {
        VisionCorrectionSituationDTO visionCorrectionSituationDTO = new VisionCorrectionSituationDTO();

        // 视力矫正情况
        visionCorrectionSituationDTO.setVisionCorrectionSituationInfo(VisionCorrectionSituationDTO.VisionCorrectionSituationInfo.getInstance(statConclusions));

        // 戴镜情况
        visionCorrectionSituationDTO.setWearingGlasses(VisionCorrectionSituationDTO.WearingGlasses.getInstance(statConclusions));

        // 矫正情况
        visionCorrectionSituationDTO.setCorrectionSituation(VisionCorrectionSituationDTO.CorrectionSituation.getInstance(statConclusions));

        // 年级未矫/欠矫
        visionCorrectionSituationDTO.setGradeUnderCorrectedAndUncorrected(VisionCorrectionSituationDTO.GradeUnderCorrectedAndUncorrected.getInstance(gradeCodes, statConclusionGradeMap));

        // 班级未矫/欠矫
        visionCorrectionSituationDTO.setClassUnderCorrectedAndUncorrected(VisionCorrectionSituationDTO.ClassUnderCorrectedAndUncorrected.getInstance(gradeCodes, classMap, statConclusionClassMap));
        return visionCorrectionSituationDTO;
    }

    /**
     * 屈光情况
     *
     * @return RefractiveSituationDTO
     */
    private RefractiveSituationDTO generateRefractiveSituationDTO(List<StatConclusion> statConclusions, List<String> gradeCodes,
                                                                  Map<String, List<SchoolClass>> classMap,
                                                                  Map<String, List<StatConclusion>> statConclusionGradeMap,
                                                                  Map<String, List<StatConclusion>> statConclusionClassMap) {
        RefractiveSituationDTO refractiveSituationDTO = new RefractiveSituationDTO();

        // 屈光情况信息
        refractiveSituationDTO.setRefractiveSituationInfo(RefractiveSituationDTO.RefractiveSituationInfo.getInstance(statConclusions));

        // 不同性别屈光情况
        refractiveSituationDTO.setGenderRefractiveSituation(RefractiveSituationDTO.GenderRefractiveSituation.getInstance(statConclusions));

        // 不同年级屈光情况
        refractiveSituationDTO.setGradeRefractiveSituation(RefractiveSituationDTO.GradeRefractiveSituation.getInstance(gradeCodes, statConclusionGradeMap));

        // 不同班级屈光情况/欠矫
        refractiveSituationDTO.setClassRefractiveSituation(RefractiveSituationDTO.ClassRefractiveSituation.getInstance(gradeCodes, classMap, statConclusionClassMap));

        return refractiveSituationDTO;
    }

    /**
     * 预警情况
     *
     * @return WarningSituationDTO
     */
    private WarningSituationDTO generateWarningSituationDTO(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap, List<StatConclusion> statConclusions) {
        WarningSituationDTO warningSituationDTO = new WarningSituationDTO();

        // 不同年级学生视力预警情况
        warningSituationDTO.setGradeWarningSituation(WarningSituationDTO.GradeWarningSituation.getInstance(gradeCodes, statConclusionGradeMap, statConclusions, false));
        return warningSituationDTO;
    }

    /**
     * 获取报告总述信息
     * @param sp
     * @param school
     * @param statBase
     * @param statGender
     * @param planScreeningNum
     * @param gradeNum
     * @param classNum
     * @return
     */
    public ScreeningSummaryDTO getScreeningSummary(ScreeningPlan sp, School school, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum) {

        ScreeningSummaryDTO summary = new ScreeningSummaryDTO();
        // 获取通用概述
        ReportBaseSummaryDTO baseSummary = visionReportService.getScreeningSummary(sp, school, statBase, statGender, planScreeningNum, gradeNum, classNum);
        BeanUtils.copyProperties(baseSummary, summary);

        // 按预警等级分类，计算预警人数
        List<StatConclusion> valid = statBase.getValid();
        int validSize = valid.size();

        // 近视人数
        int myopiaNum = (int)valid.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        return summary.setMyopiaNum(myopiaNum)
                .setMyopiaRatio(MathUtil.divideFloat(myopiaNum, validSize))
                .setUncorrectedRatio(MathUtil.divideFloat((int)valid.stream().filter(stat -> VisionCorrection.UNCORRECTED.code.equals(stat.getVisionCorrection())).count(), myopiaNum))
                .setUnderCorrectedRatio(MathUtil.divideFloat((int)valid.stream().filter(stat -> VisionCorrection.UNDER_CORRECTED.code.equals(stat.getVisionCorrection())).count(),
                        (int)valid.stream().filter(stat -> !GlassesTypeEnum.NOT_WEARING.code.equals(stat.getGlassesType())).count()))
                .setLightMyopiaRatio(MathUtil.divideFloat((int)valid.stream().filter(stat -> MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code.equals(stat.getMyopiaLevel())).count(), validSize))
                .setHighMyopiaRatio(MathUtil.divideFloat((int)valid.stream().filter(stat -> MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code.equals(stat.getMyopiaLevel())).count(), validSize));

    }

    /**
     * 学生近视情况
     * @param statGender
     * @param summary
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public MyopiaInfoDTO getMyopiaInfo(StatGenderDTO statGender, ScreeningSummaryDTO summary,
                                       List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap,
                                       Map<String, List<SchoolClass>> classMap,
                                       Map<String, List<StatConclusion>> statConclusionClassMap) {
        ThreeTuple<List<MyopiaDTO>, Float, Float> genderMyopia = getGenderMyopia(statGender, summary);
        TwoTuple<List<MyopiaInfoDTO.StudentGenderMyopia>, List<SummaryDTO>> gradeMyopia = getGradeMyopia(gradeCodes, statConclusionGradeMap);
        return MyopiaInfoDTO.builder()
                .genderMyopia(genderMyopia.getFirst())
                .maleGeneralMyopiaRatio(genderMyopia.getSecond())
                .femaleGeneralMyopiaRatio(genderMyopia.getThird())
                .gradeMyopia(gradeMyopia.getFirst())
                .gradeMyopiaSummary(gradeMyopia.getSecond())
                .classMyopia(getClassMyopia(gradeCodes, classMap, statConclusionClassMap))
                .build();
    }

    /**
     * 获取不同性别学生近视情况
     * @param statGender
     * @param summary
     * @return 不同性别近视情况，男生占总体近视率，女生占总体近视率
     */
    public ThreeTuple<List<MyopiaDTO>, Float, Float> getGenderMyopia(StatGenderDTO statGender, ScreeningSummaryDTO summary) {

        int maleNum = statGender.getMale().size();
        int femaleNum = statGender.getFemale().size();

        int maleMyopiaNum = (int)statGender.getMale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        int femaleMyopiaNum = (int)statGender.getFemale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();

        MyopiaDTO male = MyopiaDTO.getInstance(maleNum, "男", maleMyopiaNum, MathUtil.divideFloat(maleMyopiaNum, maleNum));
        MyopiaDTO female = MyopiaDTO.getInstance(femaleNum, "女", femaleMyopiaNum, MathUtil.divideFloat(femaleMyopiaNum, femaleNum));
        MyopiaDTO total = MyopiaDTO.getInstance(summary.getValidScreeningNum(), "总体情况", summary.getMyopiaNum(), summary.getMyopiaRatio());

        return new ThreeTuple<>(Arrays.asList(male, female, total),
                MathUtil.divideFloat(maleMyopiaNum, summary.getValidScreeningNum()),
                MathUtil.divideFloat(femaleMyopiaNum, summary.getValidScreeningNum()));
    }

    /**
     * 获取学生近视监测结果（年级）及总结
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @return  年级近视情况，年级近视总结
     */
    public TwoTuple<List<MyopiaInfoDTO.StudentGenderMyopia>, List<SummaryDTO>> getGradeMyopia(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        // 整体年级各性别近视情况
        List<MyopiaInfoDTO.StudentGenderMyopia> gradeMyopia = gradeCodes.stream().map(grade -> {
            MyopiaInfoDTO.StudentGenderMyopia item = MyopiaInfoDTO.StudentGenderMyopia.getGradeInstance(GradeCodeEnum.getDesc(grade));
            conclusion2GenderMyopia(statConclusionGradeMap.get(grade), item);
            return item;
        }).collect(Collectors.toList());
        // 年级近视总结
        SummaryDTO general = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getMyopiaRatio, "general");
        // 男生近视总结
        SummaryDTO male = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getMaleMyopiaRatio, "male");
        // 女生近视总结
        SummaryDTO female = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getFemaleMyopiaRatio, "female");
        return TwoTuple.of(gradeMyopia, Arrays.asList(general, male, female));
    }

    /**
     * 年级近视总结
     * @param gradeMyopia
     * @param keyMapper
     * @param keyName
     * @return
     */
    private SummaryDTO gradeMyopiaSummary(List<MyopiaInfoDTO.StudentGenderMyopia> gradeMyopia, Function<? super MyopiaInfoDTO.StudentGenderMyopia, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = gradeMyopia.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.getGradeName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

    /**
     * 获取学生近视监测结果（班级）
     * @param gradeCodes
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public List<MyopiaInfoDTO.StudentGenderMyopia> getClassMyopia(List<String> gradeCodes,
                                                                  Map<String, List<SchoolClass>> classMap,
                                                                  Map<String, List<StatConclusion>> statConclusionClassMap) {
        return gradeCodes.stream().map(grade -> {
            // 生成班级近视情况数据
            List<SchoolClass> classes = classMap.getOrDefault(grade, Collections.emptyList());
            List<MyopiaInfoDTO.StudentGenderMyopia> classMyopia = classes.stream().map(clazz -> {
                MyopiaInfoDTO.StudentGenderMyopia gradeMyopia = MyopiaInfoDTO.StudentGenderMyopia.getClassInstance(GradeCodeEnum.getDesc(grade), clazz.getName());
                conclusion2GenderMyopia(statConclusionClassMap.get(grade + clazz.getName()), gradeMyopia);
                return gradeMyopia;
            }).collect(Collectors.toList());
            // 每一条数据设置rowspan
            if (!CollectionUtils.isEmpty(classMyopia)) {
                classMyopia.get(0).setRowSpan(classMyopia.size());
            }
            return classMyopia;
        }).reduce(new ArrayList<>(), (list1, list2) -> {list1.addAll(list2); return list1;});
    }

    /**
     * 统计各性别近视情况
     * @param stats
     * @param genderMyopia
     * @return
     */
    private void conclusion2GenderMyopia(List<StatConclusion> stats, GenderMyopiaInfoDTO genderMyopia) {
        // 若没有统计数据，生成无数据情况下近视情况
        if (CollectionUtils.isEmpty(stats)) {
            genderMyopia.empty();
            return ;
        }
        // 统计按性别近视情况
        int validScreeningNum = stats.size();
        int myopiaNum = (int)stats.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        StatGenderDTO statGender = new StatGenderDTO(stats);
        int maleNum = statGender.getMale().size();
        int maleMyopiaNum = (int)statGender.getMale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        int femaleNum = statGender.getFemale().size();
        int femaleMyopiaNum = (int)statGender.getFemale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        genderMyopia.generateData(validScreeningNum, myopiaNum, maleNum, maleMyopiaNum, femaleNum, femaleMyopiaNum);
    }

    /**
     * 学生近视情况
     * @param statGender
     * @param summary
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public VisionInfoDTO getVisionInfo(List<StatConclusion> valid, StatGenderDTO statGender, ScreeningSummaryDTO summary,
                                       List<String> gradeCodes,
                                       Map<String, List<StatConclusion>> statConclusionGradeMap,
                                       Map<String, List<SchoolClass>> classMap,
                                       Map<String, List<StatConclusion>> statConclusionClassMap) {
        ThreeTuple<LowVisionLevelDTO, LowVisionLevelDTO, VisionInfoDTO.LowVisionSummary> generalVision = getGeneralVision(valid);
        TwoTuple<List<VisionInfoDTO.GenderLowVisionLevel>, List<SummaryDTO>> genderVision = getGenderVision(statGender);
        TwoTuple<List<VisionInfoDTO.StudentLowVisionLevel>, SummaryDTO> gradeVision = getGradeVision(gradeCodes, statConclusionGradeMap);
        return VisionInfoDTO.builder()
                .general(generalVision.getFirst())
                .lowVision(generalVision.getSecond())
                .lowVisionSummary(generalVision.getThird())
                .genderVision(genderVision.getFirst())
                .genderVisionSummary(genderVision.getSecond())
                .gradeVision(gradeVision.getFirst())
                .gradeVisionSummary(gradeVision.getSecond())
                .classVision(getClassVision(gradeCodes, classMap, statConclusionClassMap))
                .build();
    }

    /**
     * 获取整体视力程度情况
     * @param valid
     * @return 整体视力程度情况, 视力不良程度情况, 视力不良总结
     */
    public ThreeTuple<LowVisionLevelDTO, LowVisionLevelDTO, VisionInfoDTO.LowVisionSummary> getGeneralVision(List<StatConclusion> valid) {
        // 整体视力程度情况
        LowVisionLevelDTO general = new LowVisionLevelDTO();
        conclusion2MyopiaLevel(valid, general, true);
        // 视力不良程度情况
        LowVisionLevelDTO lowVision = new LowVisionLevelDTO();
        conclusion2MyopiaLevel(valid, lowVision, false);
        // 视力不良总结
        VisionInfoDTO.LowVisionSummary lowVisionSummary = new VisionInfoDTO.LowVisionSummary();
        BeanUtils.copyProperties(getLowVisionSummary(general), lowVisionSummary);
        lowVisionSummary.setLowVisionRatio(Math.max(Math.max(lowVision.getLightLowVisionRatio(), lowVision.getMiddleLowVisionRatio()), lowVision.getHighLowVisionRatio()));
        return new ThreeTuple(general, lowVision, lowVisionSummary);
    }

    /**
     * 获取性别视力情况及总结
     * @param statGender
     * @return 性别视力情况，总结
     */
    public TwoTuple<List<VisionInfoDTO.GenderLowVisionLevel>, List<SummaryDTO>> getGenderVision(StatGenderDTO statGender) {
        // 男女视力情况
        VisionInfoDTO.GenderLowVisionLevel male = VisionInfoDTO.GenderLowVisionLevel.getInstance("男");
        conclusion2MyopiaLevel(statGender.getMale(), male, true);
        VisionInfoDTO.GenderLowVisionLevel female = VisionInfoDTO.GenderLowVisionLevel.getInstance("女");
        conclusion2MyopiaLevel(statGender.getFemale(), female,true);
        // 视力情况及总结
        return TwoTuple.of(Arrays.asList(male, female),  Arrays.asList(getLowVisionSummary(male), getLowVisionSummary(female)));
    }

    /**
     * 获取视力程度情况（年级）及总结
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @return 视力程度情况（年级），总结
     */
    public TwoTuple<List<VisionInfoDTO.StudentLowVisionLevel>, SummaryDTO> getGradeVision(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        // 整体年级各性别视力情况
        List<VisionInfoDTO.StudentLowVisionLevel> gradeMyopia = gradeCodes.stream().map(grade -> {
            VisionInfoDTO.StudentLowVisionLevel item = VisionInfoDTO.StudentLowVisionLevel.getGradeInstance(GradeCodeEnum.getDesc(grade));
            conclusion2MyopiaLevel(statConclusionGradeMap.get(grade), item, true);
            return item;
        }).collect(Collectors.toList());
        // 年级整体视力不良总结
        SummaryDTO general = gradeVisionSummary(gradeMyopia, VisionInfoDTO.StudentLowVisionLevel::getLowVisionRatio, "general");
        return TwoTuple.of(gradeMyopia, general);
    }

    /**
     * 获取视力程度情况（班级）
     * @param gradeCodes
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public List<VisionInfoDTO.StudentLowVisionLevel> getClassVision(List<String> gradeCodes,
                                                                    Map<String, List<SchoolClass>> classMap,
                                                                    Map<String, List<StatConclusion>> statConclusionClassMap) {
        return gradeCodes.stream().map(grade -> {
            // 生成班级视力情况数据
            List<SchoolClass> classes = classMap.getOrDefault(grade, Collections.emptyList());
            List<VisionInfoDTO.StudentLowVisionLevel> classVision = classes.stream().map(clazz -> {
                VisionInfoDTO.StudentLowVisionLevel gradeVision = VisionInfoDTO.StudentLowVisionLevel.getClassInstance(GradeCodeEnum.getDesc(grade), clazz.getName());
                conclusion2MyopiaLevel(statConclusionClassMap.get(grade + clazz.getName()), gradeVision, true);
                return gradeVision;
            }).collect(Collectors.toList());
            // 每一条数据设置rowspan
            if (!CollectionUtils.isEmpty(classVision)) {
                classVision.get(0).setRowSpan(classVision.size());
            }
            return classVision;
        }).reduce(new ArrayList<>(), (list1, list2) -> {list1.addAll(list2); return list1;});
    }

    /**
     * 生成视力不良情况总结
     * @param general
     * @return
     */
    public SummaryDTO getLowVisionSummary(LowVisionLevelDTO general) {
        SummaryDTO summary = new SummaryDTO();
        // 设置最高占比
        summary.setHighRadio(Math.max(Math.max(general.getLightLowVisionRatio(), general.getMiddleLowVisionRatio()), general.getHighLowVisionRatio()));
        // 设置最高占比说明
        List<String> highLowVisionLevel = new ArrayList<>();
        if (summary.getHighRadio().equals(general.getLightLowVisionRatio())) {
            highLowVisionLevel.add("轻度视力不良率");
        }
        if (summary.getHighRadio().equals(general.getMiddleLowVisionRatio())) {
            highLowVisionLevel.add("中度视力不良率");
        }
        if (summary.getHighRadio().equals(general.getHighLowVisionRatio())) {
            highLowVisionLevel.add("高度视力不良率");

        }
        summary.setHighName(highLowVisionLevel);
        return summary;
    }

    /**
     * 年级近视总结
     * @param gradeVistel
     * @param keyMapper
     * @param keyName
     * @return
     */
    private SummaryDTO gradeVisionSummary(List<VisionInfoDTO.StudentLowVisionLevel> gradeVistel, Function<? super VisionInfoDTO.StudentLowVisionLevel, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = gradeVistel.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.getGradeName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

    /**
     * 统计各性别近视情况
     * @param stats
     * @param myopiaLevel
     * @return
     */
    private void conclusion2MyopiaLevel(List<StatConclusion> stats, LowVisionLevelDTO myopiaLevel, boolean isGlobalRatio) {
        // 若没有统计数据，生成无数据情况下近视情况
        if (CollectionUtils.isEmpty(stats)) {
            myopiaLevel.empty();
            return ;
        }
        // 统计视力不良各级情况
        Map<Integer, Long> lowVisionLevelMap = stats.stream().filter(stat -> Objects.nonNull(stat.getLowVisionLevel())).collect(Collectors.groupingBy(StatConclusion::getLowVisionLevel, Collectors.counting()));
        int validScreeningNum = stats.size();
        int lowVisionNum = (int)stats.stream().filter(s->Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
        int lightMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT.code, 0L).intValue();
        int middleMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE.code, 0L).intValue();
        int highMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH.code, 0L).intValue();
        myopiaLevel.generateData(validScreeningNum, lowVisionNum, lightMyopiaNum, middleMyopiaNum, highMyopiaNum, isGlobalRatio);
    }

}