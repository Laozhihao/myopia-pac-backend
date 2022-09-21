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
            return buildEmptyValue(kindergartenSchoolStatisticVO);
        }

        //视力筛查情况
        kindergartenSchoolStatisticVO.setPlanScreeningNum(visionScreeningResultStatistic.getPlanScreeningNum())
                .setRealScreeningNum(visionScreeningResultStatistic.getRealScreeningNum())
                .setFinishRatio(visionScreeningResultStatistic.getFinishRatio())
                .setValidScreeningNum(visionScreeningResultStatistic.getValidScreeningNum());

        //视力情况统计
        KindergartenVisionAnalysisDO visionAnalysis = (KindergartenVisionAnalysisDO)visionScreeningResultStatistic.getVisionAnalysis();
        kindergartenSchoolStatisticVO.setLowVisionNum(visionAnalysis.getLowVisionNum())
                .setLowVisionRatio(visionAnalysis.getLowVisionRatio())
                .setAvgLeftVision(visionAnalysis.getAvgLeftVision())
                .setAvgRightVision(visionAnalysis.getAvgRightVision())
                .setMyopiaLevelInsufficientNum(visionAnalysis.getMyopiaLevelInsufficientNum())
                .setMyopiaLevelInsufficientRatio(visionAnalysis.getMyopiaLevelInsufficientRatio())
                .setAnisometropiaNum(visionAnalysis.getAnisometropiaNum())
                .setAnisometropiaRatio(visionAnalysis.getAnisometropiaRatio())
                .setAmetropiaNum(visionAnalysis.getAmetropiaNum())
                .setAmetropiaRatio(visionAnalysis.getAmetropiaRatio())
                .setTreatmentAdviceNum(visionAnalysis.getTreatmentAdviceNum())
                .setTreatmentAdviceRatio(visionAnalysis.getTreatmentAdviceRatio());

        //视力监测预警
        VisionWarningDO visionWarning = visionScreeningResultStatistic.getVisionWarning();
        kindergartenSchoolStatisticVO.setVisionWarningNum(visionWarning.getVisionWarningNum())
                .setVisionWarningRatio(visionWarning.getVisionWarningRatio())
                .setVisionLabel0Num(visionWarning.getVisionLabel0Num())
                .setVisionLabel1Num(visionWarning.getVisionLabel1Num())
                .setVisionLabel2Num(visionWarning.getVisionLabel2Num())
                .setVisionLabel3Num(visionWarning.getVisionLabel3Num())
                .setVisionLabel0Ratio(visionWarning.getVisionLabel0Ratio())
                .setVisionLabel1Ratio(visionWarning.getVisionLabel1Ratio())
                .setVisionLabel2Ratio(visionWarning.getVisionLabel2Ratio())
                .setVisionLabel3Ratio(visionWarning.getVisionLabel3Ratio());

        //视力异常跟踪
        kindergartenSchoolStatisticVO.setBindMpNum(visionScreeningResultStatistic.getBindMpNum())
                .setBindMpRatio(visionScreeningResultStatistic.getBindMpRatio())
                .setReviewNum(visionScreeningResultStatistic.getReviewNum())
                .setReviewRatio(visionScreeningResultStatistic.getReviewRatio());

        return kindergartenSchoolStatisticVO;
    }

    private static KindergartenSchoolStatisticVO buildEmptyValue(KindergartenSchoolStatisticVO kindergartenSchoolStatisticVO) {
        return kindergartenSchoolStatisticVO
                .setPlanScreeningNum(ZERO).setRealScreeningNum(ZERO).setValidScreeningNum(ZERO).setFinishRatio(ZERO_RATIO)
                .setLowVisionNum(ZERO).setLowVisionRatio(ZERO_RATIO).setAvgLeftVision(new BigDecimal(ZERO_POINT)).setAvgRightVision(new BigDecimal(ZERO_POINT))
                .setMyopiaLevelInsufficientNum(ZERO).setMyopiaLevelInsufficientRatio(ZERO_RATIO).setAnisometropiaNum(ZERO).setAnisometropiaRatio(ZERO_RATIO)
                .setAmetropiaNum(ZERO).setAmetropiaRatio(ZERO_RATIO).setValidScreeningNum(ZERO);
    }


    /**
     * 构建小学及以上筛查数据结果
     * @param visionScreeningResultStatistic 视力筛查数据结果
     */
    private PrimarySchoolAndAboveSchoolStatisticVO buildPrimarySchoolAndAboveSchoolStatisticVO(VisionScreeningResultStatistic visionScreeningResultStatistic) {
        if (Objects.isNull(visionScreeningResultStatistic)){
            return null;
        }
        PrimarySchoolAndAboveSchoolStatisticVO primarySchoolAndAboveSchoolStatisticVO = new PrimarySchoolAndAboveSchoolStatisticVO();

        //视力筛查情况
        primarySchoolAndAboveSchoolStatisticVO.setPlanScreeningNum(visionScreeningResultStatistic.getPlanScreeningNum())
                .setRealScreeningNum(visionScreeningResultStatistic.getRealScreeningNum())
                .setFinishRatio(visionScreeningResultStatistic.getFinishRatio())
                .setValidScreeningNum(visionScreeningResultStatistic.getValidScreeningNum());

        //视力情况统计
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis = (PrimarySchoolAndAboveVisionAnalysisDO)visionScreeningResultStatistic.getVisionAnalysis();
        primarySchoolAndAboveSchoolStatisticVO.setLowVisionNum(visionAnalysis.getLowVisionNum())
                .setLowVisionRatio(visionAnalysis.getLowVisionRatio())
                .setAvgLeftVision(visionAnalysis.getAvgLeftVision())
                .setAvgRightVision(visionAnalysis.getAvgRightVision())
                .setWearingGlassesNum(visionAnalysis.getWearingGlassesNum())
                .setWearingGlassesRatio(visionAnalysis.getWearingGlassesRatio())
                .setNightWearingOrthokeratologyLensesNum(visionAnalysis.getNightWearingOrthokeratologyLensesNum())
                .setNightWearingOrthokeratologyLensesRatio(visionAnalysis.getNightWearingOrthokeratologyLensesRatio())
                .setMyopiaNum(visionAnalysis.getMyopiaNum())
                .setMyopiaRatio(visionAnalysis.getMyopiaRatio())
                .setMyopiaLevelEarlyNum(visionAnalysis.getMyopiaLevelEarlyNum())
                .setMyopiaLevelEarlyRatio(visionAnalysis.getMyopiaLevelEarlyRatio())
                .setLowMyopiaNum(visionAnalysis.getLowMyopiaNum())
                .setLowMyopiaRatio(visionAnalysis.getLowMyopiaRatio())
                .setHighMyopiaNum(visionAnalysis.getHighMyopiaNum())
                .setHighMyopiaRatio(visionAnalysis.getHighMyopiaRatio())
                .setTreatmentAdviceNum(visionAnalysis.getTreatmentAdviceNum())
                .setTreatmentAdviceRatio(visionAnalysis.getTreatmentAdviceRatio());

        //视力监测预警
        VisionWarningDO visionWarning = visionScreeningResultStatistic.getVisionWarning();
        primarySchoolAndAboveSchoolStatisticVO.setVisionWarningNum(visionWarning.getVisionWarningNum())
                .setVisionWarningRatio(visionWarning.getVisionWarningRatio())
                .setVisionLabel0Num(visionWarning.getVisionLabel0Num())
                .setVisionLabel1Num(visionWarning.getVisionLabel1Num())
                .setVisionLabel2Num(visionWarning.getVisionLabel2Num())
                .setVisionLabel3Num(visionWarning.getVisionLabel3Num())
                .setVisionLabel0Ratio(visionWarning.getVisionLabel0Ratio())
                .setVisionLabel1Ratio(visionWarning.getVisionLabel1Ratio())
                .setVisionLabel2Ratio(visionWarning.getVisionLabel2Ratio())
                .setVisionLabel3Ratio(visionWarning.getVisionLabel3Ratio());

        //视力异常跟踪
        primarySchoolAndAboveSchoolStatisticVO.setBindMpNum(visionScreeningResultStatistic.getBindMpNum())
                .setBindMpRatio(visionScreeningResultStatistic.getBindMpRatio())
                .setReviewNum(visionScreeningResultStatistic.getReviewNum())
                .setReviewRatio(visionScreeningResultStatistic.getReviewRatio());

        //常见病
        if (Objects.equals(visionScreeningResultStatistic.getScreeningType(), ScreeningTypeEnum.COMMON_DISEASE.getType())){
            CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic = (CommonDiseaseScreeningResultStatistic) visionScreeningResultStatistic;
            SaprodontiaDO saprodontia = commonDiseaseScreeningResultStatistic.getSaprodontia();
            primarySchoolAndAboveSchoolStatisticVO.setDmftNum(saprodontia.getDmftNum())
                    .setDmftRatio(saprodontia.getDmftRatio())
                    .setSaprodontiaNum(saprodontia.getSaprodontiaNum())
                    .setSaprodontiaRatio(saprodontia.getSaprodontiaRatio())
                    .setSaprodontiaLossNum(saprodontia.getSaprodontiaLossNum())
                    .setSaprodontiaLossRatio(saprodontia.getSaprodontiaLossRatio())
                    .setSaprodontiaRepairNum(saprodontia.getSaprodontiaRepairNum())
                    .setSaprodontiaRepairRatio(saprodontia.getSaprodontiaRepairRatio())
                    .setSaprodontiaLossAndRepairNum(saprodontia.getSaprodontiaLossAndRepairNum())
                    .setSaprodontiaLossAndRepairRatio(saprodontia.getSaprodontiaLossAndRepairRatio())
                    .setSaprodontiaLossAndRepairTeethNum(saprodontia.getSaprodontiaLossAndRepairTeethNum())
                    .setSaprodontiaLossAndRepairTeethRatio(saprodontia.getSaprodontiaLossAndRepairTeethRatio());
            CommonDiseaseDO commonDisease = commonDiseaseScreeningResultStatistic.getCommonDisease();
            primarySchoolAndAboveSchoolStatisticVO.setAbnormalSpineCurvatureNum(commonDisease.getAbnormalSpineCurvatureNum())
                    .setAbnormalSpineCurvatureRatio(commonDisease.getAbnormalSpineCurvatureRatio())
                    .setHighBloodPressureNum(commonDisease.getHighBloodPressureNum())
                    .setHighBloodPressureRatio(commonDisease.getHighBloodPressureRatio())
                    .setOverweightNum(commonDisease.getOverweightNum())
                    .setOverweightRatio(commonDisease.getOverweightRatio())
                    .setObeseNum(commonDisease.getObeseNum())
                    .setObeseRatio(commonDisease.getObeseRatio())
                    .setMalnourishedNum(commonDisease.getMalnourishedNum())
                    .setMalnourishedRatio(commonDisease.getMalnourishedRatio())
                    .setStuntingNum(commonDisease.getStuntingNum())
                    .setStuntingRatio(commonDisease.getStuntingRatio());
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
}
