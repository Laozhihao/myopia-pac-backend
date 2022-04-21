package com.wupol.myopia.business.core.screening.flow.util;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;


/**
 * @Author  钓猫的小鱼
 * @Date  2022/4/13 20:42
 * @Email: shuailong.wu@vistel.cn
 * @Des: 复测工具类
 */
@UtilityClass
public class ReScreenCardUtil {





    /**
     * 复测卡结果（同一个学生）
     * @param firstScreeningResult 初筛结果(第一次)
     * @param reScreenResult 复测结果
     * @param qualityControlName 质控员
     * @return 复测卡结果
     */
    public ReScreeningCardVO reScreenResultCard(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreenResult,
                                              String qualityControlName){
        ReScreeningCardVO reScreeningResultCard = new ReScreeningCardVO();
        VisionVO vision  = new VisionVO();
        //选取视力中左眼的戴镜类型
        vision.setGlassesType(EyeDataUtil.glassesType(reScreenResult));

        VisionResultVO visionResult = new VisionResultVO();
        visionResult.setRightEyeData(rightEyeData(firstScreeningResult, reScreenResult));
        visionResult.setLeftEyeData(leftEyeData(firstScreeningResult, reScreenResult));

        vision.setVisionResult(visionResult);
        vision.setComputerOptometryResult(computerOptometryResult(firstScreeningResult, reScreenResult));
        vision.setVisionOrOptometryDeviation(EyeDataUtil.optometryDeviation(reScreenResult));
        vision.setQualityControlName(qualityControlName);
        vision.setCreateTime(EyeDataUtil.createTime(reScreenResult));

        reScreeningResultCard.setVision(vision);

        CommonDiseasesVO commonDiseases = new CommonDiseasesVO();

        CommonDiseasesVO.HeightAndWeightResult heightAndWeightResult = new CommonDiseasesVO.HeightAndWeightResult();
        heightAndWeightResult.setHeight(EyeDataUtil.height(firstScreeningResult));
        heightAndWeightResult.setHeightReScreen(EyeDataUtil.height(reScreenResult));
        heightAndWeightResult.setHeightDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.height(firstScreeningResult),EyeDataUtil.height(reScreenResult)));
        heightAndWeightResult.setHeightDeviationRemark(EyeDataUtil.heightWeightDeviationRemark(reScreenResult));

        heightAndWeightResult.setWeight(EyeDataUtil.weight(firstScreeningResult));
        heightAndWeightResult.setWeightReScreen(EyeDataUtil.weight(reScreenResult));
        heightAndWeightResult.setWeightDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.weight(firstScreeningResult),EyeDataUtil.weight(reScreenResult)));
        heightAndWeightResult.setWeightDeviationRemark(EyeDataUtil.heightWeightDeviationRemark(reScreenResult));

        commonDiseases.setHeightAndWeightResult(heightAndWeightResult);
        commonDiseases.setQualityControlName(qualityControlName);
        commonDiseases.setCreateTime(EyeDataUtil.createTime(reScreenResult));

        reScreeningResultCard.setCommonDiseases(commonDiseases);
        return reScreeningResultCard;
    }
    /**
     * 电脑验光结果
     * @param firstScreenResult 初筛结果(第一次)
     * @param reScreenResult 复测结果
     * @return 电脑验光结果
     */
    public ComputerOptometryResultVO computerOptometryResult(VisionScreeningResult firstScreenResult, VisionScreeningResult reScreenResult) {
        ComputerOptometryResultVO computerOptometryResult = new ComputerOptometryResultVO();
        computerOptometryResult.setRightSE(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(firstScreenResult),EyeDataUtil.rightCyl(firstScreenResult)));

        computerOptometryResult.setRightSEReScreen(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(reScreenResult),EyeDataUtil.rightCyl(reScreenResult)));
        computerOptometryResult.setRightSEDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(firstScreenResult),
                EyeDataUtil.rightCyl(firstScreenResult)),EyeDataUtil.calculationSE(EyeDataUtil.rightSph(reScreenResult),EyeDataUtil.rightCyl(reScreenResult))));

        computerOptometryResult.setLeftSE(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(firstScreenResult),EyeDataUtil.leftCyl(firstScreenResult)));
        computerOptometryResult.setLeftSEScreening(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(reScreenResult),EyeDataUtil.leftCyl(reScreenResult)));
        computerOptometryResult.setLeftSEDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(firstScreenResult),EyeDataUtil.leftCyl(firstScreenResult)),
                EyeDataUtil.calculationSE(EyeDataUtil.leftSph(reScreenResult),EyeDataUtil.leftCyl(reScreenResult))));
        return computerOptometryResult;
    }

    /**
     * 左眼视力
     * @param firstScreenResult 初筛结果(第一次)
     * @param reScreenResult 复测结果
     * @return 左眼视力
     */
    public VisionResultVO.VisionData leftEyeData(VisionScreeningResult firstScreenResult, VisionScreeningResult reScreenResult) {
        VisionResultVO.VisionData leftEyeData = new  VisionResultVO.VisionData();
        leftEyeData.setNakedVision(EyeDataUtil.leftNakedVision(firstScreenResult));
        leftEyeData.setNakedVisionReScreen(EyeDataUtil.leftNakedVision(reScreenResult));
        leftEyeData.setNakedVisionDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.leftNakedVision(firstScreenResult),EyeDataUtil.leftNakedVision(reScreenResult)));
        leftEyeData.setCorrectedVision(EyeDataUtil.leftCorrectedVision(firstScreenResult));
        leftEyeData.setCorrectedVisionReScreen((EyeDataUtil.leftCorrectedVision(reScreenResult)));
        leftEyeData.setCorrectedVisionDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.leftCorrectedVision(firstScreenResult),EyeDataUtil.leftCorrectedVision(reScreenResult)));
        return leftEyeData;
    }

    /**
     * 右眼视力
     * @param firstScreenResult 初筛结果(第一次)
     * @param reScreenResult 复测结果
     * @return 右眼视力
     */
    public VisionResultVO.VisionData rightEyeData(VisionScreeningResult firstScreenResult, VisionScreeningResult reScreenResult) {
        VisionResultVO.VisionData rightEyeData = new  VisionResultVO.VisionData();
        rightEyeData.setNakedVision(EyeDataUtil.rightNakedVision(firstScreenResult));
        rightEyeData.setNakedVisionReScreen(EyeDataUtil.rightNakedVision(reScreenResult));
        rightEyeData.setNakedVisionDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.rightNakedVision(firstScreenResult),EyeDataUtil.rightNakedVision(reScreenResult)));
        rightEyeData.setCorrectedVision(EyeDataUtil.rightCorrectedVision(firstScreenResult));
        rightEyeData.setCorrectedVisionReScreen((EyeDataUtil.rightCorrectedVision(reScreenResult)));
        rightEyeData.setCorrectedVisionDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.rightCorrectedVision(firstScreenResult),EyeDataUtil.rightCorrectedVision(reScreenResult)));
        return rightEyeData;
    }
}

