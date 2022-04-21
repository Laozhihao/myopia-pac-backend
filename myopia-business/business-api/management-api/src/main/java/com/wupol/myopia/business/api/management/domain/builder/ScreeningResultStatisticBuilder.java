package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 筛查结果统计构造器
 *
 * @author hang.yuan 2022/4/13 19:27
 */
@UtilityClass
public class ScreeningResultStatisticBuilder {



    /**
     * 按学校 - 视力筛查数据统计
     */
    public static VisionScreeningResultStatistic buildSchoolVisionScreening(School school,ScreeningOrganization screeningOrg, ScreeningPlan screeningPlan,
                                                                            Integer planScreeningNumbers,List<StatConclusionDTO> statConclusions) {


        Map<Boolean, List<StatConclusionDTO>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        Map<Boolean, List<StatConclusionDTO>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusionDTO> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusionDTO>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int realScreeningNumber = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();

        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        Integer wearingGlassNumber =(int) statConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        Integer myopiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        Integer ametropiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer lowVisionNumber = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();

        // 近视等级人数
        Map<Integer, Long> myopiaLevelMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getMyopiaLevel())).collect(Collectors.groupingBy(StatConclusion::getMyopiaLevel, Collectors.counting()));
        // 预警人群、建议就诊使用所有筛查数据（有效、无效）
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionLabelZeroSPNumbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer treatmentAdviceNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).average().orElse(0);
        int validScreeningNumbers = statConclusions.size();
        Integer type = school.getType();
        VisionAnalysis visionAnalysis = getVisionAnalysis(wearingGlassNumber, lowVisionNumber, treatmentAdviceNumber,
                avgLeftVision, avgRightVision, validScreeningNumbers,
                type,ametropiaNumber,myopiaNumber,visionLabelZeroSPNumbers,myopiaLevelMap);

        RescreenSituationDO rescreenSituation = new RescreenSituationDO();

        VisionWarningDO visionWarning = getVisionWarning(visionLabel0Numbers, visionLabel1Numbers, visionLabel2Numbers, visionLabel3Numbers, visionLabelZeroSPNumbers, keyWarningNumbers, validScreeningNumbers);

        statistic.setSchoolId(school.getId())
                .setSchoolType(type)
                .setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                .setScreeningPlanId(screeningPlan.getId())
                .setDistrictId(school.getDistrictId())
                .setVisionAnalysis(visionAnalysis)
                .setVisionWarning(visionWarning)
                .setValidScreeningNum(validScreeningNumbers)
                .setPlanScreeningNum(planScreeningNumbers)
                .setRealScreeningNum(realScreeningNumber);

        return statistic;
    }


    /**
     * 按学校 - 常见病筛查数据统计
     */
    public static CommonDiseaseScreeningResultStatistic buildSchoolCommonDiseaseScreening(School school,ScreeningOrganization screeningOrg, ScreeningPlan screeningPlan,
                                                                                          Integer planScreeningNumbers,List<StatConclusionDTO> statConclusions) {

        Map<Boolean, List<StatConclusionDTO>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        Map<Boolean, List<StatConclusionDTO>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusionDTO> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusionDTO>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int realScreeningNumber = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();

        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        Integer wearingGlassNumber =
                (int) statConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        Integer myopiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        Integer ametropiaNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        Integer lowVisionNumber = (int) statConclusions.stream().filter(StatConclusion::getIsLowVision).count();

        // 近视等级人数
        Map<Integer, Long> myopiaLevelMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getMyopiaLevel())).collect(Collectors.groupingBy(StatConclusion::getMyopiaLevel, Collectors.counting()));
        // 预警人群、建议就诊使用所有筛查数据（有效、无效）
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionLabelZeroSPNumbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer treatmentAdviceNumber = (int) statConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        double avgLeftVision = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).average().orElse(0);
        double avgRightVision = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).average().orElse(0);
        int validScreeningNumbers = statConclusions.size();
        Integer type = school.getType();
        VisionAnalysis visionAnalysis = getVisionAnalysis(wearingGlassNumber, lowVisionNumber, treatmentAdviceNumber,
                avgLeftVision, avgRightVision, validScreeningNumbers,
                type,ametropiaNumber,myopiaNumber,visionLabelZeroSPNumbers,myopiaLevelMap);

        RescreenSituationDO rescreenSituation = new RescreenSituationDO();

        VisionWarningDO visionWarning = getVisionWarning(visionLabel0Numbers, visionLabel1Numbers, visionLabel2Numbers, visionLabel3Numbers, visionLabelZeroSPNumbers, keyWarningNumbers, validScreeningNumbers);

        statistic.setSchoolId(school.getId())
                .setSchoolType(type)
                .setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                .setScreeningPlanId(screeningPlan.getId())
                .setDistrictId(school.getDistrictId())
                .setVisionAnalysis(visionAnalysis)
                .setVisionWarning(visionWarning)
                .setValidScreeningNum(validScreeningNumbers)
                .setPlanScreeningNum(planScreeningNumbers)
                .setRealScreeningNum(realScreeningNumber);

        return statistic;
    }


    /**
     * 视力预警
     */
    private static VisionWarningDO getVisionWarning(Integer visionLabel0Numbers, Integer visionLabel1Numbers, Integer visionLabel2Numbers, Integer visionLabel3Numbers, Integer visionLabelZeroSPNumbers, Integer keyWarningNumbers, int validScreeningNumbers) {
        VisionWarningDO visionWarning = new VisionWarningDO();
        visionWarning.setVisionLabel0Num(visionLabel0Numbers + visionLabelZeroSPNumbers)
                .setVisionLabel0Ratio(ratio(visionLabel0Numbers + visionLabelZeroSPNumbers, validScreeningNumbers))
                .setVisionLabel1Num(visionLabel1Numbers)
                .setVisionLabel1Ratio(ratio(visionLabel1Numbers, validScreeningNumbers))
                .setVisionLabel2Num(visionLabel2Numbers)
                .setVisionLabel2Ratio(ratio(visionLabel2Numbers, validScreeningNumbers))
                .setVisionLabel3Num(visionLabel3Numbers)
                .setVisionLabel3Ratio(ratio(visionLabel3Numbers, validScreeningNumbers))
                .setVisionWarningNum(keyWarningNumbers);
        return visionWarning;
    }

    /**
     * 占比
     * @param numerator 分子
     * @param denominator 分母
     */
    public static String ratio(Integer numerator, Integer denominator){
        return MathUtil.divide(numerator, denominator).toString()+"%";
    }

    /**
     * 视力分析
     */
    private static VisionAnalysis getVisionAnalysis(Integer wearingGlassNumber, Integer lowVisionNumber,
                                                    Integer treatmentAdviceNumber, double avgLeftVision, double avgRightVision,
                                                    int validScreeningNumbers, Integer type,Integer ametropiaNumber,
                                                    Integer myopiaNumber,Integer visionLabelZeroSPNumbers,Map<Integer, Long> myopiaLevelMap) {

        if(Objects.equals(SchoolEnum.TYPE_KINDERGARTEN.getType(),type)){
            KindergartenVisionAnalysisDO visionAnalysis = new KindergartenVisionAnalysisDO();
            visionAnalysis.setLowVisionNum(lowVisionNumber)
                    .setLowVisionRatio(ratio(lowVisionNumber, validScreeningNumbers))
                    .setAmetropiaNum(ametropiaNumber)
                    .setAmetropiaRatio(ratio(ametropiaNumber, validScreeningNumbers))
                    .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision))
                    .setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                    .setWearingGlassesNum(wearingGlassNumber)
                    .setWearingGlassesRatio(ratio(wearingGlassNumber, validScreeningNumbers))
                    .setMyopiaLevelInsufficientNum(visionLabelZeroSPNumbers)
                    .setTreatmentAdviceNum(treatmentAdviceNumber)
                    .setTreatmentAdviceRatio(ratio(treatmentAdviceNumber, validScreeningNumbers));
            return visionAnalysis;
        }else {
            PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis= new PrimarySchoolAndAboveVisionAnalysisDO();
            visionAnalysis.setLowVisionNum(lowVisionNumber)
                    .setLowVisionRatio(ratio(lowVisionNumber, validScreeningNumbers))
                    .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision))
                    .setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                    .setWearingGlassesNum(wearingGlassNumber)
                    .setWearingGlassesRatio(ratio(wearingGlassNumber, validScreeningNumbers))
                    .setTreatmentAdviceNum(treatmentAdviceNumber)
                    .setTreatmentAdviceRatio(ratio(treatmentAdviceNumber, validScreeningNumbers))
                    .setMyopiaNum(myopiaNumber)
                    .setMyopiaRatio(ratio(myopiaNumber, validScreeningNumbers))
                    .setMyopiaLevelEarlyNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code,0L).intValue())
                    .setLowMyopiaNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code,0L).intValue())
                    .setHighMyopiaNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code,0L).intValue())
            ;

            return visionAnalysis;
        }
    }
}
