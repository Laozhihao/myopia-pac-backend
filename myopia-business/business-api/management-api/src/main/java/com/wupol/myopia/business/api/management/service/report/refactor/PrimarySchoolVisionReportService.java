package com.wupol.myopia.business.api.management.service.report.refactor;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.PrimarySchoolVisionReportDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.RefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.VisionCorrectionSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.WarningSituationDTO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public PrimarySchoolVisionReportDTO primarySchoolVisionReport(Integer planId, Integer schoolId) {
        List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId).stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).filter(s -> !Objects.equals(SchoolAge.KINDERGARTEN.getCode(), s.getSchoolAge())).sorted(Comparator.comparing(s -> Integer.valueOf(s.getSchoolGradeCode()))).collect(Collectors.toList());

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planId, schoolId);
        // 获取年级
        List<SchoolGrade> gradeList = schoolGradeService.getByIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));

        List<String> gradeCodes = gradeList.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getGradeCode())).sorted(Comparator.comparing(s -> Integer.valueOf(s.getGradeCode()))).map(SchoolGrade::getGradeCode).collect(Collectors.toList());
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

        PrimarySchoolVisionReportDTO reportDTO = new PrimarySchoolVisionReportDTO();

        reportDTO.setVisionCorrectionSituation(generateVisionCorrectionSituationDTO(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap));
        reportDTO.setRefractiveSituation(generateRefractiveSituationDTO(statConclusions, gradeCodes, classMap, statConclusionGradeMap, statConclusionClassMap));
        reportDTO.setWarningSituation(generateWarningSituationDTO(gradeCodes, statConclusionGradeMap));
        return reportDTO;
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
    private WarningSituationDTO generateWarningSituationDTO(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        WarningSituationDTO warningSituationDTO = new WarningSituationDTO();

        // 不同年级学生视力预警情况
        warningSituationDTO.setGradeWarningSituation(WarningSituationDTO.GradeWarningSituation.getInstance(gradeCodes, statConclusionGradeMap));
        return warningSituationDTO;
    }
}