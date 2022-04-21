package com.wupol.myopia.business.core.screening.flow.util;
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
public class ReScreeningCardUtil {

    /**
     * 视力是否误差
     * @param firstScreening 视力误差
     * @param reScreening 复测值
     * @param standard 标准值
     * @return 1：误差 0：没误差
     */
    public int isDeviation(BigDecimal firstScreening,BigDecimal reScreening,BigDecimal standard){
        BigDecimal result = subtractAbsBigDecimal(firstScreening, reScreening);
        if (result.abs().compareTo(standard) > 0){
            return 1;
        }
        return 0;
    }

    /**
     * 绝对差值
     * @param firstScreening 初测值
     * @param reScreening 复测值
     * @return 绝对差值
     */
    public BigDecimal subtractAbsBigDecimal(BigDecimal firstScreening, BigDecimal reScreening) {
        BigDecimal first;
        if (firstScreening !=null){
            first = firstScreening;
        }else {
            first = new BigDecimal("0");
        }

        BigDecimal retest;
        if (reScreening !=null){
            retest = reScreening;
        }else {
            retest = new BigDecimal("0");
        }

        return first.abs().subtract(retest.abs());
    }

    /**
     * 复测卡结果（同一个学生）
     * @param firstScreeningResult 初筛结果(第一次)
     * @param reScreeningResult 复测结果
     * @param qualityControlName 质控员
     * @return 复测卡结果
     */
    public ReScreeningCardVO retestResultCard(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult,
                                              String qualityControlName){
        ReScreeningCardVO retestResultCard = new ReScreeningCardVO();
        VisionVO vision  = new VisionVO();
        //选取视力中左眼的戴镜类型
        vision.setGlassesType(EyeDataUtil.glassesType(reScreeningResult));

        VisionResultVO visionResult = new VisionResultVO();
        visionResult.setRightEyeData(rightEyeData(firstScreeningResult, reScreeningResult));
        visionResult.setLeftEyeData(leftEyeData(firstScreeningResult, reScreeningResult));

        vision.setVisionResult(visionResult);
        vision.setComputerOptometryResult(computerOptometryResult(firstScreeningResult, reScreeningResult));
        vision.setVisionOrOptometryDeviation(EyeDataUtil.optometryDeviation(reScreeningResult));
        vision.setQualityControlName(qualityControlName);
        vision.setCreateTime(EyeDataUtil.createTime(reScreeningResult));

        retestResultCard.setVision(vision);

        CommonDiseasesVO commonDiseases = new CommonDiseasesVO();

        CommonDiseasesVO.HeightAndWeightResult heightAndWeightResult = new CommonDiseasesVO.HeightAndWeightResult();
        heightAndWeightResult.setHeight(EyeDataUtil.height(firstScreeningResult));
        heightAndWeightResult.setHeightReScreen(EyeDataUtil.height(reScreeningResult));
        heightAndWeightResult.setHeightDeviation(subtractAbsBigDecimal(EyeDataUtil.height(firstScreeningResult),EyeDataUtil.height(reScreeningResult)));
        heightAndWeightResult.setHeightDeviationRemark(EyeDataUtil.heightWeightDeviationRemark(reScreeningResult));

        heightAndWeightResult.setWeight(EyeDataUtil.weight(firstScreeningResult));
        heightAndWeightResult.setWeightReScreen(EyeDataUtil.weight(reScreeningResult));
        heightAndWeightResult.setWeightDeviation(subtractAbsBigDecimal(EyeDataUtil.weight(firstScreeningResult),EyeDataUtil.weight(reScreeningResult)));
        heightAndWeightResult.setWeightDeviationRemark(EyeDataUtil.heightWeightDeviationRemark(reScreeningResult));

        commonDiseases.setHeightAndWeightResult(heightAndWeightResult);
        commonDiseases.setQualityControlName(qualityControlName);
        commonDiseases.setCreateTime(EyeDataUtil.createTime(reScreeningResult));

        retestResultCard.setCommonDiseases(commonDiseases);
        return retestResultCard;
    }
    /**
     * 电脑验光结果
     * @param firstScreeningResult 初筛结果(第一次)
     * @param reScreeningResult 复测结果
     * @return 电脑验光结果
     */
    public ComputerOptometryResultVO computerOptometryResult(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        ComputerOptometryResultVO computerOptometryResult = new ComputerOptometryResultVO();
        computerOptometryResult.setRightSE(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(firstScreeningResult),EyeDataUtil.rightCyl(firstScreeningResult)));

        computerOptometryResult.setRightSERetest(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(reScreeningResult),EyeDataUtil.rightCyl(reScreeningResult)));
        computerOptometryResult.setRightSEDeviation(subtractAbsBigDecimal(EyeDataUtil.calculationSE(EyeDataUtil.rightSph(firstScreeningResult),
                EyeDataUtil.rightCyl(firstScreeningResult)),EyeDataUtil.calculationSE(EyeDataUtil.rightSph(reScreeningResult),EyeDataUtil.rightCyl(reScreeningResult))));

        computerOptometryResult.setLeftSE(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(firstScreeningResult),EyeDataUtil.leftCyl(firstScreeningResult)));
        computerOptometryResult.setLeftSERetest(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(reScreeningResult),EyeDataUtil.leftCyl(reScreeningResult)));
        computerOptometryResult.setLeftSEDeviation(subtractAbsBigDecimal(EyeDataUtil.calculationSE(EyeDataUtil.leftSph(firstScreeningResult),EyeDataUtil.leftCyl(firstScreeningResult)),
                EyeDataUtil.calculationSE(EyeDataUtil.leftSph(reScreeningResult),EyeDataUtil.leftCyl(reScreeningResult))));
        return computerOptometryResult;
    }

    /**
     * 左眼视力
     * @param firstScreeningResult 初筛结果(第一次)
     * @param reScreeningResult 复测结果
     * @return 左眼视力
     */
    public VisionResultVO.VisionData leftEyeData(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        VisionResultVO.VisionData leftEyeData = new  VisionResultVO.VisionData();
        leftEyeData.setNakedVision(EyeDataUtil.leftNakedVision(firstScreeningResult));
        leftEyeData.setNakedVisionRetest(EyeDataUtil.leftNakedVision(reScreeningResult));
        leftEyeData.setNakedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.leftNakedVision(firstScreeningResult),EyeDataUtil.leftNakedVision(reScreeningResult)));
        leftEyeData.setCorrectedVision(EyeDataUtil.leftCorrectedVision(firstScreeningResult));
        leftEyeData.setCorrectedVisionRetest(EyeDataUtil.leftCorrectedVision(reScreeningResult));
        leftEyeData.setCorrectedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.leftCorrectedVision(firstScreeningResult),EyeDataUtil.leftCorrectedVision(reScreeningResult)));
        return leftEyeData;
    }

    /**
     * 右眼视力
     * @param firstScreeningResult 初筛结果(第一次)
     * @param reScreeningResult 复测结果
     * @return 右眼视力
     */
    public VisionResultVO.VisionData rightEyeData(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        VisionResultVO.VisionData rightEyeData = new  VisionResultVO.VisionData();
        rightEyeData.setNakedVision(EyeDataUtil.rightNakedVision(firstScreeningResult));
        rightEyeData.setNakedVisionRetest(EyeDataUtil.rightNakedVision(reScreeningResult));
        rightEyeData.setNakedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.rightNakedVision(firstScreeningResult),EyeDataUtil.rightNakedVision(reScreeningResult)));
        rightEyeData.setCorrectedVision(EyeDataUtil.rightCorrectedVision(firstScreeningResult));
        rightEyeData.setCorrectedVisionRetest(EyeDataUtil.rightCorrectedVision(reScreeningResult));
        rightEyeData.setCorrectedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.rightCorrectedVision(firstScreeningResult),EyeDataUtil.rightCorrectedVision(reScreeningResult)));
        return rightEyeData;
    }
}

