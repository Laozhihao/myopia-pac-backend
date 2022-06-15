package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.GlassesTypeEnum;
import com.wupol.myopia.business.core.screening.flow.constant.ReScreenConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ReScreenDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;


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
     * @param firstScreenResult 初筛结果(第一次)
     * @param reScreenResult 复测结果
     * @param qualityControlName 质控员
     * @param commonDiseasesCode 常见病code
     * @return 复测卡结果
     */
    public ReScreeningCardVO reScreenResultCard(VisionScreeningResult firstScreenResult, VisionScreeningResult reScreenResult,
                                              String qualityControlName, String commonDiseasesCode){
        ReScreeningCardVO reScreeningResultCard = new ReScreeningCardVO();
        reScreeningResultCard.setCommonDiseasesCode(commonDiseasesCode);

        // 设置视力数据
        VisionVO vision  = new VisionVO();
        // 选取视力中左眼的戴镜类型
        vision.setGlassesType(EyeDataUtil.glassesType(reScreenResult));
        VisionResultVO visionResult = new VisionResultVO();
        visionResult.setRightEyeData(rightEyeData(firstScreenResult, reScreenResult));
        visionResult.setLeftEyeData(leftEyeData(firstScreenResult, reScreenResult));
        vision.setVisionResult(visionResult);
        vision.setComputerOptometryResult(computerOptometryResult(firstScreenResult, reScreenResult));
        vision.setVisionOrOptometryDeviation(EyeDataUtil.optometryDeviation(reScreenResult));
        vision.setQualityControlName(qualityControlName);
        vision.setUpdateTime(EyeDataUtil.updateTime(reScreenResult));
        reScreeningResultCard.setVision(vision);

        // 设置常见病数据
        CommonDiseasesVO commonDiseases = new CommonDiseasesVO();
        CommonDiseasesVO.HeightAndWeightResult heightAndWeightResult = new CommonDiseasesVO.HeightAndWeightResult();
        heightAndWeightResult.setHeight(EyeDataUtil.height(firstScreenResult));
        heightAndWeightResult.setHeightReScreen(EyeDataUtil.height(reScreenResult));
        heightAndWeightResult.setHeightDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.height(firstScreenResult),EyeDataUtil.height(reScreenResult)));
        heightAndWeightResult.setWeight(EyeDataUtil.weight(firstScreenResult));
        heightAndWeightResult.setWeightReScreen(EyeDataUtil.weight(reScreenResult));
        heightAndWeightResult.setWeightDeviation(BigDecimalUtil.subtractAbsBigDecimal(EyeDataUtil.weight(firstScreenResult),EyeDataUtil.weight(reScreenResult)));
        heightAndWeightResult.setHeightWeightDeviation(EyeDataUtil.heightWeightDeviationRemark(reScreenResult));
        commonDiseases.setHeightAndWeightResult(heightAndWeightResult);
        commonDiseases.setQualityControlName(qualityControlName);
        commonDiseases.setUpdateTime(EyeDataUtil.updateTime(reScreenResult));
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
        computerOptometryResult.setRightSE(EyeDataUtil.rightSE(firstScreenResult));
        computerOptometryResult.setRightSEReScreen(EyeDataUtil.rightSE(reScreenResult));
        computerOptometryResult.setRightSEDeviation(BigDecimalUtil.subtractAbsBigDecimal(computerOptometryResult.getRightSE(), computerOptometryResult.getRightSEReScreen()));

        computerOptometryResult.setLeftSE(EyeDataUtil.leftSE(firstScreenResult));
        computerOptometryResult.setLeftSEScreening(EyeDataUtil.leftSE(reScreenResult));
        computerOptometryResult.setLeftSEDeviation(BigDecimalUtil.subtractAbsBigDecimal(computerOptometryResult.getLeftSE(), computerOptometryResult.getLeftSEScreening()));
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
    public ReScreenDTO reScreeningResult(VisionScreeningResult result, VisionScreeningResult reScreening) {
        int deviationCount =0;

        ReScreenDTO rescreening = new ReScreenDTO();

        ReScreenDTO.ReScreeningResult reScreeningResult = new ReScreenDTO.ReScreeningResult();
        //戴镜情况
        reScreeningResult.setGlassesTypeDesc(GlassesTypeEnum.getDescByCode(EyeDataUtil.glassesType(reScreening)));

        //裸眼（右）
        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightNakedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightNakedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.rightNakedVision(result),
                EyeDataUtil.rightNakedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightNakedVisionType);

        rightNakedVision.setType(rightNakedVisionType);
        rightNakedVision.setContent(EyeDataUtil.rightNakedVision(reScreening));
        reScreeningResult.setRightNakedVision(rightNakedVision);

        // 裸眼（左）
        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftNakedVision = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();

        boolean leftNakedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.leftNakedVision(result),
                EyeDataUtil.leftNakedVision(reScreening),new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftNakedVisionType);

        leftNakedVision.setType(leftNakedVisionType);
        leftNakedVision.setContent(EyeDataUtil.leftNakedVision(reScreening));
        reScreeningResult.setLeftNakedVision(leftNakedVision);

        //等效球镜右
        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightSE = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightSEType = BigDecimalUtil.isDeviation(EyeDataUtil.rightSE(result),
                EyeDataUtil.rightSE(reScreening),new BigDecimal(ReScreenConstant.SE_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightSEType);

        rightSE.setType(rightSEType);
        rightSE.setContent(EyeDataUtil.rightSE(reScreening));
        reScreeningResult.setRightSE(rightSE);

        //等效球镜左
        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftSE = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftSEType = BigDecimalUtil.isDeviation(EyeDataUtil.leftSE(result),
                EyeDataUtil.leftSE(reScreening),new BigDecimal(ReScreenConstant.SE_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftSEType);

        leftSE.setType(leftSEType);
        leftSE.setContent(EyeDataUtil.leftSE(reScreening));
        reScreeningResult.setLeftSE(leftSE);

        // 球镜 柱镜 不参与计算
        notParticipateDeviation(reScreening, reScreeningResult);
        // 有差异的计算
        deviationCount = differenceDeviationCount(result, reScreening, deviationCount, rescreening, reScreeningResult);
        rescreening.setDeviationCount(deviationCount);
        rescreening.setRescreeningResult(reScreeningResult);
        rescreening.setDeviation(EyeDataUtil.deviationData(reScreening));

        return rescreening;
    }

    /**
     * 有差异的计算
     * @param result 筛查结果
     * @param reScreening 复测结果
     * @param deviationCount 错误项次数
     * @param rescreening 复测返扩展类
     * @param reScreeningResult 计算后内容（复测）
     * @return 错误项次数
     */
    private static int differenceDeviationCount(VisionScreeningResult result, VisionScreeningResult reScreening, int deviationCount, ReScreenDTO rescreening, ReScreenDTO.ReScreeningResult reScreeningResult) {
        Integer glassesType = Optional.ofNullable(reScreening).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
        if (Objects.isNull(glassesType)) {
            return deviationCount;
        }
        // 是否为常见病筛查类型 TODO：替换为常量
        boolean isCommonDiseaseScreeningType = reScreening.getScreeningType() == 1;
        if (glassesType.equals(GlassesTypeEnum.FRAME_GLASSES.code) || glassesType.equals(GlassesTypeEnum.CONTACT_LENS.code)) {
            // 佩戴框架眼镜和隐形眼镜
            rescreening.setDoubleCount(isCommonDiseaseScreeningType ? ReScreenConstant.COMMON_RESCREEN_IS_GLASS_NUM : ReScreenConstant.VISION_RESCREEN_IS_GLASS_NUM);
            deviationCount = correctedDeviation(result, reScreening, deviationCount, reScreeningResult);
        } else if (glassesType.equals(GlassesTypeEnum.NOT_WEARING.code)){
            // 没有戴镜
            rescreening.setDoubleCount(isCommonDiseaseScreeningType ? ReScreenConstant.COMMON_RESCREEN_NOT_GLASS_NUM : ReScreenConstant.VISION_RESCREEN_NOT_GLASS_NUM);
        }
        return isCommonDiseaseScreeningType ? heightWeightDeviation(result, reScreening, deviationCount, reScreeningResult) : deviationCount;
    }

    /**
     * 球镜 柱镜 不参与计算
     * @param reScreening 筛查结果
     * @param reScreeningResult 计算后内容（复测）
     */
    private static void notParticipateDeviation(VisionScreeningResult reScreening, ReScreenDTO.ReScreeningResult reScreeningResult) {
        //球镜右
        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightSph = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        rightSph.setType(Boolean.FALSE);
        rightSph.setContent(EyeDataUtil.rightSph(reScreening));
        reScreeningResult.setRightSph(rightSph);

        //柱镜右
        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightCyl = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        rightCyl.setType(Boolean.FALSE);
        rightCyl.setContent(EyeDataUtil.rightCyl(reScreening));
        reScreeningResult.setRightCyl(rightCyl);

        //球镜左
        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftSph = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        leftSph.setType(Boolean.FALSE);
        leftSph.setContent(EyeDataUtil.leftSph(reScreening));
        reScreeningResult.setLeftSph(leftSph);

        //柱镜左
        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftCyl = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        leftCyl.setType(Boolean.FALSE);
        leftCyl.setContent(EyeDataUtil.leftCyl(reScreening));
        reScreeningResult.setLeftCyl(leftCyl);
    }

    /**
     * 身高体重计算
     * @param result 筛查结果
     * @param reScreening 复测结果
     * @param deviationCount 错误项次数
     * @param reScreeningResult 计算后内容（复测）
     * @return 错误项次数
     */
    private static int heightWeightDeviation(VisionScreeningResult result, VisionScreeningResult reScreening, int deviationCount, ReScreenDTO.ReScreeningResult reScreeningResult) {
        //身高
        ReScreenDTO.ReScreeningResult.ScreeningDeviation height = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean heightType = BigDecimalUtil.isDeviation(EyeDataUtil.height(result),
                EyeDataUtil.height(reScreening),new BigDecimal(ReScreenConstant.HEIGHT_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, heightType);

        height.setType(heightType);
        height.setContent(EyeDataUtil.height(reScreening));
        reScreeningResult.setHeight(height);

        //体重
        ReScreenDTO.ReScreeningResult.ScreeningDeviation weight = new  ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean weightType = BigDecimalUtil.isDeviation(EyeDataUtil.weight(result),
                EyeDataUtil.weight(reScreening), new BigDecimal(ReScreenConstant.WEIGHT_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, weightType);

        weight.setType(weightType);
        weight.setContent(EyeDataUtil.weight(reScreening));
        reScreeningResult.setWeight(weight);
        return deviationCount;
    }

    /**
     * 矫正视力计算
     * @param result 筛查结果
     * @param reScreening 复测结果
     * @param deviationCount 错误项次数
     * @param reScreeningResult 计算后内容（复测）
     * @return 错误项次数
     */
    private static int correctedDeviation(VisionScreeningResult result, VisionScreeningResult reScreening, int deviationCount, ReScreenDTO.ReScreeningResult reScreeningResult) {
        //矫正右
        ReScreenDTO.ReScreeningResult.ScreeningDeviation rightCorrectedVision = new ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean rightCorrectedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.rightCorrectedVision(result),
                EyeDataUtil.rightCorrectedVision(reScreening), new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, rightCorrectedVisionType);

        rightCorrectedVision.setType(rightCorrectedVisionType);
        rightCorrectedVision.setContent(EyeDataUtil.rightCorrectedVision(reScreening));
        reScreeningResult.setRightCorrectedVision(rightCorrectedVision);

        //矫正左
        ReScreenDTO.ReScreeningResult.ScreeningDeviation leftCorrectedVision = new ReScreenDTO.ReScreeningResult.ScreeningDeviation();
        boolean leftCorrectedVisionType = BigDecimalUtil.isDeviation(EyeDataUtil.leftCorrectedVision(result),
                EyeDataUtil.leftCorrectedVision(reScreening), new BigDecimal(ReScreenConstant.VISION_DEVIATION));
        deviationCount = getDeviationCount(deviationCount, leftCorrectedVisionType);

        leftCorrectedVision.setType(leftCorrectedVisionType);
        leftCorrectedVision.setContent(EyeDataUtil.leftCorrectedVision(reScreening));
        reScreeningResult.setLeftCorrectedVision(leftCorrectedVision);
        return deviationCount;
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

