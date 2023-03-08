package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.SEUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/12/11:05
 * @Description:
 */
@Slf4j
@UtilityClass
public class EyeDataUtil {

    private static final String EMPTY_DATA = "--";

    /**
     * 戴镜类型(正常情况下，两个眼睛要么同时有带镜类型数据，要么同时没有)
     * @param visionScreeningResult 筛查结果
     * @return 戴镜类型
     */
    public static String glassesTypeString(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(x -> WearingGlassesSituation.getType(x.getGlassesType())).orElse(EMPTY_DATA);
    }


    /**
     * 左眼轴位
     *
     * @param visionScreeningResult 筛查数据
     * @return 左眼轴位
     */
    public static String computerLeftAxial(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getAxial).map(x -> x.setScale(0, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    /**
     * 右眼轴位
     *
     * @param visionScreeningResult 筛查数据
     * @return 右眼轴位
     */
    public static String computerRightAxial(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getAxial).map(x -> x.setScale(0, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    /**
     * 左眼柱镜
     *
     * @param visionScreeningResult 筛查数据
     * @return 左眼柱镜
     */
    public static String computerLeftCyl(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).map(EyeDataUtil::setSphCyl).orElse(EMPTY_DATA);
    }

    public static String computerLeftCylNull(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).map(BigDecimal::toString).orElse(null);

    }

    /**
     * 右眼柱镜
     *
     * @param visionScreeningResult 筛查数据
     * @return 右眼柱镜
     */
    public static String computerRightCyl(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).map(EyeDataUtil::setSphCyl).orElse(EMPTY_DATA);
    }

    /**
     * 右眼柱镜非空验证
     *
     * @param visionScreeningResult 筛查数据
     * @return 右眼柱镜非空验证
     */
    public static String computerRightCylNull(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getCyl).map(BigDecimal::toString).orElse(null);
    }

    /**
     * 左眼球镜
     *
     * @param visionScreeningResult 筛查数据
     * @return 左眼球镜
     */
    public static String computerLeftSph(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).map(EyeDataUtil::setSphCyl).orElse(EMPTY_DATA);

    }

    public static String computerLeftSphNull(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getLeftEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).map(BigDecimal::toString).orElse(null);

    }

    /**
     * 右眼球镜
     * @param visionScreeningResult 筛查数据
     * @return 右眼球镜
     */
    public static String computerRightSph(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).map(EyeDataUtil::setSphCyl).orElse(EMPTY_DATA);
    }
    /**
     * 右眼球镜非空验证
     * @param visionScreeningResult 筛查数据
     * @return 右眼球镜非空验证
     */
    public static String computerRightSphNULL(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getRightEyeData).map(ComputerOptometryDO.ComputerOptometry::getSph).map(BigDecimal::toString).orElse(null);
    }

    public static String correctedLeftDataToStr(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getCorrectedVision).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    public static String correctedRightDataToStr(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getCorrectedVision).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    public static String visionRightDataToStr(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getNakedVision).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    public static String visionLeftDataToStr(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getNakedVision).map(x -> x.setScale(1, RoundingMode.DOWN).toString()).orElse(EMPTY_DATA);
    }

    private static String setSphCyl(BigDecimal bigDecimal){
        if (Objects.isNull(bigDecimal)) {
            return EMPTY_DATA;
        }
        int r = bigDecimal.compareTo(BigDecimal.ZERO);
        if (r >= 0){
            return "+" + bigDecimal.setScale(2, RoundingMode.DOWN);
        }
        return bigDecimal.setScale(2, RoundingMode.DOWN).toString();
    }

    /**
     * 获取右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼裸视力
     */
    public static BigDecimal rightNakedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getNakedVision) .orElse(null);
    }

    /**
     * 获取左眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 左眼裸视力
     */
    public static BigDecimal leftNakedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getLeftEyeData)
                .map(VisionDataDO.VisionData::getNakedVision) .orElse(null);
    }

    /**
     * 获取右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼戴镜视力
     */
    public static BigDecimal rightCorrectedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getCorrectedVision) .orElse(null);
    }

    /**
     * 获取左眼裸视力
     * @param visionScreenResult 筛查结果
     * @return 左眼戴镜视力
     */
    public static BigDecimal leftCorrectedVision(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getLeftEyeData)
                .map(VisionDataDO.VisionData::getCorrectedVision) .orElse(null);
    }

    /**
     * 获取右眼球镜
     * @param visionScreenResult 筛查结果
     * @return 右眼球镜
     */
    public static BigDecimal rightSph(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getSph) .orElse(null);
    }
    /**
     * 获取右眼柱镜
     * @param visionScreenResult 筛查结果
     * @return 右眼柱镜
     */
    public static BigDecimal rightCyl(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getCyl) .orElse(null);
    }
    /**
     * 获取右眼轴位
     * @param visionScreenResult 筛查结果
     * @return 筛查轴位
     */
    public static BigDecimal rightAxial(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getAxial) .orElse(null);
    }

    /**
     * 获取左眼球镜
     * @param visionScreenResult 筛查结果
     * @return 左眼球镜
     */
    public static BigDecimal leftSph(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getSph) .orElse(null);
    }
    /**
     * 获取左眼柱镜
     * @param visionScreenResult 筛查结果
     * @return 左眼柱镜
     */
    public static BigDecimal leftCyl(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getCyl) .orElse(null);
    }
    /**
     * 获取左眼轴位
     * @param visionScreenResult 筛查结果
     * @return 左眼轴位
     */
    public static BigDecimal leftAxial(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getAxial) .orElse(null);
    }

    /**
     * 获取身高
     * @param visionScreenResult 筛查结果
     * @return 身高
     */
    public static BigDecimal height(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getHeight).orElse(null);
    }

    /**
     * 获取体重
     * @param visionScreenResult 筛查结果
     * @return 体重
     */
    public static BigDecimal weight(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getWeight).orElse(null);
    }

    /**
     * 计算 等效球镜（右眼）
     * @param visionScreenResult 筛查数据
     * @return 计算 等效球镜（右眼）
     */
    public static BigDecimal rightSE(VisionScreeningResult visionScreenResult) {
        BigDecimal sph = rightSph(visionScreenResult);
        BigDecimal cyl = rightCyl(visionScreenResult);
        return SEUtil.getSphericalEquivalent(sph, cyl);
    }

    /**
     * 计算 等效球镜（左眼）
     * @param visionScreenResult 筛查数据
     * @return 计算 等效球镜（右眼）
     */
    public static BigDecimal leftSE(VisionScreeningResult visionScreenResult) {
        BigDecimal sph = leftSph(visionScreenResult);
        BigDecimal cyl = leftCyl(visionScreenResult);
        return SEUtil.getSphericalEquivalent(sph, cyl);
    }

    /**
     * 电脑验光误差
     * @param visionScreenResult 筛查结果
     * @return 电脑验光误差
     */
    public static DeviationDO.VisionOrOptometryDeviation optometryDeviation(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData)
                .map(DeviationDO::getVisionOrOptometryDeviation).orElse(null);
    }

    /**
     * 身高/体重误差说明
     * @param visionScreenResult 筛查结果
     * @return 身高/体重误差说明
     */
    public static DeviationDO.HeightWeightDeviation heightWeightDeviationRemark(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData)
                .map(DeviationDO::getHeightWeightDeviation).orElse(null);
    }

    /**
     * 误差结果
     * @param visionScreenResult 筛查结果
     * @return 误差结果
     */
    public static DeviationDO deviationData(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData) .orElse(null);
    }

    /**
     * 更新时间
     * @param visionScreenResult 筛查结果
     * @return 更新时间
     */
    public static Date updateTime(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getUpdateTime) .orElse(null);
    }

    /**
     * 戴镜类型
     * @param visionScreenResult 筛查结果
     * @return 戴镜类型
     */
    public static Integer glassesType(VisionScreeningResult visionScreenResult){
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getGlassesType) .orElse(null);
    }

    /**
     * 合并左右眼数据
     *
     * @param right 右眼
     * @param left  左眼
     *
     * @return 右眼/左眼
     */
    public static String mergeEyeData(String right, String left) {
        return right + StrUtil.SLASH + left;
    }

    /**
     * 课桌椅建议
     *
     * @param heightStr 身高
     * @param schoolAge 学龄
     *
     * @return TwoTuple<String, String>
     */
    public static TwoTuple<String, String> getDeskChairSuggest(String heightStr, Integer schoolAge) {
        if (StringUtils.isEmpty(heightStr) || Objects.isNull(schoolAge)) {
            return new TwoTuple<>(EMPTY_DATA, EMPTY_DATA);
        }
        float height = new BigDecimal(heightStr).floatValue();
        List<Integer> deskAndChairType = SchoolAge.KINDERGARTEN.code.equals(schoolAge) ? DeskChairTypeEnum.getKindergartenTypeByHeight(height) : DeskChairTypeEnum.getPrimarySecondaryTypeByHeight(height);
        String deskAndChairTypeDesc = deskAndChairType.stream().map(x -> x + "号").collect(Collectors.joining("或"));
        return new TwoTuple<>(com.wupol.myopia.base.util.StrUtil.spliceChar("，建议桌面高：", deskAndChairTypeDesc, BigDecimal.valueOf(height * 0.43).multiply(new BigDecimal("10")).setScale(0, RoundingMode.HALF_UP).toString()),
                com.wupol.myopia.base.util.StrUtil.spliceChar("，建议座面高：", deskAndChairTypeDesc, BigDecimal.valueOf(height * 0.24).multiply(new BigDecimal("10")).setScale(0, RoundingMode.HALF_UP).toString()));
    }

    /**
     * 获取屈光情况描述
     *
     * @param statConclusion 结论
     * @param isKindergarten 是否幼儿园
     *
     * @return 屈光情况
     */
    public static String getRefractiveResultDesc(StatConclusion statConclusion, boolean isKindergarten) {
        if (Objects.isNull(statConclusion)) {
            return StringUtils.EMPTY;
        }

        List<String> result = new ArrayList<>();
        if (isKindergarten) {
            if (Objects.equals(statConclusion.getWarningLevel(), WarningLevel.ZERO_SP.code)) {
                result.add(WarningLevel.ZERO_SP.desc);
            }
            if (Objects.equals(statConclusion.getIsAnisometropia(), Boolean.TRUE)) {
                result.add("屈光参差");
            }
            if (Objects.equals(statConclusion.getIsRefractiveError(), Boolean.TRUE)) {
                result.add("屈光不正");
            }
        } else {
            result.add(MyopiaLevelEnum.getDescByCode(statConclusion.getMyopiaLevel()));
            result.add(HyperopiaLevelEnum.getDescByCode(statConclusion.getHyperopiaLevel()));
            result.add(AstigmatismLevelEnum.getDescByCode(statConclusion.getAstigmatismLevel()));
        }
        List<String> resultList = result.stream().filter(StringUtils::isNotBlank).filter(s -> !StringUtils.equals(s, "正常")).distinct().collect(Collectors.toList());
        return CollectionUtils.isEmpty(resultList) ? "正常" : String.join(",", resultList);
    }

    /**
     * 获取身高
     * @param visionScreenResult 筛查结果
     * @return 身高
     */
    public static String heightToStr(VisionScreeningResult visionScreenResult) {
        BigDecimal bigDecimal = Optional.ofNullable(visionScreenResult).map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getHeight).orElse(null);
        if (Objects.isNull(bigDecimal)) {
            return StringUtils.EMPTY;
        }
        return bigDecimal.toString();
    }

    /**
     * 拼接符号
     */
    public String spliceSymbol(BigDecimal val) {
        if (Objects.isNull(val)) {
            return StringUtils.EMPTY;
        }
        if (BigDecimalUtil.moreThanAndEqual(val, "0")) {
            return "+" + val;
        }
        return val.toString();
    }

    /**
     * 获取体重
     *
     * @param visionScreenResult 筛查结果
     *
     * @return 体重
     */
    public static String weightToStr(VisionScreeningResult visionScreenResult) {
        BigDecimal bigDecimal = Optional.ofNullable(visionScreenResult).map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getWeight).orElse(null);
        if (Objects.isNull(bigDecimal)) {
            return StringUtils.EMPTY;
        }
        return bigDecimal.toString();
    }

    public static String biometricRightK1(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getRightEyeData).map(BiometricDataDO.BiometricData::getK1).orElse(EMPTY_DATA);
    }
    public static String biometricRightK2(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getRightEyeData).map(BiometricDataDO.BiometricData::getK2).orElse(EMPTY_DATA);
    }
    public static String biometricRightK1Axis(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getRightEyeData).map(BiometricDataDO.BiometricData::getK1Axis).orElse(EMPTY_DATA);
    }
    public static String biometricRightK2Axis(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getRightEyeData).map(BiometricDataDO.BiometricData::getK2Axis).orElse(EMPTY_DATA);
    }
    public static String biometricLeftK1(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getLeftEyeData).map(BiometricDataDO.BiometricData::getK1).orElse(EMPTY_DATA);
    }
    public static String biometricLeftK2(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getLeftEyeData).map(BiometricDataDO.BiometricData::getK2).orElse(EMPTY_DATA);
    }
    public static String biometricLeftK1Axis(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getLeftEyeData).map(BiometricDataDO.BiometricData::getK1Axis).orElse(EMPTY_DATA);
    }
    public static String biometricLeftK2Axis(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getLeftEyeData).map(BiometricDataDO.BiometricData::getK2Axis).orElse(EMPTY_DATA);
    }

    public static String biometricRightAl(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getRightEyeData).map(BiometricDataDO.BiometricData::getAl).orElse(EMPTY_DATA);
    }
    public static String biometricLeftAl(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getBiometricData).map(BiometricDataDO::getLeftEyeData).map(BiometricDataDO.BiometricData::getAl).orElse(EMPTY_DATA);
    }

    public static String leftEyePressure(VisionScreeningResult visionScreeningResult) {
        BigDecimal bigDecimal = Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getEyePressureData).map(EyePressureDataDO::getRightEyeData).map(EyePressureDataDO.EyePressureData::getPressure).orElse(null);
        if (Objects.isNull(bigDecimal)) {
            return EMPTY_DATA;
        }
        return bigDecimal.toString();
    }

    public static String rightEyePressure(VisionScreeningResult visionScreeningResult) {
        BigDecimal bigDecimal = Optional.ofNullable(visionScreeningResult).map(VisionScreeningResult::getEyePressureData).map(EyePressureDataDO::getLeftEyeData).map(EyePressureDataDO.EyePressureData::getPressure).orElse(null);
        if (Objects.isNull(bigDecimal)) {
            return EMPTY_DATA;
        }
        return bigDecimal.toString();
    }



}
