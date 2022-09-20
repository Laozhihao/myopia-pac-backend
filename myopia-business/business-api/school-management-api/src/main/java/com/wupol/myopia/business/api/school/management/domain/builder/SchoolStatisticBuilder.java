package com.wupol.myopia.business.api.school.management.domain.builder;

import com.wupol.myopia.business.api.school.management.domain.vo.KindergartenSchoolStatisticVO;
import com.wupol.myopia.business.api.school.management.domain.vo.PrimarySchoolAndAboveSchoolStatisticVO;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.PrimarySchoolAndAboveVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.VisionWarningDO;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * 学校统计构建
 *
 * @author hang.yuan 2022/9/19 17:42
 */
@UtilityClass
public class SchoolStatisticBuilder {

    /**
     * 构建幼儿园筛查数据结果
     * @param visionScreeningResultStatistic 视力筛查结果
     */
    public KindergartenSchoolStatisticVO buildKindergartenSchoolStatisticVO(VisionScreeningResultStatistic visionScreeningResultStatistic) {
        if (Objects.isNull(visionScreeningResultStatistic)){
            return null;
        }
        KindergartenSchoolStatisticVO kindergartenSchoolStatisticVO = new KindergartenSchoolStatisticVO();

        kindergartenSchoolStatisticVO.setPlanScreeningNum(visionScreeningResultStatistic.getPlanScreeningNum())
                .setRealScreeningNum(visionScreeningResultStatistic.getRealScreeningNum())
                .setFinishRatio(visionScreeningResultStatistic.getFinishRatio())
                .setValidScreeningNum(visionScreeningResultStatistic.getValidScreeningNum());

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

        return kindergartenSchoolStatisticVO;
    }


    /**
     * 构建小学及以上筛查数据结果
     * @param visionScreeningResultStatistic 视力筛查数据结果
     */
    public PrimarySchoolAndAboveSchoolStatisticVO buildPrimarySchoolAndAboveSchoolStatisticVO(VisionScreeningResultStatistic visionScreeningResultStatistic) {
        if (Objects.isNull(visionScreeningResultStatistic)){
            return null;
        }
        PrimarySchoolAndAboveSchoolStatisticVO primarySchoolAndAboveSchoolStatisticVO = new PrimarySchoolAndAboveSchoolStatisticVO();

        primarySchoolAndAboveSchoolStatisticVO.setPlanScreeningNum(visionScreeningResultStatistic.getPlanScreeningNum())
                .setRealScreeningNum(visionScreeningResultStatistic.getRealScreeningNum())
                .setFinishRatio(visionScreeningResultStatistic.getFinishRatio())
                .setValidScreeningNum(visionScreeningResultStatistic.getValidScreeningNum());

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


        return primarySchoolAndAboveSchoolStatisticVO;
    }
}
