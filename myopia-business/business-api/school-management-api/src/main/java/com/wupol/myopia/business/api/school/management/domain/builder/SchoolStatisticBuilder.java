package com.wupol.myopia.business.api.school.management.domain.builder;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.business.api.school.management.domain.vo.KindergartenSchoolStatisticVO;
import com.wupol.myopia.business.api.school.management.domain.vo.PrimarySchoolAndAboveSchoolStatisticVO;
import com.wupol.myopia.business.api.school.management.domain.vo.SchoolStatistic;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 学校统计构建
 *
 * @author hang.yuan 2022/9/19 17:42
 */
@UtilityClass
public class SchoolStatisticBuilder {

    private static final Integer ZERO = 0;
    private static final String ZERO_POINT = "0.0";
    private static final String ZERO_RATIO = "0.00%";
    private static final BigDecimal decimal = new BigDecimal(ZERO_POINT);

    /**
     * 构建幼儿园筛查数据结果
     * @param visionScreeningResultStatisticList 视力筛查结果集合
     * @param type 学校类型
     */
    public SchoolStatistic buildVisionScreeningSchoolStatisticVO(List<VisionScreeningResultStatistic> visionScreeningResultStatisticList, Integer type) {

        VisionScreeningResultStatistic visionScreeningResultStatistic = visionScreeningResultStatisticList.stream().filter(visionStatistic -> Objects.equals(visionStatistic.getSchoolType(), type)).findFirst().orElse(null);

        if (Objects.equals(SchoolEnum.TYPE_KINDERGARTEN.getType(),type)){
            return buildKindergartenSchoolStatisticVO(visionScreeningResultStatistic);
        }

        if (Objects.equals(SchoolEnum.TYPE_PRIMARY.getType(),type)){
            return buildPrimarySchoolAndAboveSchoolStatisticVO(visionScreeningResultStatistic);
        }
        return null;
    }

    /**
     * 构建幼儿园筛查结果
     * @param visionScreeningResultStatistic
     */
    private static KindergartenSchoolStatisticVO buildKindergartenSchoolStatisticVO(VisionScreeningResultStatistic visionScreeningResultStatistic) {

        KindergartenSchoolStatisticVO kindergartenSchoolStatisticVO = new KindergartenSchoolStatisticVO();
        if (Objects.isNull(visionScreeningResultStatistic)){
            visionScreeningResultStatistic = new VisionScreeningResultStatistic();
        }

        //视力筛查情况
        kindergartenSchoolStatisticVO.setPlanScreeningNum(getValueByInteger(visionScreeningResultStatistic.getPlanScreeningNum()))
                .setRealScreeningNum(getValueByInteger(visionScreeningResultStatistic.getRealScreeningNum()))
                .setFinishRatio(getValueByString(visionScreeningResultStatistic.getFinishRatio()))
                .setValidScreeningNum(getValueByInteger(visionScreeningResultStatistic.getValidScreeningNum()));

        //视力情况统计
        KindergartenVisionAnalysisDO visionAnalysis = (KindergartenVisionAnalysisDO)visionScreeningResultStatistic.getVisionAnalysis();
        if (Objects.isNull(visionAnalysis)){
            visionAnalysis = new KindergartenVisionAnalysisDO();
        }
        kindergartenSchoolStatisticVO.setLowVisionNum(getValueByInteger(visionAnalysis.getLowVisionNum()))
                .setLowVisionRatio(getValueByString(visionAnalysis.getLowVisionRatio()))
                .setAvgLeftVision(getValueByBigDecimal(visionAnalysis.getAvgLeftVision()))
                .setAvgRightVision(getValueByBigDecimal(visionAnalysis.getAvgRightVision()))
                .setMyopiaLevelInsufficientNum(getValueByInteger(visionAnalysis.getMyopiaLevelInsufficientNum()))
                .setMyopiaLevelInsufficientRatio(getValueByString(visionAnalysis.getMyopiaLevelInsufficientRatio()))
                .setAnisometropiaNum(getValueByInteger(visionAnalysis.getAnisometropiaNum()))
                .setAnisometropiaRatio(getValueByString(visionAnalysis.getAnisometropiaRatio()))
                .setAmetropiaNum(getValueByInteger(visionAnalysis.getAmetropiaNum()))
                .setAmetropiaRatio(getValueByString(visionAnalysis.getAmetropiaRatio()))
                .setTreatmentAdviceNum(getValueByInteger(visionAnalysis.getTreatmentAdviceNum()))
                .setTreatmentAdviceRatio(getValueByString(visionAnalysis.getTreatmentAdviceRatio()));

        //视力监测预警
        VisionWarningDO visionWarning = visionScreeningResultStatistic.getVisionWarning();
        if (Objects.isNull(visionWarning)){
            visionWarning = new VisionWarningDO();
        }
        kindergartenSchoolStatisticVO.setVisionWarningNum(getValueByInteger(visionWarning.getVisionWarningNum()))
                .setVisionWarningRatio(getValueByString(visionWarning.getVisionWarningRatio()))
                .setVisionLabel0Num(getValueByInteger(visionWarning.getVisionLabel0Num()))
                .setVisionLabel1Num(getValueByInteger(visionWarning.getVisionLabel1Num()))
                .setVisionLabel2Num(getValueByInteger(visionWarning.getVisionLabel2Num()))
                .setVisionLabel3Num(getValueByInteger(visionWarning.getVisionLabel3Num()))
                .setVisionLabel0Ratio(getValueByString(visionWarning.getVisionLabel0Ratio()))
                .setVisionLabel1Ratio(getValueByString(visionWarning.getVisionLabel1Ratio()))
                .setVisionLabel2Ratio(getValueByString(visionWarning.getVisionLabel2Ratio()))
                .setVisionLabel3Ratio(getValueByString(visionWarning.getVisionLabel3Ratio()));

        //视力异常跟踪
        kindergartenSchoolStatisticVO.setBindMpNum(getValueByInteger(visionScreeningResultStatistic.getBindMpNum()))
                .setBindMpRatio(getValueByString(visionScreeningResultStatistic.getBindMpRatio()))
                .setReviewNum(getValueByInteger(visionScreeningResultStatistic.getReviewNum()))
                .setReviewRatio(getValueByString(visionScreeningResultStatistic.getReviewRatio()));

        return kindergartenSchoolStatisticVO;
    }



    /**
     * 构建小学及以上筛查数据结果
     * @param visionScreeningResultStatistic 视力筛查数据结果
     */
    private PrimarySchoolAndAboveSchoolStatisticVO buildPrimarySchoolAndAboveSchoolStatisticVO(VisionScreeningResultStatistic visionScreeningResultStatistic) {
        PrimarySchoolAndAboveSchoolStatisticVO primarySchoolAndAboveSchoolStatisticVO = new PrimarySchoolAndAboveSchoolStatisticVO();
        if (Objects.isNull(visionScreeningResultStatistic)){
            visionScreeningResultStatistic = new CommonDiseaseScreeningResultStatistic();
        }

        //视力筛查情况
        primarySchoolAndAboveSchoolStatisticVO.setPlanScreeningNum(getValueByInteger(visionScreeningResultStatistic.getPlanScreeningNum()))
                .setRealScreeningNum(getValueByInteger(visionScreeningResultStatistic.getRealScreeningNum()))
                .setFinishRatio(getValueByString(visionScreeningResultStatistic.getFinishRatio()))
                .setValidScreeningNum(getValueByInteger(visionScreeningResultStatistic.getValidScreeningNum()));

        //视力情况统计
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis = (PrimarySchoolAndAboveVisionAnalysisDO)visionScreeningResultStatistic.getVisionAnalysis();
        if (Objects.isNull(visionAnalysis)){
            visionAnalysis = new PrimarySchoolAndAboveVisionAnalysisDO();
        }
        primarySchoolAndAboveSchoolStatisticVO.setLowVisionNum(getValueByInteger(visionAnalysis.getLowVisionNum()))
                .setLowVisionRatio(getValueByString(visionAnalysis.getLowVisionRatio()))
                .setAvgLeftVision(getValueByBigDecimal(visionAnalysis.getAvgLeftVision()))
                .setAvgRightVision(getValueByBigDecimal(visionAnalysis.getAvgRightVision()))
                .setWearingGlassesNum(getValueByInteger(visionAnalysis.getWearingGlassesNum()))
                .setWearingGlassesRatio(getValueByString(visionAnalysis.getWearingGlassesRatio()))
                .setNightWearingOrthokeratologyLensesNum(getValueByInteger(visionAnalysis.getNightWearingOrthokeratologyLensesNum()))
                .setNightWearingOrthokeratologyLensesRatio(getValueByString(visionAnalysis.getNightWearingOrthokeratologyLensesRatio()))
                .setMyopiaNum(getValueByInteger(visionAnalysis.getMyopiaNum()))
                .setMyopiaRatio(getValueByString(visionAnalysis.getMyopiaRatio()))
                .setMyopiaLevelEarlyNum(getValueByInteger(visionAnalysis.getMyopiaLevelEarlyNum()))
                .setMyopiaLevelEarlyRatio(getValueByString(visionAnalysis.getMyopiaLevelEarlyRatio()))
                .setLowMyopiaNum(getValueByInteger(visionAnalysis.getLowMyopiaNum()))
                .setLowMyopiaRatio(getValueByString(visionAnalysis.getLowMyopiaRatio()))
                .setHighMyopiaNum(getValueByInteger(visionAnalysis.getHighMyopiaNum()))
                .setHighMyopiaRatio(getValueByString(visionAnalysis.getHighMyopiaRatio()))
                .setTreatmentAdviceNum(getValueByInteger(visionAnalysis.getTreatmentAdviceNum()))
                .setTreatmentAdviceRatio(getValueByString(visionAnalysis.getTreatmentAdviceRatio()));

        //视力监测预警
        VisionWarningDO visionWarning = visionScreeningResultStatistic.getVisionWarning();
        if (Objects.isNull(visionWarning)){
            visionWarning = new VisionWarningDO();
        }
        primarySchoolAndAboveSchoolStatisticVO.setVisionWarningNum(getValueByInteger(visionWarning.getVisionWarningNum()))
                .setVisionWarningRatio(getValueByString(visionWarning.getVisionWarningRatio()))
                .setVisionLabel0Num(getValueByInteger(visionWarning.getVisionLabel0Num()))
                .setVisionLabel1Num(getValueByInteger(visionWarning.getVisionLabel1Num()))
                .setVisionLabel2Num(getValueByInteger(visionWarning.getVisionLabel2Num()))
                .setVisionLabel3Num(getValueByInteger(visionWarning.getVisionLabel3Num()))
                .setVisionLabel0Ratio(getValueByString(visionWarning.getVisionLabel0Ratio()))
                .setVisionLabel1Ratio(getValueByString(visionWarning.getVisionLabel1Ratio()))
                .setVisionLabel2Ratio(getValueByString(visionWarning.getVisionLabel2Ratio()))
                .setVisionLabel3Ratio(getValueByString(visionWarning.getVisionLabel3Ratio()));

        //视力异常跟踪
        primarySchoolAndAboveSchoolStatisticVO.setBindMpNum(getValueByInteger(visionScreeningResultStatistic.getBindMpNum()))
                .setBindMpRatio(getValueByString(visionScreeningResultStatistic.getBindMpRatio()))
                .setReviewNum(getValueByInteger(visionScreeningResultStatistic.getReviewNum()))
                .setReviewRatio(getValueByString(visionScreeningResultStatistic.getReviewRatio()));

        //常见病
        if (Objects.equals(visionScreeningResultStatistic.getScreeningType(), ScreeningTypeEnum.COMMON_DISEASE.getType())){
            CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic = (CommonDiseaseScreeningResultStatistic) visionScreeningResultStatistic;
            SaprodontiaDO saprodontia = commonDiseaseScreeningResultStatistic.getSaprodontia();
            if (Objects.isNull(saprodontia)){
                saprodontia = new SaprodontiaDO();
            }
            primarySchoolAndAboveSchoolStatisticVO.setDmftNum(getValueByInteger(saprodontia.getDmftNum()))
                    .setDmftRatio(getValueByString(saprodontia.getDmftRatio()))
                    .setSaprodontiaNum(getValueByInteger(saprodontia.getSaprodontiaNum()))
                    .setSaprodontiaRatio(getValueByString(saprodontia.getSaprodontiaRatio()))
                    .setSaprodontiaLossNum(getValueByInteger(saprodontia.getSaprodontiaLossNum()))
                    .setSaprodontiaLossRatio(getValueByString(saprodontia.getSaprodontiaLossRatio()))
                    .setSaprodontiaRepairNum(getValueByInteger(saprodontia.getSaprodontiaRepairNum()))
                    .setSaprodontiaRepairRatio(getValueByString(saprodontia.getSaprodontiaRepairRatio()))
                    .setSaprodontiaLossAndRepairNum(getValueByInteger(saprodontia.getSaprodontiaLossAndRepairNum()))
                    .setSaprodontiaLossAndRepairRatio(getValueByString(saprodontia.getSaprodontiaLossAndRepairRatio()))
                    .setSaprodontiaLossAndRepairTeethNum(getValueByInteger(saprodontia.getSaprodontiaLossAndRepairTeethNum()))
                    .setSaprodontiaLossAndRepairTeethRatio(getValueByString(saprodontia.getSaprodontiaLossAndRepairTeethRatio()));

            CommonDiseaseDO commonDisease = commonDiseaseScreeningResultStatistic.getCommonDisease();
            if (Objects.isNull(commonDisease)){
                commonDisease = new CommonDiseaseDO();
            }
            primarySchoolAndAboveSchoolStatisticVO.setAbnormalSpineCurvatureNum(getValueByInteger(commonDisease.getAbnormalSpineCurvatureNum()))
                    .setAbnormalSpineCurvatureRatio(getValueByString(commonDisease.getAbnormalSpineCurvatureRatio()))
                    .setHighBloodPressureNum(getValueByInteger(commonDisease.getHighBloodPressureNum()))
                    .setHighBloodPressureRatio(getValueByString(commonDisease.getHighBloodPressureRatio()))
                    .setOverweightNum(getValueByInteger(commonDisease.getOverweightNum()))
                    .setOverweightRatio(getValueByString(commonDisease.getOverweightRatio()))
                    .setObeseNum(getValueByInteger(commonDisease.getObeseNum()))
                    .setObeseRatio(getValueByString(commonDisease.getObeseRatio()))
                    .setMalnourishedNum(getValueByInteger(commonDisease.getMalnourishedNum()))
                    .setMalnourishedRatio(getValueByString(commonDisease.getMalnourishedRatio()))
                    .setStuntingNum(getValueByInteger(commonDisease.getStuntingNum()))
                    .setStuntingRatio(getValueByString(commonDisease.getStuntingRatio()));
        }else {
            primarySchoolAndAboveSchoolStatisticVO
                    .setDmftNum(ZERO).setDmftRatio(ZERO_POINT).setSaprodontiaNum(ZERO).setSaprodontiaRatio(ZERO_RATIO)
                    .setSaprodontiaLossNum(ZERO).setSaprodontiaLossRatio(ZERO_RATIO).setSaprodontiaRepairNum(ZERO).setSaprodontiaRepairRatio(ZERO_RATIO)
                    .setSaprodontiaLossAndRepairNum(ZERO).setSaprodontiaLossAndRepairRatio(ZERO_RATIO)
                    .setSaprodontiaLossAndRepairTeethNum(ZERO).setSaprodontiaLossAndRepairTeethRatio(ZERO_RATIO)
                    .setAbnormalSpineCurvatureNum(ZERO).setAbnormalSpineCurvatureRatio(ZERO_RATIO)
                    .setHighBloodPressureNum(ZERO).setHighBloodPressureRatio(ZERO_RATIO)
                    .setOverweightNum(ZERO).setOverweightRatio(ZERO_RATIO).setObeseNum(ZERO).setObeseRatio(ZERO_RATIO)
                    .setMalnourishedNum(ZERO).setMalnourishedRatio(ZERO_RATIO).setStuntingNum(ZERO).setStuntingRatio(ZERO_RATIO);
        }

        return primarySchoolAndAboveSchoolStatisticVO;
    }

    /**
     * 构建常见病筛查数据结果
     * @param commonDiseaseScreeningResultStatisticList 常见病筛查结果集合
     * @param type 学校类型
     */
    public SchoolStatistic buildCommonDiseaseScreeningResultStatisticVO(List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList, Integer type) {
        if (CollUtil.isEmpty(commonDiseaseScreeningResultStatisticList)){
            return null;
        }
        VisionScreeningResultStatistic visionScreeningResultStatistic = commonDiseaseScreeningResultStatisticList.stream().filter(visionStatistic -> Objects.equals(visionStatistic.getSchoolType(), type)).findFirst().orElse(null);

        if (Objects.isNull(visionScreeningResultStatistic)){
            return null;
        }

        if (Objects.equals(SchoolEnum.TYPE_KINDERGARTEN.getType(),type)){
            return buildKindergartenSchoolStatisticVO(visionScreeningResultStatistic);
        }

        if (Objects.equals(SchoolEnum.TYPE_PRIMARY.getType(),type)){
            return buildPrimarySchoolAndAboveSchoolStatisticVO(visionScreeningResultStatistic);
        }
        return null;

    }

    private Integer getValueByInteger(Integer value){
        return Optional.ofNullable(value).orElse(ZERO);
    }

    private String getValueByString(String value){
        return Optional.ofNullable(value).orElse(ZERO_RATIO);
    }

    private BigDecimal getValueByBigDecimal(BigDecimal value){
        return Optional.ofNullable(value).orElse(decimal);
    }
}
