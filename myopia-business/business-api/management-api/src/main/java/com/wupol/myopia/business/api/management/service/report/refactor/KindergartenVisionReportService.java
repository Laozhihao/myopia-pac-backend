package com.wupol.myopia.business.api.management.service.report.refactor;

import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.WarningSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenRefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionReportDTO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private ThreadPoolTaskExecutor executor;
    @Resource
    private VisionReportService visionReportService;

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
    public KindergartenScreeningSummaryDTO getScreeningSummary(ScreeningPlan sp, School school, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum) {

    @Resource
    private DistrictService districtService;
        KindergartenScreeningSummaryDTO summary = new KindergartenScreeningSummaryDTO();
        // 获取通用概述
        ReportBaseSummaryDTO baseSummary = visionReportService.getScreeningSummary(sp, school, statBase, statGender, planScreeningNum, gradeNum, classNum);
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
    public KindergartenVisionInfoDTO getVisionInfo(List<StatConclusion> valid, StatGenderDTO statGender, KindergartenScreeningSummaryDTO summary,
                                                   List<String> gradeCodes,
                                                   Map<String, List<StatConclusion>> statConclusionGradeMap,
                                                   Map<String, List<SchoolClass>> classMap,
                                                   Map<String, List<StatConclusion>> statConclusionClassMap) {

        return KindergartenVisionInfoDTO.builder()
                .genderVision(null)
                .gradeVision(null)
                .gradeVisionSummary(null)
                .classVision(null)
                .build();
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

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    public KindergartenVisionReportDTO kindergartenSchoolVisionReport(Integer planId, Integer schoolId) {


        List<StatConclusion> allConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId).stream().filter(s -> SchoolAge.primaryAndAboveCode().contains(s.getSchoolAge())).collect(Collectors.toList());

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
        try {
            classList.sort(Comparator.comparing(s -> Integer.valueOf(s.getName().substring(0, s.getName().length() - 1))));
        } catch (Exception e) {
            log.error("中小学报告年级排序异常!planId:{},schoolId:{}", planId, schoolId);
        }
        Map<String, List<SchoolClass>> classMap = classList.stream().collect(Collectors.groupingBy(s -> gradeMap.get(s.getGradeId())));

        Map<String, List<StatConclusion>> statConclusionGradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, List<StatConclusion>> statConclusionClassMap = statConclusions.stream().collect(Collectors.groupingBy(s -> s.getSchoolGradeCode() + s.getSchoolClassName()));

        KindergartenVisionReportDTO reportDTO = new KindergartenVisionReportDTO();
        reportDTO.setSummary(null);
        reportDTO.setStudentVision(null);
        reportDTO.setKindergartenRefractiveSituationDTO(generateRefractiveSituation(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap));
        reportDTO.setWarningSituation(generateWarningSituation(gradeCodes, statConclusionGradeMap, statConclusions));
        return reportDTO;
    }

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

    private WarningSituationDTO generateWarningSituation(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap, List<StatConclusion> statConclusions) {
        WarningSituationDTO warningSituationDTO = new WarningSituationDTO();

        warningSituationDTO.setGradeWarningSituation(WarningSituationDTO.GradeWarningSituation.getInstance(gradeCodes, statConclusionGradeMap, statConclusions, true));
        return warningSituationDTO;
    }
}