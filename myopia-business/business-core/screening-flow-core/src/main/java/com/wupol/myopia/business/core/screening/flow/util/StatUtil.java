package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 筛查结论计算工具
 */
@UtilityClass
public class StatUtil {

    /**
     * 是否近视
     *
     * @param sphere      球镜
     * @param cylinder    柱镜
     * @param age
     * @param nakedVision
     * @return
     */
    public static boolean isMyopia(Float sphere, Float cylinder, Integer age, Float nakedVision) {
        if (Objects.nonNull(age)) {
            if (age < 6 && nakedVision < 4.9) {
                return true;
            }
            if (Objects.nonNull(nakedVision) && age >= 6 && nakedVision < 5) {
                return true;
            }
        }
        return Objects.requireNonNull(getMyopiaWarningLevel(sphere, cylinder)).code > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }

    /**
     * 是否近视
     *
     * @param myopiaWarningLevel 近视预警级别
     * @return
     */
    public static boolean isMyopia(MyopiaLevelEnum myopiaWarningLevel) {
        return myopiaWarningLevel.code > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }

    /**
     * 是否近视
     *
     * @param myopiaWarningLevel 近视预警级别
     * @return boolean
     */
    public static boolean isMyopia(Integer myopiaWarningLevel) {
        if (Objects.isNull(myopiaWarningLevel)) {
            return false;
        }
        return myopiaWarningLevel > MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code;
    }

    /**
     * 是否远视
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static boolean isHyperopia(Float sphere, Float cylinder, Integer age) {
        if (Objects.isNull(age)) {
            return false;
        }
        float se = getSphericalEquivalent(sphere, cylinder);

        if (age < 4 && se > 3) {
            return true;
        }
        if ((age < 6 && age >= 4) && se > 2) {
            return true;
        }
        if ((age < 8 && age >= 6) && se > 1.5) {
            return true;
        }
        if (age >= 8 && se > 0.5) {
            return true;
        }
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        return hyperopiaWarningLevel != null && hyperopiaWarningLevel.code > HyperopiaLevelEnum.ZERO.code;
    }

    /**
     * 是否远视
     *
     * @param hyperopiaWarningLevel 远视预警级别
     * @return
     */
    public static boolean isHyperopia(HyperopiaLevelEnum hyperopiaWarningLevel) {
        return hyperopiaWarningLevel.code > HyperopiaLevelEnum.ZERO.code;
    }

    /**
     * 是否散光
     *
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isAstigmatism(Float cylinder) {
        AstigmatismLevelEnum astigmatismWarningLevel = getAstigmatismWarningLevel(cylinder);
        return astigmatismWarningLevel != null && astigmatismWarningLevel.code > AstigmatismLevelEnum.ZERO.getCode();
    }

    /**
     * 是否散光
     *
     * @param astigmatismWarningLevel 散光预警级别
     * @return
     */
    public static Boolean isAstigmatism(AstigmatismLevelEnum astigmatismWarningLevel) {
        return isWarningLevelGreatThanZero(astigmatismWarningLevel);
    }

    /**
     * 判断预警级别是否大于0
     *
     * @param warningLevel 预警级别
     * @return
     */
    private static boolean isWarningLevelGreatThanZero(AstigmatismLevelEnum warningLevel) {
        return warningLevel.code > AstigmatismLevelEnum.ZERO.getCode();
    }

    /**
     * 视力低下等级
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return
     */
    public static WarningLevel getNakedVisionWarningLevel(Float nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 6) {
            return null;
        }
        if (nakedVision >= 5.0f ) {
            return null;
        }
        if (nakedVision == 4.9f) {
            return WarningLevel.ONE;
        }
        if (nakedVision >= 4.6f && nakedVision <= 4.8f) {
            return WarningLevel.TWO;
        }
        if (nakedVision <= 4.5f) {
            return WarningLevel.THREE;
        }
        return null;
    }

    /**
     * 判断是否视力低下
     *
     * @param nakedVision
     * @param age
     * @return
     */
    public static Boolean isLowVision(Float nakedVision, Integer age) {
        if (nakedVision == null || age == null || age < 0) {
            return null;
        }
        if (age > 0 && age < 3 && nakedVision <= 4.6) {
            return true;
        }
        if (age == 3 && nakedVision <= 4.7) {
            return true;
        }
        if (age == 4 && nakedVision <= 4.8) {
            return true;
        }
        if (age == 5 && nakedVision <= 4.9) {
            return true;
        }
        return age >= 6 && nakedVision < 5.0;
    }

    /**
     * 返回远视预警级别
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static HyperopiaLevelEnum getHyperopiaWarningLevel(Float sphere, Float cylinder, Integer age) {
        if (sphere == null || cylinder == null || age == null) {
            return null;
        }
        float se = getSphericalEquivalent(sphere, cylinder);

        if (age >= 12) {
            if (se > 0.5f && se <= 3.0f) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT;
            }
            if (se > 3.0f && se <= 6.0f) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_MIDDLE;
            }
            if (se > 6.0f) {
                return HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH;
            }
            return HyperopiaLevelEnum.ZERO;
        }
        return null;
    }

    /**
     * 获取远视Level
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return Integer
     */
    public static Integer getHyperopiaLevel(Float sphere, Float cylinder, Integer age) {
        HyperopiaLevelEnum hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        if (Objects.nonNull(hyperopiaWarningLevel)) {
            return hyperopiaWarningLevel.code;
        }
        return null;
    }

    /**
     * 返回近视预警级别
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static MyopiaLevelEnum getMyopiaWarningLevel(Float sphere, Float cylinder) {
        if (!ObjectsUtil.allNotNull(sphere, cylinder)) {
            return null;
        }
        float se = getSphericalEquivalent(sphere, cylinder);
        if (se == -0.5) {
            return MyopiaLevelEnum.ZERO;
        }
        if (se > -0.5 && se <= 0.75) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_EARLY;
        }
        if (se >= -3.0f && se < -0.5f) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT;
        }
        if (se >= -6.0f && se < -3.0f) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE;
        }
        if (se < -6.0f) {
            return MyopiaLevelEnum.MYOPIA_LEVEL_HIGH;
        }
        return MyopiaLevelEnum.ZERO;
    }

    /**
     * 返回近视预警级别Level
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static Integer getMyopiaLevel(Float sphere, Float cylinder) {
        MyopiaLevelEnum myopiaWarningLevel = getMyopiaWarningLevel(sphere, cylinder);
        if (Objects.nonNull(myopiaWarningLevel)) {
            return myopiaWarningLevel.code;
        }
        return null;
    }

    public static Integer getAstigmatismLevel(Float cylinder) {
        AstigmatismLevelEnum astigmatismWarningLevel = getAstigmatismWarningLevel(cylinder);
        if (Objects.nonNull(astigmatismWarningLevel)) {
            return astigmatismWarningLevel.code;
        }
        return null;
    }

    /**
     * 返回散光预警级别
     *
     * @param cylinder 柱镜
     */
    public static AstigmatismLevelEnum getAstigmatismWarningLevel(Float cylinder) {
        if (cylinder == null) {
            return null;
        }
        float cylinderAbs = Math.abs(cylinder);
        if (cylinderAbs >= 0.5f && cylinderAbs <= 2.0f) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT;
        }
        if (cylinderAbs > 2.0f && cylinderAbs <= 4.0f) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE;
        }
        if (cylinderAbs > 4.0f) {
            return AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH;
        }
        return AstigmatismLevelEnum.ZERO;
    }

    /**
     * 是否屈光不正
     *
     * @param isAstigmatism 是否散光
     * @param isMyopia      是否近视
     * @param isHyperopia   是否远视
     */
    public static boolean isRefractiveError(
            boolean isAstigmatism, boolean isMyopia, boolean isHyperopia) {
        return isAstigmatism || isMyopia || isHyperopia;
    }

    /**
     * 是否屈光不正
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     */
    public static boolean isRefractiveError(Float sphere, Float cylinder, Integer age, Float nakedVision) {
        return isAstigmatism(cylinder) && isMyopia(sphere, cylinder, age, nakedVision)
                && isHyperopia(sphere, cylinder, age);
    }

    /**
     * 计算等效球镜
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static BigDecimal getSphericalEquivalent(BigDecimal sphere, BigDecimal cylinder) {
        if (ObjectsUtil.hasNull(sphere, cylinder)) {
            return null;
        }
        return cylinder.divide(new BigDecimal(2)).add(sphere);
    }

    /**
     * 计算等效球镜
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static Float getSphericalEquivalent(Float sphere, Float cylinder) {
        return cylinder / 2 + sphere;
    }

    /**
     * 初筛数据完整性判断
     * <p>
     * 1，配镜情况：没有配镜，需要：裸眼视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 2，配镜情况：佩戴框架眼镜，需要裸眼视力、矫正视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 3，配镜情况：佩戴隐形眼镜，需要裸眼视力、矫正视力和电脑验光数据（球镜、柱镜、轴位）
     * <p>
     * 4，配镜情况：佩戴角膜塑形镜，需要矫正视力。(如没有度数则默认放在2级预警中 如有度数则根据度数定预警级别)
     * <p>
     * 复测数据完整性判断
     * <p>
     * <p>
     * <p>
     * 1、复测随机抽取满足条件的已筛查学生的6% （一开始是5%，调整为5%，确保：纳入发生率统计的复测学生达到5%。）
     * <p>
     * 数据是否有效或者完整
     *
     * @param visionData
     * @param computerOptometry
     * @return
     */
    public static boolean isCompletedData(VisionDataDO visionData, ComputerOptometryDO computerOptometry) {
        if (visionData == null || visionData.getLeftEyeData() == null || visionData.getLeftEyeData().getGlassesType() == null) {
            return false;
        }
        Integer glassesType = visionData.getLeftEyeData().getGlassesType();
        if (WearingGlassesSituation.NOT_WEARING_GLASSES_KEY.equals(glassesType)) {
            return visionData.validNakedVision() && Objects.nonNull(computerOptometry) && computerOptometry.valid();
        } else if (WearingGlassesSituation.WEARING_FRAME_GLASSES_KEY.equals(glassesType) || WearingGlassesSituation.WEARING_CONTACT_LENS_KEY.equals(glassesType)) {
            return visionData.validNakedVision() && visionData.validCorrectedVision() && Objects.nonNull(computerOptometry) && computerOptometry.valid();
        } else if (WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(glassesType)) {
            return visionData.validCorrectedVision();
        } else {
            return false;
        }
    }

    /**
     * 获取近视等级描述（严重的眼球）
     *
     * @param leftSpn          左眼球镜
     * @param leftCyl          左眼柱镜
     * @param rightSpn         右眼球镜
     * @param rightCyl         右眼球镜
     * @param age
     * @param leftNakedVision
     * @param rightNakedVision
     * @return String
     */
    public String getMyopiaLevelDesc(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl,
                                     Integer age, BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        if (ObjectsUtil.allNull(leftSpn, leftCyl, rightCyl, rightSpn)) {
            return "";
        }
        Integer leftMyopiaLevel = null;
        Integer rightMyopiaLevel = null;
        if (ObjectsUtil.allNotNull(leftSpn, leftCyl)) {
            leftMyopiaLevel = getMyopiaLevel(leftSpn.floatValue(), leftCyl.floatValue());
        }
        if (ObjectsUtil.allNotNull(rightSpn, rightCyl)) {
            rightMyopiaLevel = getMyopiaLevel(rightSpn.floatValue(), rightCyl.floatValue());
        }
        if (!ObjectsUtil.allNull(leftMyopiaLevel, rightMyopiaLevel)) {
            Integer seriousLevel = getSeriousLevel(leftMyopiaLevel, rightMyopiaLevel);
            return MyopiaLevelEnum.getDesc(seriousLevel);
        }
        return "";
    }

    /**
     * 获取远视等级描述（严重的眼球）
     *
     * @param leftSpn  左眼球镜
     * @param leftCyl  左眼柱镜
     * @param rightSpn 右眼球镜
     * @param rightCyl 右眼球镜
     * @return String
     */
    public String getHyperopiaDesc(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl, Integer age) {
        if (ObjectsUtil.allNull(leftSpn, leftCyl, rightSpn, rightCyl)) {
            return "";
        }
        HyperopiaLevelEnum leftLevel = null;
        HyperopiaLevelEnum rightLevel = null;
        if (ObjectsUtil.allNotNull(leftSpn, leftCyl)) {
            leftLevel = getHyperopiaWarningLevel(leftSpn.floatValue(), leftCyl.floatValue(), age);
        }
        if (ObjectsUtil.allNotNull(rightSpn, rightCyl)) {
            rightLevel = getHyperopiaWarningLevel(rightSpn.floatValue(), rightCyl.floatValue(), age);
        }

        if (ObjectsUtil.allNull(leftLevel, rightLevel)) {
            return "";
        }
        Integer leftHyperopiaLevel = Objects.nonNull(leftLevel) ? leftLevel.code : null;
        Integer rightHyperopiaLevel = Objects.nonNull(rightLevel) ? rightLevel.code : null;
        if (!ObjectsUtil.allNull(leftHyperopiaLevel, rightHyperopiaLevel)) {
            Integer seriousLevel = getSeriousLevel(leftHyperopiaLevel, rightHyperopiaLevel);
            return HyperopiaLevelEnum.getDesc(seriousLevel);
        }
        return "";
    }

    /**
     * 获取散光描述
     *
     * @param leftCyl  左眼柱镜
     * @param rightCyl 右眼球镜
     * @return String
     */
    public String getAstigmatismDesc(BigDecimal leftCyl, BigDecimal rightCyl) {
        if (Objects.nonNull(leftCyl) && (leftCyl.abs().compareTo(new BigDecimal("0.5")) > 0)) {
            return "散光";
        }
        if (Objects.nonNull(rightCyl) && (rightCyl.abs().compareTo(new BigDecimal("0.5")) > 0)) {
            return "散光";
        }
        return "";
    }

    /**
     * 获取屈光描述
     *
     * @param leftSpn          左眼球镜
     * @param leftCyl          左眼柱镜
     * @param rightSpn         右眼球镜
     * @param rightCyl         右眼球镜
     * @param age              年龄
     * @param leftNakedVision
     * @param rightNakedVision
     * @return 描述
     */
    public String getRefractiveResult(BigDecimal leftSpn, BigDecimal leftCyl,
                                      BigDecimal rightSpn, BigDecimal rightCyl,
                                      Integer age, BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        return getMyopiaLevelDesc(leftSpn, leftCyl, rightSpn, rightCyl, age, leftNakedVision, rightNakedVision)
                + getHyperopiaDesc(leftSpn, leftCyl, rightSpn, rightCyl, age)
                + getAstigmatismDesc(leftCyl, rightCyl);
    }

    /**
     * 获取预警等级（两眼取严重的）
     *
     * @param leftCyl          左柱镜
     * @param leftSpn          左球镜
     * @param leftNakedVision  左裸眼视力
     * @param rightCyl         右柱镜
     * @param rightSpn         右球镜
     * @param rightNakedVision 右裸眼视力
     * @param age              年龄
     * @return {@link WarningLevel}
     */
    public Integer getWarningLevelInt(BigDecimal leftCyl, BigDecimal leftSpn, BigDecimal leftNakedVision,
                                      BigDecimal rightCyl, BigDecimal rightSpn, BigDecimal rightNakedVision,
                                      Integer age) {
        WarningLevel left = getWarningLevel(leftCyl, leftSpn, leftNakedVision, age);
        WarningLevel right = getWarningLevel(rightCyl, rightSpn, rightNakedVision, age);
        return getWarningSeriousLevel(Objects.nonNull(left) ? left.code : null, Objects.nonNull(right) ? right.code : null);
    }

    /**
     * 单眼获取预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    public WarningLevel getWarningLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision, Integer age) {

        if (ObjectsUtil.hasNull(cyl, spn, nakedVision, age)) {
            return null;
        }
        if (age >= 3 && age < 6) {
            return between3And5GetLevel(cyl, spn, nakedVision, age);
        }
        if (age >= 6 && age < 8) {
            return between6And7GetLevel(cyl, spn, nakedVision, age);
        }
        if (age >= 8) {
            return moreThan8GetLevel(cyl, spn, nakedVision);
        }
        return zeroSPWarningLevel(cyl, spn, age);
    }

    /**
     * 3到5岁预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel between3And5GetLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        BigDecimal absCyl = cyl.abs();
        if (Objects.nonNull(zeroSPWarningLevel(cyl, spn, age))) {
            return zeroSPWarningLevel(cyl, spn, age);
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (age < 4 && BigDecimalUtil.moreThan(se, "9")) || (age >= 4 && BigDecimalUtil.moreThan(se, "8")) || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "6", "9") || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "5", "8")) || twoAbsCyl(absCyl))) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.6", "4.7") || oneSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "3", "6")) || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "2", "5")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.7") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 6到7岁预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel between6And7GetLevel(BigDecimal cyl, BigDecimal spn,
                                              BigDecimal nakedVision, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        BigDecimal absCyl = cyl.abs();
        if (Objects.isNull(se)) {
            return null;
        }
        if (Objects.nonNull(zeroSPWarningLevel(cyl, spn, age))) {
            return zeroSPWarningLevel(cyl, spn, age);
        }
        if ((BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5")) || threeSE(se) || BigDecimalUtil.moreThan(se, "7.5") || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || BigDecimalUtil.isBetweenRight(se, "4.5", "7.5") || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.8") || oneSE(se) || BigDecimalUtil.isBetweenRight(se, "1.5", "4.5") || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.8") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 8岁以上预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @return {@link WarningLevel}
     */
    private WarningLevel moreThan8GetLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        BigDecimal absCyl = cyl.abs();
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (BigDecimalUtil.moreThan(se, "6") || threeAbsCyl(absCyl))) {
            return WarningLevel.THREE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.7") || twoSE(se) || (BigDecimalUtil.isBetweenRight(se, "3", "6")) || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.9") || oneSE(se) || (BigDecimalUtil.isBetweenRight(se, "0.5", "3")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.moreThan(nakedVision, "4.9") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        return null;
    }

    /**
     * 0级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean zeroSE(BigDecimal se) {
        return (BigDecimalUtil.isBetweenAll(se, "-0.5", "-0.25"));
    }

    /**
     * 1级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean oneSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenLeft(se, "-3", "-0.5");
    }

    /**
     * 2级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean twoSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenLeft(se, "-6", "-3");
    }

    /**
     * 3级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean threeSE(BigDecimal se) {
        return BigDecimalUtil.lessThan(se, "-6");
    }

    /**
     * 0级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean zeroAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenLeft(absCyl, "0.25", "0.5");
    }

    /**
     * 1级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean oneAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenAll(absCyl, "0.5", "2");
    }

    /**
     * 2级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean twoAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.isBetweenRight(absCyl, "2", "4");
    }

    /**
     * 3级预警绝对柱镜
     *
     * @param absCyl 绝对柱镜
     * @return 是否满足条件
     */
    private boolean threeAbsCyl(BigDecimal absCyl) {
        return BigDecimalUtil.moreThan(absCyl, "4");
    }

    /**
     * 取预警级别严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getWarningSeriousLevel(Integer leftLevel, Integer rightLevel) {
        // 排除远视储备不足
        if (Objects.isNull(leftLevel)) {
            return rightLevel;
        }
        if (Objects.isNull(rightLevel)) {
            return leftLevel;
        }
        if (leftLevel.equals(WarningLevel.ZERO_SP.code)) {
            if (rightLevel.equals(WarningLevel.ZERO.code) || rightLevel.equals(WarningLevel.NORMAL.code)) {
                return leftLevel;
            }
            return rightLevel;
        }
        if (rightLevel.equals(WarningLevel.ZERO_SP.code)) {
            if (leftLevel.equals(WarningLevel.ZERO.code) || leftLevel.equals(WarningLevel.NORMAL.code)) {
                return rightLevel;
            }
            return leftLevel;
        }
        return leftLevel > rightLevel ? leftLevel : rightLevel;
    }


    /**
     * 取严重的等级
     *
     * @param leftLevel  左眼视力
     * @param rightLevel 右眼视力
     * @return 视力
     */
    public Integer getSeriousLevel(Integer leftLevel, Integer rightLevel) {
        if (Objects.isNull(leftLevel)) {
            return rightLevel;
        }
        if (Objects.isNull(rightLevel)) {
            return leftLevel;
        }
        return leftLevel > rightLevel ? leftLevel : rightLevel;
    }

    /**
     * 远视储备不足
     *
     * @param cyl 柱镜
     * @param spn 球镜
     * @param age 年龄
     * @return {@link WarningLevel}
     */
    private WarningLevel zeroSPWarningLevel(BigDecimal cyl, BigDecimal spn, Integer age) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        if (Objects.isNull(se)) {
            return null;
        }
        if (age >= 3 && age < 6 && BigDecimalUtil.isBetweenAll(se, "0", "1.5")) {
            return WarningLevel.ZERO_SP;
        }

        if (age >= 6 && age < 8 && BigDecimalUtil.isBetweenAll(se, "0", "1")) {
            return WarningLevel.ZERO_SP;
        }
        return null;
    }
}
