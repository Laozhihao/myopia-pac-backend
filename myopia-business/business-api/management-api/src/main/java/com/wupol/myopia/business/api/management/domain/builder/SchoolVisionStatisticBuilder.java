package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
@UtilityClass
public class SchoolVisionStatisticBuilder {

    public static SchoolVisionStatistic build(School school, ScreeningOrganization screeningOrg, Integer screeningNoticeId, Integer screeningTaskId, Integer screeningPlanId,
                                              List<StatConclusionDTO> statConclusions, Integer realScreeningNumber, Integer planScreeningNumbers) {
        SchoolVisionStatistic statistic = new SchoolVisionStatistic();
        Integer wearingGlassNumber =
                (int) statConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        Integer myopiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        Integer ametropiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer lowVisionNumber = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        // 近视等级人数
        Map<Integer, Long> myopiaLevelMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getMyopiaLevel, Collectors.counting()));
        // 预警人群、建议就诊使用所有筛查数据（有效、无效）
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionLabelZeroSPNumbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer treatmentAdviceNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionL).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(StatConclusion::getVisionR).average().orElse(0);
        int validScreeningNumbers = statConclusions.size();
        statistic.setSchoolId(school.getId()).setSchoolName(school.getName()).setSchoolType(school.getType())
                .setScreeningOrgId(screeningOrg.getId()).setScreeningOrgName(screeningOrg.getName())
                .setScreeningNoticeId(screeningNoticeId).setScreeningTaskId(screeningTaskId).setScreeningPlanId(screeningPlanId).setDistrictId(school.getDistrictId())
                .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision)).setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                .setWearingGlassesNumbers(wearingGlassNumber).setWearingGlassesRatio(MathUtil.divide(wearingGlassNumber, validScreeningNumbers))
                .setMyopiaNumbers(myopiaNumber).setMyopiaRatio(MathUtil.divide(myopiaNumber, validScreeningNumbers))
                .setAmetropiaNumbers(ametropiaNumber).setAmetropiaRatio(MathUtil.divide(ametropiaNumber, validScreeningNumbers))
                .setLowVisionNumbers(lowVisionNumber).setLowVisionRatio(MathUtil.divide(lowVisionNumber, validScreeningNumbers))
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel0Ratio(MathUtil.divide(visionLabel0Numbers, validScreeningNumbers))
                .setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel1Ratio(MathUtil.divide(visionLabel1Numbers, validScreeningNumbers))
                .setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel2Ratio(MathUtil.divide(visionLabel2Numbers, validScreeningNumbers))
                .setVisionLabel3Numbers(visionLabel3Numbers).setVisionLabel3Ratio(MathUtil.divide(visionLabel3Numbers, validScreeningNumbers))
                .setTreatmentAdviceNumbers(treatmentAdviceNumber).setTreatmentAdviceRatio(MathUtil.divide(treatmentAdviceNumber, validScreeningNumbers))
                .setKeyWarningNumbers(keyWarningNumbers).setFocusTargetsNumbers(keyWarningNumbers).setValidScreeningNumbers(validScreeningNumbers)
                .setPlanScreeningNumbers(planScreeningNumbers).setRealScreeningNumbers(realScreeningNumber)
                .setMyopiaLevelEarly(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code,0L).intValue())
                .setMyopiaLevelLight(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code,0L).intValue())
                .setMyopiaLevelMiddle(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE.code,0L).intValue())
                .setMyopiaLevelHigh(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code,0L).intValue())
                .setMyopiaLevelInsufficient(visionLabelZeroSPNumbers);
        return statistic;
    }
}
