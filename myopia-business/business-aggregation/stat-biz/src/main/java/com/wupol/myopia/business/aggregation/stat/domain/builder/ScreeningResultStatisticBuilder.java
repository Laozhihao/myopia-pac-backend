package com.wupol.myopia.business.aggregation.stat.domain.builder;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
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
    public void visionScreening(StatisticResultBO totalStatistic,
                                List<StatConclusion> statConclusions,
                                List<VisionScreeningResultStatistic> visionScreeningResultStatisticList){

        if (ObjectsUtil.hasNull(totalStatistic,statConclusions,visionScreeningResultStatisticList)){
            return;
        }

        //有效数据（初筛数据完整性判断）
        List<StatConclusion> validStatConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsValid())).collect(Collectors.toList());

        //纳入统计数据
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusionList = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList());
        int validScreeningNum = (int)validStatConclusionList.stream().filter(sc->Objects.equals(0,sc.getIsCooperative())).count();

        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        VisionScreeningResultStatistic statistic = new VisionScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置其它数据
        setOtherData(statConclusions, statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(), SchoolEnum.TYPE_KINDERGARTEN.getType())){
            setKindergartenVisionAnalysis(totalStatistic, validStatConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, validStatConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,validStatConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(validStatConclusions,isRescreenMap,statistic,totalStatistic.getScreeningType());

        visionScreeningResultStatisticList.add(statistic);

    }

    /**
     *  常见病筛查数据统计
     */
    public void commonDiseaseScreening(StatisticResultBO totalStatistic,
                                         List<StatConclusion> statConclusions,
                                         List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList){

        //有效数据（初筛数据完整性判断）
        List<StatConclusion> validStatConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsValid())).collect(Collectors.toList());

        //纳入统计数据
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusionList = isRescreenMap.getOrDefault(Boolean.FALSE, Collections.emptyList());
        int validScreeningNum = (int)validStatConclusionList.stream().filter(sc->Objects.equals(0,sc.getIsCooperative())).count();


        //复测数据
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        //实际筛查人数
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(Boolean.FALSE, Collections.emptyList()).size();


        CommonDiseaseScreeningResultStatistic statistic = new CommonDiseaseScreeningResultStatistic();
        //设置基础数据
        setBasicData(statConclusions,totalStatistic,realScreeningStudentNum,validScreeningNum,statistic);

        //设置视力分析数据
        if (Objects.equals(totalStatistic.getSchoolType(),SchoolEnum.TYPE_KINDERGARTEN.getType())){
            setKindergartenVisionAnalysis(totalStatistic, validStatConclusions, validScreeningNum, statistic);
        }else {
            setPrimarySchoolAndAboveVisionAnalysis(totalStatistic, validStatConclusions, validScreeningNum, statistic);
        }
        //设置视力预警数据
        setVisionWarning(validScreeningNum,validStatConclusions, statistic);
        //设置复测情况数据
        setRescreenSituation(validStatConclusions,isRescreenMap,statistic,totalStatistic.getScreeningType());

        //设置龋齿数据
        setSaprodontia(statConclusions, realScreeningStudentNum, statistic);
        //设置常见病数据
        setCommonDisease(statConclusions, realScreeningStudentNum, statistic);
        //设置问卷调查数据(暂时没数据)
        //setQuestionnaire(statistic);

        commonDiseaseScreeningResultStatisticList.add(statistic);

    }


    /**
     * 基础数据
     */
    private void setBasicData(List<StatConclusion> statConclusions,
                              StatisticResultBO totalStatistic,
                              Integer realScreeningStudentNum,
                              Integer validScreeningNum,
                              VisionScreeningResultStatistic statistic) {
        Integer planScreeningNum = totalStatistic.getPlanStudentCount();
        int schoolNum = (int)statConclusions.stream().map(StatConclusion::getSchoolId).filter(Objects::nonNull).distinct().count();
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
     * 设置小学及以上视力分析数据 (基于初筛数据)
     */
    private void setPrimarySchoolAndAboveVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysisDO = new PrimarySchoolAndAboveVisionAnalysisDO();
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsRescreen())).filter(sc->Objects.equals(0,sc.getIsCooperative())).collect(Collectors.toList());

        Integer lowVisionNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsLowVision)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        TwoTuple<BigDecimal, BigDecimal> tuple = StatUtil.calculateAverageVision(statConclusions);

        Integer myopiaNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsMyopia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        Integer myopiaLevelEarlyNum = (int) statConclusions.stream()
                .filter(sc->Objects.equals(2,sc.getMyopiaLevel())).count();
        Integer lowMyopiaNum = (int) statConclusions.stream()
                .filter(sc->Objects.equals(3,sc.getMyopiaLevel())).count();
        Integer highMyopiaNum = (int) statConclusions.stream()
                .filter(sc->Objects.equals(5,sc.getMyopiaLevel())).count();

        Integer astigmatismNum =(int) statConclusions.stream()
                .map(StatConclusion::getIsAstigmatism)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        Integer wearingGlassNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsWearingGlasses)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        Integer nightWearingOrthokeratologyLensesNum = (int) statConclusions.stream()
                .filter(sc-> Objects.equals(3,sc.getGlassesType())).count();

        Integer treatmentAdviceNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsRecommendVisit)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

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
     * 设置幼儿园视力分析数据(基于初筛数据)
     */
    private void setKindergartenVisionAnalysis(StatisticResultBO totalStatistic, List<StatConclusion> statConclusions, int validScreeningNum, VisionScreeningResultStatistic statistic) {
        KindergartenVisionAnalysisDO visionAnalysisDO =new KindergartenVisionAnalysisDO();
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsRescreen())).filter(sc->Objects.equals(0,sc.getIsCooperative())).collect(Collectors.toList());

        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));

        Integer lowVisionNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsLowVision)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer ametropiaNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsRefractiveError)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer anisometropiaNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsAnisometropia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer wearingGlassNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsWearingGlasses)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        Integer treatmentAdviceNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsRecommendVisit)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
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

    private static void setOtherData(List<StatConclusion> statConclusions, VisionScreeningResultStatistic statistic) {
        //公众号
        Integer bindMpNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsBindMp)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        statistic.setBindMpNum(bindMpNum).setBindMpRatio(MathUtil.ratio(bindMpNum,statistic.getRealScreeningNum()));

        //去医院
        Integer reviewNum = (int) statConclusions.stream().map(StatConclusion::getReportId).filter(Objects::nonNull).count();
        statistic.setReviewNum(reviewNum).setReviewRatio(MathUtil.ratio(reviewNum,statistic.getRealScreeningNum()));
    }


    /**
     * 设置视力预警数据 (基于初筛数据)
     */
    private void setVisionWarning(int validScreeningNum,
                                  List<StatConclusion> statConclusions,
                                  VisionScreeningResultStatistic statistic) {
        VisionWarningDO visionWarningDO = new VisionWarningDO();
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsRescreen())).filter(sc->Objects.equals(0,sc.getIsCooperative())).collect(Collectors.toList());

        Map<Integer, Long> visionLabelNumberMap = statConclusions.stream().filter(stat -> Objects.nonNull(stat.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        Integer visionLabel0Num = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Num = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Num = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Num = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer visionWarningNum =visionLabel0Num+visionLabel1Num+visionLabel2Num+visionLabel3Num;

        visionWarningDO.setVisionWarningNum(visionWarningNum).setVisionWarningRatio(MathUtil.ratio(visionWarningNum,validScreeningNum))
                .setVisionLabel0Num(visionLabel0Num).setVisionLabel0Ratio(MathUtil.ratio(visionLabel0Num,validScreeningNum))
                .setVisionLabel1Num(visionLabel1Num).setVisionLabel1Ratio(MathUtil.ratio(visionLabel1Num,validScreeningNum))
                .setVisionLabel2Num(visionLabel2Num).setVisionLabel2Ratio(MathUtil.ratio(visionLabel2Num,validScreeningNum))
                .setVisionLabel3Num(visionLabel3Num).setVisionLabel3Ratio(MathUtil.ratio(visionLabel3Num,validScreeningNum));
        statistic.setVisionWarning(visionWarningDO);
    }

    /**
     * 设置复测数据 (基于复测数据)
     */
    private RescreenSituationDO setRescreenSituation(List<StatConclusion> statConclusions, Map<Boolean, List<StatConclusion>> isRescreenMap,
                                                     VisionScreeningResultStatistic statistic,Integer screeningType) {
        RescreenSituationDO rescreenSituationDO = new RescreenSituationDO();

        List<StatConclusion> statConclusionList = statConclusions.stream()
                .filter(sc->Objects.equals(Boolean.TRUE,sc.getIsRescreen())).filter(sc->Objects.equals(0,sc.getIsCooperative())).collect(Collectors.toList());

        //戴镜
        List<StatConclusion> wearingGlassList = statConclusionList.stream()
                .filter(sc -> Objects.equals(Boolean.TRUE, sc.getIsWearingGlasses())).collect(Collectors.toList());
        Integer wearingGlassRescreenNum = wearingGlassList.size();
        //戴镜复测项次数
        Integer wearingGlassRescreenItemNum =wearingGlassList.stream().map(StatConclusion::getRescreenItemNum).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        //非戴镜
        List<StatConclusion> withoutGlassList = statConclusionList.stream()
                .filter(sc -> Objects.equals(Boolean.FALSE, sc.getIsWearingGlasses())).collect(Collectors.toList());
        Integer withoutGlassRescreenNum =withoutGlassList.size();
        //非戴镜复测项次数
        Integer withoutGlassRescreenItemNum =withoutGlassList.stream().map(StatConclusion::getRescreenItemNum).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        Integer rescreenNum = (int) statConclusionList.stream().map(StatConclusion::getIsRescreen).filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        int validRescreenNum = isRescreenMap.getOrDefault(Boolean.TRUE, Collections.emptyList()).size();

        int errorItemNum = statConclusionList.stream()
                .map(StatConclusion::getRescreenErrorNum)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        int num;
        if (Objects.equals(screeningType,0)){
            num = wearingGlassRescreenNum*6+withoutGlassRescreenNum*4;
        }else {
            num = wearingGlassRescreenNum*8+withoutGlassRescreenNum*6;
        }

        String incidence = MathUtil.ratio(errorItemNum,num);

        rescreenSituationDO.setRetestNum(rescreenNum).setRetestRatio(MathUtil.ratio(rescreenNum, validRescreenNum))
                .setWearingGlassRetestNum(wearingGlassRescreenNum).setWearingGlassRetestRatio(MathUtil.ratio(wearingGlassRescreenNum, validRescreenNum))
                .setWithoutGlassRetestNum(withoutGlassRescreenNum).setWithoutGlassRetestRatio(MathUtil.ratio(withoutGlassRescreenNum, validRescreenNum))
                .setErrorItemNum(errorItemNum)
                .setRescreeningItemNum(wearingGlassRescreenItemNum+withoutGlassRescreenItemNum)
                .setIncidence(incidence);

        statistic.setRescreenSituation(rescreenSituationDO);
        return rescreenSituationDO;
    }

    /**
     * 设置龋齿数据 (基于初筛数据)
     */
    private void setSaprodontia(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        SaprodontiaDO saprodontiaDO = new SaprodontiaDO();
        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsRescreen())).collect(Collectors.toList());

        Predicate<StatConclusion> predicateFalse = sc -> !(Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia()));

        int saprodontiaFreeNum = (int)statConclusions.stream()
                .filter(predicateFalse).count();
        int saprodontiaNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusions.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();


        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

        int lossAndRepairTeethNum = statConclusions.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

        int dmftNum = statConclusions.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        saprodontiaDO
                .setSaprodontiaFreeNum(saprodontiaFreeNum).setSaprodontiaFreeRatio(MathUtil.ratio(saprodontiaFreeNum,realScreeningStudentNum))
                .setDmftNum(dmftNum).setDmftRatio(MathUtil.num(dmftNum,realScreeningStudentNum))
                .setSaprodontiaNum(saprodontiaNum).setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,realScreeningStudentNum))
                .setSaprodontiaLossNum(saprodontiaLossNum).setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,realScreeningStudentNum))
                .setSaprodontiaRepairNum(saprodontiaRepairNum).setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum).setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,realScreeningStudentNum))
                .setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum).setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        statistic.setSaprodontia(saprodontiaDO);
    }

    /**
     * 设置常见病数据 (基于初筛数据)
     */
    private void setCommonDisease(List<StatConclusion> statConclusions, Integer realScreeningStudentNum, CommonDiseaseScreeningResultStatistic statistic) {
        CommonDiseaseDO commonDiseaseDO = new CommonDiseaseDO();

        statConclusions = statConclusions.stream().filter(sc->Objects.equals(Boolean.FALSE,sc.getIsRescreen())).collect(Collectors.toList());
        int overweightNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsOverweight)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int obeseNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsObesity)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int malnourishedNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsMalnutrition)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int stuntingNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsStunting)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int abnormalSpineCurvatureNum = (int)statConclusions.stream()
                .map(StatConclusion::getIsSpinalCurvature)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int highBloodPressureNum = (int)statConclusions.stream()
                .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

        int reviewStudentNum = (int) statConclusions.stream()
                .map(StatConclusion::getIsReview)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

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
        String ratio= CommonConst.PERCENT_ZERO;
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

    public static Integer getKey(String schoolGradeCode){
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
        if (Objects.isNull(gradeCodeEnum)){
            throw new BusinessException(String.format("不存在年级编码:%s",schoolGradeCode));
        }
        if (Objects.equals(gradeCodeEnum.getType(), SchoolAge.KINDERGARTEN.code)) {
            return SchoolEnum.TYPE_KINDERGARTEN.getType();
        }else {
            return SchoolEnum.TYPE_PRIMARY.getType();
        }
    }

    public void screening(List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                           List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList,
                           StatisticResultBO statisticResultBO,
                           List<StatConclusion> schoolStatConclusionList) {

        Map<Integer,Integer> planSchoolStudentMap= Maps.newHashMap();
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = statisticResultBO.getPlanSchoolStudentList();
        if (CollUtil.isNotEmpty(planSchoolStudentList)){
            int kindergarten = (int)planSchoolStudentList.stream().filter(planSchoolStudent -> Objects.equals(planSchoolStudent.getGradeType(), SchoolAge.KINDERGARTEN.code)).count();
            int primary = (int)planSchoolStudentList.stream().filter(planSchoolStudent -> !Objects.equals(planSchoolStudent.getGradeType(), SchoolAge.KINDERGARTEN.code)).count();
            planSchoolStudentMap.put(SchoolEnum.TYPE_KINDERGARTEN.getType(),kindergarten);
            planSchoolStudentMap.put(SchoolEnum.TYPE_PRIMARY.getType(),primary);
        }


        Map<Integer, List<StatConclusion>> schoolMap = schoolStatConclusionList.stream().collect(Collectors.groupingBy(sc -> getKey(sc.getSchoolGradeCode())));

        schoolMap.forEach((schoolAge,list)->{
            statisticResultBO.setSchoolType(schoolAge);
            if (CollUtil.isNotEmpty(planSchoolStudentMap)){
                Integer planSchoolStudentCount = planSchoolStudentMap.get(schoolAge);
                statisticResultBO.setPlanStudentCount(planSchoolStudentCount);
            }
            if (Objects.nonNull(visionScreeningResultStatisticList)){
                visionScreening(statisticResultBO,list,visionScreeningResultStatisticList);
            }
            if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
                commonDiseaseScreening(statisticResultBO,list,commonDiseaseScreeningResultStatisticList);
            }
        });
    }

}
