package com.wupol.myopia.business.core.screening.flow.util;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.GlassesTypeEnum;
import com.wupol.myopia.business.core.screening.flow.constant.ReScreenConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ReScreenDTO;
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


    /**
     * 设置复测
     * @param result 初测数据
     * @param reScreening 复测数据
     * @return 复测结果
     */
    public ReScreenDTO reScreeningVOResult(VisionScreeningResult result, VisionScreeningResult reScreening) {
        int deviationCount =0;

        ReScreenDTO rescreening = new ReScreenDTO();

        ReScreenDTO.ReScreeningResult reScreeningResult = new ReScreenDTO.ReScreeningResult();
        //戴镜情况
        reScreeningResult.setGlassesTypeDesc(GlassesTypeEnum.getDescByCode(EyeDataUtil.glassesType(reScreening)));

        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightNakedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightNakedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.rightNakedVision(result),
                EyeDataUtil.rightNakedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightNakedVisionType);

        rightNakedVision.setType(rightNakedVisionType);
        rightNakedVision.setContent(EyeDataUtil.rightNakedVision(reScreening));
        reScreeningResult.setRightNakedVision(rightNakedVision);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftNakedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();

        boolean leftNakedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.leftNakedVision(result),
                EyeDataUtil.leftNakedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftNakedVisionType);

        leftNakedVision.setType(leftNakedVisionType);
        leftNakedVision.setContent(EyeDataUtil.leftNakedVision(reScreening));
        reScreeningResult.setLeftNakedVision(leftNakedVision);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightCorrectedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightCorrectedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.rightCorrectedVision(result),
                EyeDataUtil.rightCorrectedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightCorrectedVisionType);

        rightCorrectedVision.setType(rightCorrectedVisionType);
        rightCorrectedVision.setContent(EyeDataUtil.rightCorrectedVision(reScreening));
        reScreeningResult.setRightCorrectedVision(rightCorrectedVision);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftCorrectedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftCorrectedVisionType =BigDecimalUtil.isDeviation(EyeDataUtil.leftCorrectedVision(result),
                EyeDataUtil.leftCorrectedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftCorrectedVisionType);

        leftCorrectedVision.setType(leftCorrectedVisionType);
        leftCorrectedVision.setContent(EyeDataUtil.leftCorrectedVision(reScreening));
        reScreeningResult.setLeftCorrectedVision(leftCorrectedVision);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightSph = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightSphType = BigDecimalUtil.isDeviation(EyeDataUtil.rightSph(result),
                EyeDataUtil.rightSph(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightSphType);

        rightSph.setType(rightSphType);
        rightSph.setContent(EyeDataUtil.rightSph(reScreening));
        reScreeningResult.setRightSph(rightSph);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightCyl = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightCylType = BigDecimalUtil.isDeviation(EyeDataUtil.rightCyl(result),
                EyeDataUtil.rightCyl(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightCylType);

        rightCyl.setType(rightCylType);
        rightCyl.setContent(EyeDataUtil.rightCyl(reScreening));
        reScreeningResult.setRightCyl(rightCyl);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightAxial = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightAxialType = BigDecimalUtil.isDeviation(EyeDataUtil.rightAxial(result),
                EyeDataUtil.rightAxial(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightAxialType);

        rightAxial.setType(rightAxialType);
        rightAxial.setContent(EyeDataUtil.rightAxial(reScreening));
        reScreeningResult.setRightAxial(rightAxial);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftSph = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftSphType = BigDecimalUtil.isDeviation(EyeDataUtil.leftSph(result),
                EyeDataUtil.leftSph(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftSphType);

        leftSph.setType(leftSphType);
        leftSph.setContent(EyeDataUtil.leftSph(reScreening));
        reScreeningResult.setLeftSph(leftSph);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftCyl = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftCylType = BigDecimalUtil.isDeviation(EyeDataUtil.leftCyl(result),
                EyeDataUtil.leftCyl(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftCylType);

        leftCorrectedVision.setType(leftCylType);
        leftCorrectedVision.setContent(EyeDataUtil.leftCyl(reScreening));
        reScreeningResult.setLeftCyl(leftCyl);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftAxial = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftAxialType = BigDecimalUtil.isDeviation(EyeDataUtil.leftAxial(result),
                EyeDataUtil.leftAxial(reScreening),new BigDecimal(ReScreenConstant.COMPUTEROPTOMETRY_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftAxialType);

        leftAxial.setType(leftAxialType);
        leftAxial.setContent(EyeDataUtil.leftAxial(reScreening));
        reScreeningResult.setLeftAxial(leftAxial);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation height = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean heightType = BigDecimalUtil.isDeviation(EyeDataUtil.height(result),
                EyeDataUtil.height(reScreening),new BigDecimal(ReScreenConstant.HEIGHT_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, heightType);

        height.setType(heightType);
        height.setContent(EyeDataUtil.height(reScreening));
        reScreeningResult.setHeight(height);

        ReScreenDTO.ReScreeningResult.ScreeningDeviation weight = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean weightType = BigDecimalUtil.isDeviation(EyeDataUtil.weight(result),
                EyeDataUtil.weight(reScreening), new BigDecimal(ReScreenConstant.WEIGHT_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, weightType);

        weight.setType(weightType);
        weight.setContent(EyeDataUtil.weight(reScreening));
        reScreeningResult.setWeight(weight);

        //次数检查数据为8项
        rescreening.setDoubleCount(ReScreenConstant.RESCREEN_NUM);
        rescreening.setDeviationCount(deviationCount);
        rescreening.setRescreeningResult(reScreeningResult);
        rescreening.setDeviation(EyeDataUtil.deviationData(reScreening));

        return rescreening;
    }
    /**
     *
     * @param deviationCount 错误项
     * @param type 1：错误 0 没有错误
     * @return
     */
    private int getDeviationCount(int deviationCount, boolean type) {
        if (type){
            deviationCount++;
        }
        return deviationCount;
    }
}

