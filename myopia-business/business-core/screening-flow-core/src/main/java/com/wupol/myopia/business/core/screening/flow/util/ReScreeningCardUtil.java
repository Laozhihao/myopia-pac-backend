package com.wupol.myopia.business.core.screening.flow.util;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.RescreenCardVO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;


/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/13 20:42
 * @Email: shuailong.wu@vistel.cn
 * @Des: 复测工具类
 */

/**
 * @author wushuailong
 */
@UtilityClass
public class ReScreeningCardUtil {

    /**
     * 视力是否误差
     * @param firstScreening 视力误差
     * @param reScreening 复测值
     * @param standard 标准值
     * @return
     */
    public int isDeviation(BigDecimal firstScreening,BigDecimal reScreening,BigDecimal standard){
        BigDecimal result = subtractAbsBigDecimal(firstScreening, reScreening);
        if (result.abs().compareTo(standard) == 1){
            return 1;
        }
        return 0;
    }

    /**
     * 绝对差值
     * @param firstScreening
     * @param reScreening
     * @return
     */
    private BigDecimal subtractAbsBigDecimal(BigDecimal firstScreening, BigDecimal reScreening) {
        BigDecimal first = null;
        if (firstScreening !=null){
            first = firstScreening;
        }else {
            first = new BigDecimal("0");
        }

        BigDecimal retest = null;
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
     * @return 复测卡工具类
     */
    public  RescreenCardVO retestResultCard(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult){
        RescreenCardVO retestResultCard = new RescreenCardVO();
        RescreenCardVO.Vision vision  = new RescreenCardVO.Vision();
        //选取视力中左眼的戴镜类型
        vision.setGlassesType(reScreeningResult.getVisionData().getLeftEyeData().getGlassesType());

        RescreenCardVO.Vision.VisionResult visionResult = new RescreenCardVO.Vision.VisionResult();
        visionResult.setRightEyeData(rightEyeData(firstScreeningResult, reScreeningResult));
        visionResult.setLeftEyeData(leftEyeData(firstScreeningResult, reScreeningResult));

        vision.setVisionResult(visionResult);
        vision.setComputerOptometryResult(computerOptometryResult(firstScreeningResult, reScreeningResult));
        vision.setVisionOrOptometryDeviation(reScreeningResult.getDeviationData().getVisionOrOptometryDeviation());
        vision.setQualityControlUser("质控人员");
        vision.setCreateTime(reScreeningResult.getCreateTime());

        retestResultCard.setVision(vision);

        RescreenCardVO.CommonDesease commonDesease = new RescreenCardVO.CommonDesease();

        RescreenCardVO.CommonDesease.HeightAndWeightResult heightAndWeightResult = new RescreenCardVO.CommonDesease.HeightAndWeightResult();
        heightAndWeightResult.setHeight(EyeDataUtil.height(firstScreeningResult));
        heightAndWeightResult.setHeightRescreen(EyeDataUtil.height(reScreeningResult));
        heightAndWeightResult.setHeightDeviation(subtractAbsBigDecimal(EyeDataUtil.height(firstScreeningResult),EyeDataUtil.height(reScreeningResult)));
        heightAndWeightResult.setHeightDeviationRemark(reScreeningResult.getDeviationData().getHeightWeightDeviation().getRemark());

        heightAndWeightResult.setWeight(EyeDataUtil.weight(firstScreeningResult));
        heightAndWeightResult.setWeightRescreen(EyeDataUtil.weight(reScreeningResult));
        heightAndWeightResult.setWeightDeviation(subtractAbsBigDecimal(EyeDataUtil.weight(firstScreeningResult),EyeDataUtil.weight(reScreeningResult)));
        heightAndWeightResult.setWeightDeviationRemark(reScreeningResult.getDeviationData().getHeightWeightDeviation().getRemark());

        commonDesease.setHeightAndWeightResult(heightAndWeightResult);
        commonDesease.setQualityControlUser("质控人员");
        commonDesease.setCreateTime(reScreeningResult.getCreateTime());

        retestResultCard.setCommonDesease(commonDesease);
        return retestResultCard;
    }

    private RescreenCardVO.Vision.ComputerOptometryResult computerOptometryResult(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        RescreenCardVO.Vision.ComputerOptometryResult computerOptometryResult = new RescreenCardVO.Vision.ComputerOptometryResult();
        BigDecimal rightSE = EyeDataUtil.calculationSE(EyeDataUtil.rightSph(firstScreeningResult),EyeDataUtil.rightCyl(firstScreeningResult));
        computerOptometryResult.setRightSE(rightSE);
        BigDecimal rightSERetest = EyeDataUtil.calculationSE(EyeDataUtil.rightSph(reScreeningResult),EyeDataUtil.rightCyl(reScreeningResult));
        computerOptometryResult.setRightSERetest(rightSERetest);
        computerOptometryResult.setRightSEDeviation(subtractAbsBigDecimal(rightSE,rightSERetest));

        BigDecimal leftSE = EyeDataUtil.calculationSE(EyeDataUtil.leftSph(firstScreeningResult),EyeDataUtil.leftCyl(firstScreeningResult));
        computerOptometryResult.setLeftSE(leftSE);
        BigDecimal leftSERetest = EyeDataUtil.calculationSE(EyeDataUtil.leftSph(reScreeningResult),EyeDataUtil.leftCyl(reScreeningResult));
        computerOptometryResult.setLeftSERetest(leftSERetest);
        computerOptometryResult.setLeftSEDeviation(subtractAbsBigDecimal(leftSE,leftSERetest));
        return computerOptometryResult;
    }

    /**
     * 左眼视力
     * @param firstScreeningResult
     * @param reScreeningResult
     * @return
     */
    private RescreenCardVO.Vision.VisionResult.VisionData leftEyeData(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        RescreenCardVO.Vision.VisionResult.VisionData leftEyeData = new  RescreenCardVO.Vision.VisionResult.VisionData();
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
     * @param firstScreeningResult
     * @param reScreeningResult
     * @return
     */
    private RescreenCardVO.Vision.VisionResult.VisionData rightEyeData(VisionScreeningResult firstScreeningResult, VisionScreeningResult reScreeningResult) {
        RescreenCardVO.Vision.VisionResult.VisionData rightEyeData = new  RescreenCardVO.Vision.VisionResult.VisionData();
        rightEyeData.setNakedVision(EyeDataUtil.rightNakedVision(firstScreeningResult));
        rightEyeData.setNakedVisionRetest(EyeDataUtil.rightNakedVision(reScreeningResult));
        rightEyeData.setNakedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.rightNakedVision(firstScreeningResult),EyeDataUtil.rightNakedVision(reScreeningResult)));
        rightEyeData.setCorrectedVision(EyeDataUtil.rightCorrectedVision(firstScreeningResult));
        rightEyeData.setCorrectedVisionRetest(EyeDataUtil.rightCorrectedVision(reScreeningResult));
        rightEyeData.setCorrectedVisionDeviation(subtractAbsBigDecimal(EyeDataUtil.rightCorrectedVision(firstScreeningResult),EyeDataUtil.rightCorrectedVision(reScreeningResult)));
        return rightEyeData;
    }
}

