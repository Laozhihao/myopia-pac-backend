package com.wupol.myopia.business.api.management.service.report.refactor;

import com.google.common.collect.Lists;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.framework.domain.TwoTuple;
import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.ReportBaseSummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.SummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.WarningSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenRefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenScreeningSummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionInfoDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionReportDTO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 幼儿园视力报告
 *
 * @Author wulizhou
 * @Date 2023/1/4 12:29
 */
@Service
@Slf4j
public class KindergartenVisionReportService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private VisionReportService visionReportService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    public KindergartenVisionReportDTO kindergartenSchoolVisionReport(Integer planId, Integer schoolId) {


        List<StatConclusion> allConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId).stream().filter(s -> Objects.equals(s.getSchoolAge(), SchoolAge.KINDERGARTEN.code)).collect(Collectors.toList());

        // 获取数据，并修复数据
        StatBaseDTO statBase = new StatBaseDTO(allConclusions);
        statBase.dataRepair(visionScreeningResultService.getMapByIds(statBase.getWaitingRepairResultIds()));

        List<StatConclusion> statConclusions = statBase.getValid().stream().sorted(Comparator.comparing(s -> Integer.valueOf(s.getSchoolGradeCode()))).collect(Collectors.toList());
        statBase.setValid(statConclusions);
        StatGenderDTO statGender = new StatGenderDTO(statBase.getValid());

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planId, schoolId);
        // 获取年级
        List<SchoolGrade> gradeList = schoolGradeService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));

        List<String> gradeCodes = gradeList.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getGradeCode())).sorted(Comparator.comparing(s -> Integer.valueOf(s.getGradeCode()))).map(SchoolGrade::getGradeCode).collect(Collectors.toList());
        Map<Integer, String> gradeMap = gradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getGradeCode));

        // 获取班级
        List<SchoolClass> classList = schoolClassService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList()));
        schoolClassService.sortStatList(classList);
        Map<String, List<SchoolClass>> classMap = classList.stream().collect(Collectors.groupingBy(s -> gradeMap.get(s.getGradeId())));

        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, List<StatConclusion>> statConclusionClassMap = statConclusions.stream().collect(Collectors.groupingBy(s -> s.getSchoolGradeCode() + s.getSchoolClassName()));

        KindergartenVisionReportDTO reportDTO = new KindergartenVisionReportDTO();
        reportDTO.setSummary(getScreeningSummary(planId, schoolId, statBase, statGender, planSchoolStudents.size(), statConclusionGradeMap.keySet().size(), statConclusionClassMap.keySet().size()));
        reportDTO.setStudentVision(getVisionInfo(statGender, reportDTO.getSummary(), gradeCodes, statConclusionGradeMap, classMap, statConclusionClassMap));
        reportDTO.setKindergartenRefractiveSituationDTO(generateRefractiveSituation(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap));
        reportDTO.setWarningSituation(generateWarningSituation(gradeCodes, statConclusionGradeMap, statConclusions));
        return reportDTO;
    }

    /**
     * 屈光情况
     *
     * @param statConclusions        统计数据
     * @param gradeCodes             年级Code
     * @param classMap               班级Map
     * @param statConclusionGradeMap 年级数据分组
     * @param statConclusionClassMap 班级数据分组
     *
     * @return KindergartenRefractiveSituationDTO
     */
    private KindergartenRefractiveSituationDTO generateRefractiveSituation(List<StatConclusion> statConclusions, List<String> gradeCodes,
                                                                           Map<String, List<SchoolClass>> classMap,
                                                                           Map<String, List<StatConclusion>> statConclusionGradeMap,
                                                                           Map<String, List<StatConclusion>> statConclusionClassMap) {
        KindergartenRefractiveSituationDTO refractiveSituationDTO = new KindergartenRefractiveSituationDTO();
        refractiveSituationDTO.setGenderRefractiveSituation(KindergartenRefractiveSituationDTO.GenderRefractiveSituation.getInstance(statConclusions));
        refractiveSituationDTO.setGradeRefractiveSituation(KindergartenRefractiveSituationDTO.GradeRefractiveSituation.getInstance(gradeCodes, statConclusionGradeMap));
        refractiveSituationDTO.setClassRefractiveSituation(KindergartenRefractiveSituationDTO.ClassRefractiveSituation.getInstance(gradeCodes, classMap, statConclusionClassMap));
        return refractiveSituationDTO;
    }

    /**
     * 预警情况
     *
     * @param gradeCodes             年级Code
     * @param statConclusionGradeMap 年级数据分组
     * @param statConclusions        统计数据
     *
     * @return WarningSituationDTO
     */
    private WarningSituationDTO generateWarningSituation(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap, List<StatConclusion> statConclusions) {
        WarningSituationDTO warningSituationDTO = new WarningSituationDTO();
        warningSituationDTO.setGradeWarningSituation(WarningSituationDTO.GradeWarningSituation.getInstance(gradeCodes, statConclusionGradeMap, statConclusions, true));
        return warningSituationDTO;
    }

    /**
     * 获取报告总述信息
     * @param planId
     * @param schoolId
     * @param statBase
     * @param statGender
     * @param planScreeningNum
     * @param gradeNum
     * @param classNum
     * @return
     */
    public KindergartenScreeningSummaryDTO getScreeningSummary(Integer planId, Integer schoolId, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum) {

        KindergartenScreeningSummaryDTO summary = new KindergartenScreeningSummaryDTO();
        // 获取通用概述
        ReportBaseSummaryDTO baseSummary = visionReportService.getScreeningSummary(planId, schoolId, statBase, statGender, planScreeningNum, gradeNum, classNum);
        BeanUtils.copyProperties(baseSummary, summary);

        // 按预警等级分类，计算预警人数
        List<StatConclusion> valid = statBase.getValid();
        int validSize = valid.size();

        // 屈光不正人数
        int refractiveErrorNum = (int)valid.stream().filter(sc->Objects.equals(Boolean.TRUE, sc.getIsRefractiveError())).count();
        // 屈光参差人数
        int anisometropiaNum = (int)valid.stream().filter(sc->Objects.equals(Boolean.TRUE, sc.getIsAnisometropia())).count();
        // 远视储备不足
        int insufficientHyperopiaNum = (int)valid.stream().filter(sc -> Objects.equals(WarningLevel.ZERO_SP.getCode(), sc.getWarningLevel())).count();

        return summary.setRefractiveErrorNum(refractiveErrorNum)
                .setRefractiveErrorRatio(MathUtil.divideFloat(refractiveErrorNum, validSize))
                .setAnisometropiaNum(anisometropiaNum)
                .setAnisometropiaRatio(MathUtil.divideFloat(anisometropiaNum, validSize))
                .setInsufficientHyperopiaNum(insufficientHyperopiaNum)
                .setInsufficientHyperopiaRatio(MathUtil.divideFloat(insufficientHyperopiaNum, validSize));

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
    public KindergartenVisionInfoDTO getVisionInfo(StatGenderDTO statGender, KindergartenScreeningSummaryDTO summary,
                                                   List<String> gradeCodes,
                                                   Map<String, List<StatConclusion>> statConclusionGradeMap,
                                                   Map<String, List<SchoolClass>> classMap,
                                                   Map<String, List<StatConclusion>> statConclusionClassMap) {
        // 获取性别维度视力情况
        ThreeTuple<List<KindergartenVisionInfoDTO.KindergartenGenderLowVision>, Float, Float> genderVision = getGenderVision(statGender, summary);
        // 获取年级视力情况及总结
        TwoTuple<List<KindergartenVisionInfoDTO.KindergartenStudentLowVision>, SummaryDTO> gradeVision = getGradeVision(gradeCodes, statConclusionGradeMap);
        return KindergartenVisionInfoDTO.builder()
                .genderVision(genderVision.getFirst())
                .maleGeneralLowVisionRatio(genderVision.getSecond())
                .femaleGeneralLowVisionRatio(genderVision.getThird())
                .gradeVision(gradeVision.getFirst())
                .gradeVisionSummary(gradeVision.getSecond())
                .classVision(getClassVision(gradeCodes, classMap, statConclusionClassMap))
                .build();
    }

    /**
     * 获取性别视力情况及总结
     * @param statGender
     * @return 性别视力情况, 男生总休视力低常率， 女生总体视力低常率
     */
    public ThreeTuple<List<KindergartenVisionInfoDTO.KindergartenGenderLowVision>, Float, Float> getGenderVision(StatGenderDTO statGender, KindergartenScreeningSummaryDTO summary) {
        // 男女视力情况
        KindergartenVisionInfoDTO.KindergartenGenderLowVision male = KindergartenVisionInfoDTO.KindergartenGenderLowVision.getInstance("男");
        conclusion2Vision(statGender.getMale(), male);
        KindergartenVisionInfoDTO.KindergartenGenderLowVision female = KindergartenVisionInfoDTO.KindergartenGenderLowVision.getInstance("女");
        conclusion2Vision(statGender.getFemale(), female);
        // 总结
        KindergartenVisionInfoDTO.KindergartenGenderLowVision total = KindergartenVisionInfoDTO.KindergartenGenderLowVision.getInstance("总体情况");
        total.setLowVisionNum(summary.getLowVisionNum()).setLowVisionRatio(summary.getLowVisionRatio()).setValidScreeningNum(summary.getValidScreeningNum());
        return new ThreeTuple<>(Arrays.asList(male, female, total),
                MathUtil.divideFloat(male.getLowVisionNum(), summary.getValidScreeningNum()),
                MathUtil.divideFloat(female.getLowVisionNum(), summary.getValidScreeningNum()));
    }

    /**
     * 获取学生视力情况及总结
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @return  年级视力情况，年级视力总结
     */
    public TwoTuple<List<KindergartenVisionInfoDTO.KindergartenStudentLowVision>, SummaryDTO> getGradeVision(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        // 整体年级各性别近视情况
        List<KindergartenVisionInfoDTO.KindergartenStudentLowVision> gradeVision = gradeCodes.stream().map(grade -> {
            KindergartenVisionInfoDTO.KindergartenStudentLowVision item = KindergartenVisionInfoDTO.KindergartenStudentLowVision.getGradeInstance(GradeCodeEnum.getDesc(grade));
            conclusion2Vision(statConclusionGradeMap.get(grade), item);
            return item;
        }).collect(Collectors.toList());
        // 年级近视总结
        SummaryDTO general = gradeVisionSummary(gradeVision, KindergartenVisionInfoDTO.KindergartenStudentLowVision::getLowVisionRatio, "general");
        return TwoTuple.of(gradeVision, general);
    }

    /**
     * 获取视力程度情况（班级）
     * @param gradeCodes
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public List<KindergartenVisionInfoDTO.KindergartenStudentLowVision> getClassVision(List<String> gradeCodes,
                                                                    Map<String, List<SchoolClass>> classMap,
                                                                    Map<String, List<StatConclusion>> statConclusionClassMap) {
        return gradeCodes.stream().map(grade -> {
            // 生成班级视力情况数据
            List<SchoolClass> classes = classMap.getOrDefault(grade, Collections.emptyList());
            List<KindergartenVisionInfoDTO.KindergartenStudentLowVision> classVision = classes.stream().map(clazz -> {
                KindergartenVisionInfoDTO.KindergartenStudentLowVision gradeVision = KindergartenVisionInfoDTO.KindergartenStudentLowVision.getClassInstance(GradeCodeEnum.getDesc(grade), clazz.getName());
                conclusion2Vision(statConclusionClassMap.get(grade + clazz.getName()), gradeVision);
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
     * 视力低常情况
     * @param stats
     * @param vision
     * @return
     */
    private void conclusion2Vision(List<StatConclusion> stats, KindergartenVisionInfoDTO.KindergartenLowVision vision) {
        // 若没有统计数据，生成无数据情况下近视情况
        if (CollectionUtils.isEmpty(stats)) {
            vision.empty();
            return ;
        }
        // 统计视力低常情况
        int lowVisionNum = (int)stats.stream().filter(s-> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
        vision.generateData(stats.size(), lowVisionNum);
    }

    /**
     * 年级低常总结
     * @param gradeVision
     * @param keyMapper
     * @param keyName
     * @return
     */
    private SummaryDTO gradeVisionSummary(List<KindergartenVisionInfoDTO.KindergartenStudentLowVision> gradeVision, Function<? super KindergartenVisionInfoDTO.KindergartenStudentLowVision, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = gradeVision.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.getGradeName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

}