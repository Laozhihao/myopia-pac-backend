package com.wupol.myopia.business.api.management.domain.builder;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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
     * 按区域 - 视力筛查数据统计
     */
    public static List<VisionScreeningResultStatistic> buildVisionScreening(Integer screeningNoticeId, Integer screeningTaskId,
                                                                            Integer districtId, Boolean isTotal, Integer planScreeningNum,
                                                                            List<StatConclusion> totalStatConclusions) {

        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList= Lists.newArrayList();
        //幼儿园
        List<StatConclusion> kindergarten = totalStatConclusions.stream().filter(sc -> Objects.equals(SchoolAge.KINDERGARTEN.code, sc.getSchoolAge())).collect(Collectors.toList());
        kindergartenVisionScreening(screeningNoticeId, screeningTaskId, districtId, isTotal, planScreeningNum, kindergarten,visionScreeningResultStatisticList);

        //小学及以上
        List<StatConclusion> primarySchoolAndAbove = totalStatConclusions.stream().filter(sc -> !Objects.equals(SchoolAge.KINDERGARTEN.code, sc.getSchoolAge())).collect(Collectors.toList());
        primarySchoolAndAboveVisionScreening(screeningNoticeId, screeningTaskId, districtId, isTotal, planScreeningNum, primarySchoolAndAbove,visionScreeningResultStatisticList);

        return visionScreeningResultStatisticList;
    }

    /**
     * 按区域 - 幼儿园视力筛查数据统计
     */
    private static void kindergartenVisionScreening(
            Integer screeningNoticeId, Integer screeningTaskId,
            Integer districtId, Boolean isTotal,Integer planScreeningNum,
            List<StatConclusion> totalStatConclusions,
            List<VisionScreeningResultStatistic> visionScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        statistic.setScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId)
                .setIsTotal(isTotal)
                .setFinishRatio(MathUtil.divide(realScreeningStudentNum, planScreeningNum))
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum);
        visionScreeningResultStatisticList.add(statistic);

    }

    /**
     * 按区域 - 小学及以上视力筛查数据统计
     */
    private static void primarySchoolAndAboveVisionScreening(Integer screeningNoticeId, Integer screeningTaskId,
                                                                                       Integer districtId, Boolean isTotal,Integer planScreeningNum,
                                                                                       List<StatConclusion> totalStatConclusions,
                                                                                       List<VisionScreeningResultStatistic> visionScreeningResultStatisticList){
        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        statistic.setScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId)
                .setIsTotal(isTotal)
                .setFinishRatio(MathUtil.divide(realScreeningStudentNum, planScreeningNum))
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum);
        visionScreeningResultStatisticList.add(statistic);
    }

    /**
     *  按区域 - 常见病筛查数据统计
     */
    public static CommonDiseaseScreeningResultStatistic buildCommonDiseaseScreening(Integer screeningNoticeId, Integer screeningTaskId,
                                                                                    Integer districtId, Boolean isTotal, Integer planScreeningNum,
                                                                                    List<StatConclusion> totalStatConclusions) {
        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));

        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        statistic.setScreeningNoticeId(screeningNoticeId)
                .setScreeningTaskId(screeningTaskId)
                .setDistrictId(districtId)
                .setIsTotal(isTotal)
                .setFinishRatio(MathUtil.divide(realScreeningStudentNum, planScreeningNum))
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum);
        return statistic;
    }


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
                .setVisionLabel0Ratio(MathUtil.divide(visionLabel0Numbers + visionLabelZeroSPNumbers, validScreeningNumbers))
                .setVisionLabel1Num(visionLabel1Numbers)
                .setVisionLabel1Ratio(MathUtil.divide(visionLabel1Numbers, validScreeningNumbers))
                .setVisionLabel2Num(visionLabel2Numbers)
                .setVisionLabel2Ratio(MathUtil.divide(visionLabel2Numbers, validScreeningNumbers))
                .setVisionLabel3Num(visionLabel3Numbers)
                .setVisionLabel3Ratio(MathUtil.divide(visionLabel3Numbers, validScreeningNumbers))
                .setVisionWarningNum(keyWarningNumbers);
        return visionWarning;
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
            visionAnalysis.setLowVisionNum(lowVisionNumber).setLowVisionRatio(MathUtil.divide(lowVisionNumber, validScreeningNumbers))
                    .setAmetropiaNum(ametropiaNumber)
                    .setAmetropiaRatio(MathUtil.divide(ametropiaNumber, validScreeningNumbers))
                    .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision))
                    .setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                    .setWearingGlassesNum(wearingGlassNumber)
                    .setWearingGlassesRatio(MathUtil.divide(wearingGlassNumber, validScreeningNumbers))
                    .setMyopiaLevelInsufficientNum(visionLabelZeroSPNumbers)
                    .setTreatmentAdviceNum(treatmentAdviceNumber)
                    .setTreatmentAdviceRatio(MathUtil.divide(treatmentAdviceNumber, validScreeningNumbers));
            return visionAnalysis;
        }else {
            PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis= new PrimarySchoolAndAboveVisionAnalysisDO();
            visionAnalysis.setLowVisionNum(lowVisionNumber)
                    .setLowVisionRatio(MathUtil.divide(lowVisionNumber, validScreeningNumbers))
                    .setAvgLeftVision(BigDecimal.valueOf(avgLeftVision))
                    .setAvgRightVision(BigDecimal.valueOf(avgRightVision))
                    .setWearingGlassesNum(wearingGlassNumber)
                    .setWearingGlassesRatio(MathUtil.divide(wearingGlassNumber, validScreeningNumbers))
                    .setTreatmentAdviceNum(treatmentAdviceNumber)
                    .setTreatmentAdviceRatio(MathUtil.divide(treatmentAdviceNumber, validScreeningNumbers))
                    .setMyopiaNum(myopiaNumber)
                    .setMyopiaRatio(MathUtil.divide(myopiaNumber, validScreeningNumbers))
                    .setMyopiaLevelEarlyNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code,0L).intValue())
                    .setLowMyopiaNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code,0L).intValue())
                    .setHighMyopiaNum(myopiaLevelMap.getOrDefault(MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code,0L).intValue())
            ;

            return visionAnalysis;
        }
    }
}
