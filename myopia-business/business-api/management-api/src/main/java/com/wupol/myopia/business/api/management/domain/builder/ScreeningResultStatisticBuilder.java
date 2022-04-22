package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查结果统计构造器
 *
 * @author hang.yuan 2022/4/13 19:27
 */
@UtilityClass
public class ScreeningResultStatisticBuilder {


    /**
     * 视力筛查数据统计
     */
    public  void visionScreening(StatisticResultBO totalStatistic,
                                  List<StatConclusion> statConclusions,
                                  List<VisionScreeningResultStatistic> visionScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int validScreeningNum = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(), SchoolEnum.TYPE_KINDERGARTEN.getType())){
            setKindergartenVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,statConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(statConclusions,isRescreenMap,statistic);

        visionScreeningResultStatisticList.add(statistic);

    }

    /**
     *  常见病筛查数据统计
     */
    public void commonDiseaseScreening(StatisticResultBO totalStatistic,
                                         List<StatConclusion> statConclusions,
                                         List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        Map<Boolean, List<StatConclusion>> isValidMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));

        //纳入统计数据
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(Boolean.TRUE, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        int validScreeningNum = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(),SchoolEnum.TYPE_KINDERGARTEN.getType())){
            setKindergartenVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, statConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,statConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(statConclusions,isRescreenMap,statistic);

        //设置龋齿数据
        setSaprodontia(statConclusions, realScreeningStudentNum, statistic);
        //设置常见病数据
        setCommonDisease(statConclusions, realScreeningStudentNum, statistic);
        //设置问卷调查数据
        setQuestionnaire(statistic);

        commonDiseaseScreeningResultStatisticList.add(statistic);

    }


    /**
     * 基础数据
     */
    private void setBasicData(List<StatConclusion> statConclusions,
                              StatisticResultBO totalStatistic,
                              Integer realScreeningStudentNum, Integer validScreeningNum,
                              VisionScreeningResultStatistic statistic) {
        Integer planScreeningNum = totalStatistic.getPlanStudentCount();
        int schoolNum = (int)statConclusions.stream().map(StatConclusion::getSchoolId).filter(Objects::nonNull).count();
        if (Objects.isNull(statistic.getId())){
            statistic.setCreateTime(new Date());
        }
        statistic.setScreeningNoticeId(totalStatistic.getScreeningNoticeId())
                .setScreeningTaskId(totalStatistic.getScreeningTaskId())
                .setScreeningPlanId(totalStatistic.getScreeningPlanId())
                .setScreeningType(totalStatistic.getScreeningType())
                .setIsTotal(totalStatistic.getIsTotal())
                .setSchoolId(totalStatistic.getSchoolId())
                .setScreeningOrgId(totalStatistic.getScreeningOrgId())
                .setSchoolType(totalStatistic.getSchoolType())
                .setDistrictId(totalStatistic.getDistrictId())
                .setSchoolNum(schoolNum)
                .setPlanScreeningNum(planScreeningNum)
                .setRealScreeningNum(realScreeningStudentNum)
                .setFinishRatio(MathUtil.ratio(realScreeningStudentNum,planScreeningNum))
                .setValidScreeningNum(validScreeningNum)
                .setValidScreeningRatio(MathUtil.ratio(validScreeningNum,realScreeningStudentNum));
    }

    /**
     * 设置小学及以上视力分析数据
     */
    private void setPrimarySchoolAndAboveVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysisDO = new PrimarySchoolAndAboveVisionAnalysisDO();
        Integer lowVisionNum = (int) statConclusions.stream().map(StatConclusion::getIsLowVision).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        TwoTuple<BigDecimal, BigDecimal> tuple = StatUtil.calculateAverageVision(statConclusions);
        int myopiaNum = (int) statConclusions.stream().map(StatConclusion::getIsMyopia).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int myopiaLevelEarlyNum = (int) statConclusions.stream().filter(sc->Objects.equals(2,sc.getMyopiaLevel())).count();
        int lowMyopiaNum = (int) statConclusions.stream().filter(sc->Objects.equals(3,sc.getMyopiaLevel())).count();
        int highMyopiaNum = (int) statConclusions.stream().filter(sc->Objects.equals(5,sc.getMyopiaLevel())).count();
        int astigmatismNum =(int) statConclusions.stream().map(StatConclusion::getIsAstigmatism).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer wearingGlassNum = (int) statConclusions.stream().filter(sc-> Objects.equals(Boolean.TRUE,sc.getIsWearingGlasses()) && Objects.equals(Boolean.TRUE,sc.getIsValid())).count();
        Integer nightWearingOrthokeratologyLensesNum = (int) statConclusions.stream().filter(sc-> Objects.equals(3,sc.getGlassesType())).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().map(StatConclusion::getIsRecommendVisit).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        visionAnalysisDO.setLowVisionNum(lowVisionNum)
                .setLowVisionRatio(MathUtil.ratio(lowVisionNum,validScreeningNum))
                .setAvgLeftVision(tuple.getFirst()).setAvgRightVision(tuple.getSecond())
                .setMyopiaNum(myopiaNum).setMyopiaRatio(MathUtil.ratio(myopiaNum,validScreeningNum))
                .setMyopiaLevelEarlyNum(myopiaLevelEarlyNum).setMyopiaLevelEarlyRatio(MathUtil.ratio(myopiaLevelEarlyNum,validScreeningNum))
                .setLowMyopiaNum(lowMyopiaNum).setLowMyopiaRatio(MathUtil.ratio(lowMyopiaNum,validScreeningNum))
                .setHighMyopiaNum(highMyopiaNum).setHighMyopiaRatio(MathUtil.ratio(highMyopiaNum,validScreeningNum))
                .setAstigmatismNum(astigmatismNum).setAstigmatismRatio(MathUtil.ratio(astigmatismNum,validScreeningNum))
                .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(MathUtil.ratio(wearingGlassNum,validScreeningNum))
                .setNightWearingOrthokeratologyLensesNum(nightWearingOrthokeratologyLensesNum).setNightWearingOrthokeratologyLensesRatio(MathUtil.ratio(nightWearingOrthokeratologyLensesNum,validScreeningNum))
                .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(MathUtil.ratio(treatmentAdviceNum,validScreeningNum))
                .setSchoolType(totalStatistic.getSchoolType());

        statistic.setVisionAnalysis(visionAnalysisDO);
    }

    /**
     * 设置幼儿园视力分析数据
     */
    private void setKindergartenVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        KindergartenVisionAnalysisDO visionAnalysisDO =new KindergartenVisionAnalysisDO();
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer lowVisionNum = (int) statConclusions.stream().map(StatConclusion::getIsLowVision).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer ametropiaNum = (int) statConclusions.stream().map(StatConclusion::getIsRefractiveError).filter(Boolean::booleanValue).count();
        Integer anisometropiaNum = (int) statConclusions.stream().map(StatConclusion::getIsAnisometropia).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer wearingGlassNum = (int) statConclusions.stream().map(StatConclusion::getIsWearingGlasses).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream().map(StatConclusion::getIsRecommendVisit).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        TwoTuple<BigDecimal, BigDecimal> tuple = StatUtil.calculateAverageVision(statConclusions);
        Integer visionLabelZeroSpNum = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L).intValue();
        visionAnalysisDO.setLowVisionNum(lowVisionNum)
                .setLowVisionRatio(MathUtil.ratio(lowVisionNum,validScreeningNum))
                .setAvgLeftVision(tuple.getFirst()).setAvgRightVision(tuple.getSecond())
                .setAmetropiaNum(ametropiaNum).setAmetropiaRatio(MathUtil.ratio(ametropiaNum,validScreeningNum))
                .setAnisometropiaNum(anisometropiaNum).setAnisometropiaRatio(MathUtil.ratio(anisometropiaNum,validScreeningNum))
                .setMyopiaLevelInsufficientNum(visionLabelZeroSpNum).setMyopiaLevelInsufficientRatio(MathUtil.ratio(visionLabelZeroSpNum,validScreeningNum))
                .setWearingGlassesNum(wearingGlassNum).setWearingGlassesRatio(MathUtil.ratio(wearingGlassNum,validScreeningNum))
                .setTreatmentAdviceNum(treatmentAdviceNum).setTreatmentAdviceRatio(MathUtil.ratio(treatmentAdviceNum,validScreeningNum))
                .setSchoolType(totalStatistic.getSchoolType());
        statistic.setVisionAnalysis(visionAnalysisDO);
    }


    /**
     * 设置视力预警数据
     */
    private void setVisionWarning(int validScreeningNum,
                                  List<StatConclusion> statConclusions,
                                  VisionScreeningResultStatistic statistic) {
        VisionWarningDO visionWarningDO = new VisionWarningDO();
        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Num = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Num = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Num = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Num = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionWarningNum =visionLabel0Num+visionLabel1Num+visionLabel2Num+visionLabel3Num;

        visionWarningDO.setVisionWarningNum(visionWarningNum)
                .setVisionLabel0Num(visionLabel0Num).setVisionLabel0Ratio(MathUtil.ratio(visionLabel0Num,validScreeningNum))
                .setVisionLabel1Num(visionLabel1Num).setVisionLabel1Ratio(MathUtil.ratio(visionLabel1Num,validScreeningNum))
                .setVisionLabel2Num(visionLabel2Num).setVisionLabel2Ratio(MathUtil.ratio(visionLabel2Num,validScreeningNum))
                .setVisionLabel3Num(visionLabel3Num).setVisionLabel3Ratio(MathUtil.ratio(visionLabel3Num,validScreeningNum));
        statistic.setVisionWarning(visionWarningDO);
    }

    /**
     * 设置复测数据
     */
    private RescreenSituationDO setRescreenSituation(List<StatConclusion> statConclusions, Map<Boolean, List<StatConclusion>> isRescreenMap,
                                                     VisionScreeningResultStatistic statistic) {
        RescreenSituationDO rescreenSituationDO = new RescreenSituationDO();
        Integer wearingGlassRescreenNum = (int) statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsRescreen()) && Objects.equals(Boolean.TRUE,sc.getIsWearingGlasses()) && Objects.equals(Boolean.TRUE,sc.getIsValid())).count();
        Integer withoutGlassRescreenNum = (int) statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsRescreen()) && Objects.equals(Boolean.FALSE,sc.getIsWearingGlasses()) && Objects.equals(Boolean.TRUE,sc.getIsValid())).count();
        Integer rescreenNum = (int) statConclusions.stream().map(StatConclusion::getIsRescreen).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int validRescreenNum = isRescreenMap.getOrDefault(Boolean.TRUE, Collections.emptyList()).size();
        rescreenSituationDO.setRetestNum(rescreenNum).setRetestRatio(MathUtil.ratio(rescreenNum, validRescreenNum))
                .setWearingGlassRetestNum(wearingGlassRescreenNum).setWearingGlassRetestRatio(MathUtil.ratio(wearingGlassRescreenNum, validRescreenNum))
                .setWithoutGlassRetestNum(withoutGlassRescreenNum).setWithoutGlassRetestRatio(MathUtil.ratio(wearingGlassRescreenNum, validRescreenNum))
                .setRescreeningItemNum(0).setErrorItemNum(0).setIncidence("0.00%");
        statistic.setRescreenSituation(rescreenSituationDO);
        return rescreenSituationDO;
    }

    /**
     * 设置龋齿数据
     */
    private void setSaprodontia(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        SaprodontiaDO saprodontiaDO = new SaprodontiaDO();
        int saprodontiaFreeNum = (int)statConclusions.stream().filter(sc -> Objects.equals(Boolean.FALSE,sc.getIsSaprodontia()) && Objects.equals(Boolean.FALSE,sc.getIsSaprodontiaLoss()) && Objects.equals(Boolean.FALSE,sc.getIsSaprodontiaRepair())).count();
        int saprodontiaNum = (int)statConclusions.stream().map(StatConclusion::getIsSaprodontia).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusions.stream().map(StatConclusion::getIsSaprodontiaLoss).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusions.stream().map(StatConclusion::getIsSaprodontiaRepair).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusions.stream().filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) && Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        int dmftNum = statConclusions.stream().filter(Objects::nonNull)
                .filter(sc -> Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) && Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair()))
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth()).sum();

        int sumTeeth = statConclusions.stream().filter(Objects::nonNull)
                .filter(sc -> Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) && Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair()) && Objects.equals(Boolean.TRUE,sc.getIsSaprodontia()))
                .mapToInt(sc -> sc.getSaprodontiaLossTeeth() + sc.getSaprodontiaRepairTeeth() + sc.getSaprodontiaTeeth()).sum();

        saprodontiaDO
                .setSaprodontiaFreeNum(saprodontiaFreeNum).setSaprodontiaFreeRatio(MathUtil.ratio(saprodontiaFreeNum,realScreeningStudentNum))
                .setDmftNum(dmftNum).setDmftRatio(MathUtil.ratio(dmftNum,realScreeningStudentNum))
                .setSaprodontiaNum(saprodontiaNum).setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,realScreeningStudentNum))
                .setSaprodontiaLossNum(saprodontiaLossNum).setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,realScreeningStudentNum))
                .setSaprodontiaRepairNum(saprodontiaRepairNum).setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum).setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairTeethNum(dmftNum).setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(dmftNum,sumTeeth));
        statistic.setSaprodontia(saprodontiaDO);
    }

    /**
     * 设置常见病数据
     */
    private void setCommonDisease(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        CommonDiseaseDO commonDiseaseDO = new CommonDiseaseDO();
        int overweightNum = (int)statConclusions.stream().map(StatConclusion::getIsOverweight).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int obeseNum = (int)statConclusions.stream().map(StatConclusion::getIsObesity).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int malnourishedNum = (int)statConclusions.stream().map(StatConclusion::getIsMalnutrition).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int stuntingNum = (int)statConclusions.stream().map(StatConclusion::getIsStunting).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int abnormalSpineCurvatureNum = (int)statConclusions.stream().map(StatConclusion::getIsSpinalCurvature).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int highBloodPressureNum = (int)statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();
        int reviewStudentNum = (int)statConclusions.stream().map(StatConclusion::getIsRescreen).filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        commonDiseaseDO.setOverweightNum(overweightNum).setOverweightRatio(MathUtil.ratio(overweightNum,realScreeningStudentNum))
                .setObeseNum(obeseNum).setObeseRatio(MathUtil.ratio(obeseNum,realScreeningStudentNum))
                .setMalnourishedNum(malnourishedNum).setMalnourishedRatio(MathUtil.ratio(malnourishedNum,realScreeningStudentNum))
                .setStuntingNum(stuntingNum).setStuntingRatio(MathUtil.ratio(stuntingNum,realScreeningStudentNum))
                .setAbnormalSpineCurvatureNum(abnormalSpineCurvatureNum).setAbnormalSpineCurvatureRatio(MathUtil.ratio(abnormalSpineCurvatureNum,realScreeningStudentNum))
                .setHighBloodPressureNum(highBloodPressureNum).setHighBloodPressureRatio(MathUtil.ratio(highBloodPressureNum,realScreeningStudentNum))
                .setReviewStudentNum(reviewStudentNum).setReviewStudentRatio(MathUtil.ratio(reviewStudentNum,realScreeningStudentNum));

        statistic.setCommonDisease(commonDiseaseDO);
    }
    /**
     * 设置问卷调查数据
     */
    private void setQuestionnaire(CommonDiseaseScreeningResultStatistic statistic) {
        QuestionnaireDO questionnaireDO = new QuestionnaireDO();
        Integer num=0;
        String ratio="0.00%";
        questionnaireDO.setEnvHealthInfluenceQuestionnaireNum(num)
                .setEnvHealthInfluenceQuestionnaireRatio(ratio)
                .setSchoolHealthWorkAdministrativeQuestionnaireNum(num)
                .setSchoolHealthWorkAdministrativeQuestionnaireRatio(ratio)
                .setSchoolHealthWorkQuestionnaireNum(num)
                .setSchoolHealthWorkQuestionnaireRatio(ratio)
                .setPoorVisionAndAbnormalCurvatureSpineQuestionnaireNum(num)
                .setPoorVisionAndAbnormalCurvatureSpineQuestionnaireRatio(ratio)
                .setHealthStateQuestionnaireNum(num)
                .setHealthStateQuestionnaireRatio(ratio);

        statistic.setQuestionnaire(questionnaireDO);
    }

}
