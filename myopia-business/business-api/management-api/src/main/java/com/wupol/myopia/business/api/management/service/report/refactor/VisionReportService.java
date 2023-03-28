package com.wupol.myopia.business.api.management.service.report.refactor;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.HasDimension;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.ReportBaseSummaryDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.SummaryDTO;
import com.wupol.myopia.business.common.utils.constant.NumberCommonConst;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校视力报告服务
 * @Author wulizhou
 * @Date 2023/1/4 17:57
 */
@Service
@Slf4j
public class VisionReportService {

    @Autowired
    private DistrictService districtService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private SchoolService schoolService;

    /**
     * 获取报告总述信息
     * @param planId
     * @param schoolId
     * @param statBase
     * @param statGender
     * @param planScreeningNum
     * @param gradeNum
     * @param classNum
     * @param isPrimary 是否小学
     * @return
     */
    public ReportBaseSummaryDTO getScreeningSummary(Integer planId, Integer schoolId, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum, boolean isPrimary) {

        ScreeningPlan sp = screeningPlanService.getById(planId);
        School school = schoolService.getById(schoolId);
        // 按预警等级分类，计算预警人数
        int validSize = statBase.getValid().size();
        Map<Integer, Long> warningLevelMap = statBase.getValid().stream().filter(s-> Objects.nonNull(s.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        int warningNum = (int)warningLevelMap.keySet().stream().filter(WarningLevel::isWarning).mapToLong(x -> warningLevelMap.getOrDefault(x, 0L)).sum();

        // 视力不良/低常人数
        int lowVisionNum = (int)statBase.getValid().stream().filter(s-> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();
        if (isPrimary) {
            // 若为小学，视力不良人数需加上夜戴人数
            lowVisionNum += (int)statBase.getValid().stream().filter(stat -> GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(stat.getGlassesType())).count();
        }

        // 组装报告的通用概述
        return ReportBaseSummaryDTO.builder()
                .schoolName(school.getName())
                .schoolDistrict(districtService.getDistrictName(school.getDistrictDetail()))
                .reportTime(new Date())
                .startTime(sp.getStartTime())
                .endTime(sp.getEndTime())
                .gradeNum(gradeNum)
                .classNum(classNum)
                .planScreeningNum(planScreeningNum)
                .unscreenedNum(planScreeningNum - statBase.getFirstScreen().size())
                .invalidScreeningNum(statBase.getFirstScreen().size() - validSize)
                .validScreeningNum(validSize)
                .maleValidScreeningNum(statGender.getMale().size())
                .femaleValidScreeningNum(statGender.getFemale().size())
                .averageVision((averageVision(statBase.getValid()).floatValue()))
                .lowVisionNum(lowVisionNum)
                .lowVisionRatio(MathUtil.divideFloat(lowVisionNum, validSize))
                .warningNum(warningNum)
                .warningRatio(MathUtil.divideFloat(warningNum, validSize))
                .warningLevelZeroNum(warningLevelMap.getOrDefault(WarningLevel.ZERO.code, 0L) + warningLevelMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L))
                .warningLevelOneNum(warningLevelMap.getOrDefault(WarningLevel.ONE.code, 0L))
                .warningLevelTwoNum(warningLevelMap.getOrDefault(WarningLevel.TWO.code, 0L))
                .warningLevelThreeNum(warningLevelMap.getOrDefault(WarningLevel.THREE.code, 0L))
                .build();

    }


    /**
     * 平均视力
     * @param valid
     * @return
     */
    public BigDecimal averageVision(List<StatConclusion> valid) {
        if (CollectionUtils.isEmpty(valid)) {
            return new BigDecimal("0");
        }
        // 去除夜戴角膜塑形镜的无法视力数据
        List<StatConclusion> hasVision = valid.stream().filter(stat -> !GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(stat.getGlassesType())).collect(Collectors.toList());
        BigDecimal visionNum = hasVision.stream().map(stat -> stat.getVisionR().add(stat.getVisionL())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return visionNum.divide(new BigDecimal(hasVision.size() * 2), NumberCommonConst.ONE_INT, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取总结
     * @param dimensions
     * @param keyMapper
     * @param keyName
     * @return
     */
    public SummaryDTO getSummary(List<? extends HasDimension> dimensions, Function<? super HasDimension, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = dimensions.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.dimensionName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

}
