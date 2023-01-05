package com.wupol.myopia.business.api.management.service.report.refactor;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.ReportBaseSummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.SummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenScreeningSummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionInfoDTO;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
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


}
