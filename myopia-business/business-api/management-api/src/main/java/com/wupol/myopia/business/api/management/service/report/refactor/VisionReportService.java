package com.wupol.myopia.business.api.management.service.report.refactor;

import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.StatBaseDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatGenderDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.ReportBaseSummaryDTO;
import com.wupol.myopia.business.common.utils.constant.NumberCommonConst;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public ReportBaseSummaryDTO getScreeningSummary(ScreeningPlan sp, School school, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum) {

        // 按预警等级分类，计算预警人数
        int validSize = statBase.getValid().size();
        Map<Integer, Long> warningLevelMap = statBase.getValid().stream().filter(s-> Objects.nonNull(s.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        int warningNum = (int)warningLevelMap.keySet().stream().filter(WarningLevel::isWarning).mapToLong(x -> warningLevelMap.getOrDefault(x, 0L)).sum();

        // 视力不良/低常人数
        int lowVisionNum = (int)statBase.getValid().stream().filter(s-> Objects.equals(s.getIsLowVision(), Boolean.TRUE)).count();

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

}
