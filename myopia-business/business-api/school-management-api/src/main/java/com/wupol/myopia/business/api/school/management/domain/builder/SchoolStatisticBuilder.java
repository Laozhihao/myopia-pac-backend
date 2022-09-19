package com.wupol.myopia.business.api.school.management.domain.builder;

import com.wupol.myopia.business.aggregation.stat.domain.vo.KindergartenResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.PrimarySchoolAndAboveResultDetailVO;
import com.wupol.myopia.business.api.school.management.domain.vo.KindergartenSchoolStatisticVO;
import com.wupol.myopia.business.api.school.management.domain.vo.PrimarySchoolAndAboveSchoolStatisticVO;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.PrimarySchoolAndAboveVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.ScreeningSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.VisionWarningDO;
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
     * @param kindergartenResultDetail 幼儿园筛查结果
     */
    public KindergartenSchoolStatisticVO buildKindergartenSchoolStatisticVO(KindergartenResultDetailVO kindergartenResultDetail) {
        if (Objects.isNull(kindergartenResultDetail)){
            return null;
        }
        KindergartenSchoolStatisticVO kindergartenSchoolStatisticVO = new KindergartenSchoolStatisticVO();

        ScreeningSituationDO screeningSituation = kindergartenResultDetail.getScreeningSituation();
        kindergartenSchoolStatisticVO.setPlanScreeningNum(screeningSituation.getPlanScreeningNum())
                .setRealScreeningNum(screeningSituation.getRealScreeningNum())
                .setFinishRatio(screeningSituation.getFinishRatio())
                .setValidScreeningNum(screeningSituation.getValidScreeningNum());

        KindergartenVisionAnalysisDO visionAnalysis = kindergartenResultDetail.getKindergartenVisionAnalysis();
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


        return kindergartenSchoolStatisticVO;
    }


    /**
     * 构建小学及以上筛查数据结果
     * @param primarySchoolAndAboveResultDetail 小学及以上筛查数据结果
     */
    public PrimarySchoolAndAboveSchoolStatisticVO buildPrimarySchoolAndAboveSchoolStatisticVO(PrimarySchoolAndAboveResultDetailVO primarySchoolAndAboveResultDetail) {
        if (Objects.isNull(primarySchoolAndAboveResultDetail)){
            return null;
        }
        PrimarySchoolAndAboveSchoolStatisticVO primarySchoolAndAboveSchoolStatisticVO = new PrimarySchoolAndAboveSchoolStatisticVO();

        ScreeningSituationDO screeningSituation = primarySchoolAndAboveResultDetail.getScreeningSituation();
        primarySchoolAndAboveSchoolStatisticVO.setPlanScreeningNum(screeningSituation.getPlanScreeningNum())
                .setRealScreeningNum(screeningSituation.getRealScreeningNum())
                .setFinishRatio(screeningSituation.getFinishRatio())
                .setValidScreeningNum(screeningSituation.getValidScreeningNum());

        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis = primarySchoolAndAboveResultDetail.getPrimarySchoolAndAboveVisionAnalysis();
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


        VisionWarningDO visionWarning = primarySchoolAndAboveResultDetail.getVisionWarning();
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
