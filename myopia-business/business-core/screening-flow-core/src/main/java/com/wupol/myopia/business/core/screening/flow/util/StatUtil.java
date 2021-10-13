package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.VisionLabelsEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
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
     * @param sphere   球镜
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isMyopia(Float sphere, Float cylinder) {
        return Objects.requireNonNull(getMyopiaWarningLevel(sphere, cylinder)).code > 0;
    }

    /**
     * 是否近视
     *
     * @param myopiaWarningLevel 近视预警级别
     * @return
     */
    public static boolean isMyopia(WarningLevel myopiaWarningLevel) {
        return isWarningLevelGreatThanZero(myopiaWarningLevel);
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
        WarningLevel hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
        return hyperopiaWarningLevel != null && hyperopiaWarningLevel.code > 0;
    }

    /**
     * 是否远视
     *
     * @param hyperopiaWarningLevel 远视预警级别
     * @return
     */
    public static boolean isHyperopia(WarningLevel hyperopiaWarningLevel) {
        return isWarningLevelGreatThanZero(hyperopiaWarningLevel);
    }

    /**
     * 是否散光
     *
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isAstigmatism(Float cylinder) {
        WarningLevel astigmatismWarningLevel = getAstigmatismWarningLevel(cylinder);
        return astigmatismWarningLevel != null && astigmatismWarningLevel.code > 0;
    }

    /**
     * 是否散光
     *
     * @param astigmatismWarningLevel 散光预警级别
     * @return
     */
    public static Boolean isAstigmatism(WarningLevel astigmatismWarningLevel) {
        return isWarningLevelGreatThanZero(astigmatismWarningLevel);
    }

    /**
     * 是否建议就诊
     *
     * @param nakedVision      裸眼视力
     * @param sphere           球镜
     * @param cylinder         柱镜
     * @param isWearingGlasses
     * @param correctVision    矫正视力
     * @param age              年龄
     * @param schoolAge        学龄
     * @return
     */
    public static boolean isRecommendVisit(Float nakedVision, Float sphere, Float cylinder,
                                           Boolean isWearingGlasses, Float correctVision, Integer age, SchoolAge schoolAge) {
        if (nakedVision == null) {
            return false;
        }
        Float se = null;
        if (sphere != null && cylinder != null) {
            se = getSphericalEquivalent(sphere, cylinder);
        }

        if (nakedVision < 4.9) {
            if (isWearingGlasses) {
                return correctVision < 4.9;
            } else {

                if (schoolAge == null || se == null) {
                    return false;
                }

                switch (schoolAge) {
                    case PRIMARY:
                        if (se >= 0 && se < 2 && Math.abs(cylinder) < 1.5) return true;
                        if (se >= 2 || se < 0) return true;
                        break;
                    case JUNIOR:
                    case HIGH:
                    case VOCATIONAL_HIGH:
                        if (se >= -0.5 && se < 3 && Math.abs(cylinder) < 1.5) return true;
                        if (se < -0.5 || se >= 3 || Math.abs(cylinder) >= 1.5) return true;
                        break;
                    default:
                }
            }
        } else return age != null && se != null && age >= 6 && se >= 2;
        return false;
    }

    /**
     * 判断预警级别是否大于0
     *
     * @param warningLevel 预警级别
     * @return
     */
    private static boolean isWarningLevelGreatThanZero(WarningLevel warningLevel) {
        return warningLevel.code > 0;
    }

    /**
     * 返回裸眼视力预警级别
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return
     */
    public static WarningLevel getNakedVisionWarningLevel(Float nakedVision, Integer age) {
        if (nakedVision == null || age == null) {
            return null;
        }
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                if (nakedVision > 4.7f && nakedVision < 5.0f) return WarningLevel.ZERO;
                if (nakedVision > 4.6f && nakedVision <= 4.7f) return WarningLevel.ONE;
                if (nakedVision > 4.5f && nakedVision <= 4.6f) return WarningLevel.TWO;
                if (nakedVision <= 4.5f) return WarningLevel.THREE;
                break;
            case 6:
            case 7:
                if (nakedVision > 4.8f && nakedVision < 5.0f) return WarningLevel.ZERO;
                if (nakedVision > 4.7f && nakedVision <= 4.8f) return WarningLevel.ONE;
                if (nakedVision > 4.5f && nakedVision <= 4.7f) return WarningLevel.TWO;
                if (nakedVision <= 4.5f) return WarningLevel.THREE;
                break;
            default:
                if (nakedVision > 4.9f && nakedVision < 5.0f) return WarningLevel.ZERO;
                if (nakedVision > 4.7f && nakedVision <= 4.9f) return WarningLevel.ONE;
                if (nakedVision > 4.5f && nakedVision <= 4.7f) return WarningLevel.TWO;
                if (nakedVision <= 4.5f) return WarningLevel.THREE;
        }
        return WarningLevel.NORMAL;
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
        boolean isLowVision = false;

        switch (age) {
            case 0:
            case 1:
            case 2:
                if (nakedVision < 4.6f) {
                    isLowVision = true;
                }
                break;
            case 3:
                if (nakedVision < 4.7f) {
                    isLowVision = true;
                }
                break;
            case 4:
            case 5:
                if (nakedVision < 4.9f) {
                    isLowVision = true;
                }
                break;
            default:
                if (nakedVision < 5.0f) {
                    isLowVision = true;
                }
        }
        return isLowVision;
    }


    /**
     * 返回远视预警级别
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     * @param age      年龄
     * @return
     */
    public static WarningLevel getHyperopiaWarningLevel(Float sphere, Float cylinder, Integer age) {
        if (sphere == null || cylinder == null || age == null) {
            return null;
        }
        float se = getSphericalEquivalent(sphere, cylinder);

        if (age < 4 && se > 3) {
            return WarningLevel.ONE;
        }
        if ((age < 6 && age >= 4) && se > 2) {
            return WarningLevel.ONE;
        }
        if ((age < 8 && age >= 6) && se > 1.5) {
            return WarningLevel.ONE;
        }
        if (age >= 8) {
            if (se > 0.5f && se <= 3.0f) return WarningLevel.ONE;
            if (se > 3.0f && se <= 6.0f) return WarningLevel.TWO;
            if (se > 6.0f) return WarningLevel.THREE;
        }
        return WarningLevel.NORMAL;
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
        WarningLevel hyperopiaWarningLevel = getHyperopiaWarningLevel(sphere, cylinder, age);
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
    public static WarningLevel getMyopiaWarningLevel(Float sphere, Float cylinder) {
        if (!ObjectsUtil.allNotNull(sphere, cylinder)) {
            return null;
        }
        float se = getSphericalEquivalent(sphere, cylinder);
        if (se >= -3.0f && se < -0.5f) return WarningLevel.ONE;
        if (se >= -6.0f && se < -3.0f) return WarningLevel.TWO;
        if (se < -6.0f) return WarningLevel.THREE;
        return WarningLevel.NORMAL;
    }

    /**
     * 返回近视预警级别Level
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static Integer getMyopiaLevel(Float sphere, Float cylinder) {
        WarningLevel myopiaWarningLevel = getMyopiaWarningLevel(sphere, cylinder);
        if (Objects.nonNull(myopiaWarningLevel)) {
            return myopiaWarningLevel.code;
        }
        return null;
    }

    /**
     * 返回散光预警级别
     *
     * @param cylinder 柱镜
     */
    public static WarningLevel getAstigmatismWarningLevel(Float cylinder) {
        if (cylinder == null) {
            return null;
        }
        float cylinderAbs = Math.abs(cylinder);
        if (cylinderAbs >= 0.5f && cylinderAbs <= 2.0f) return WarningLevel.ONE;
        if (cylinderAbs > 2.0f && cylinderAbs <= 4.0f) return WarningLevel.TWO;
        if (cylinderAbs > 4.0f) return WarningLevel.THREE;
        return WarningLevel.NORMAL;
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
    public static boolean isRefractiveError(Float sphere, Float cylinder, Integer age) {
        return isAstigmatism(cylinder) && isMyopia(sphere, cylinder)
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
     * @param leftSpn  左眼球镜
     * @param leftCyl  左眼柱镜
     * @param rightSpn 右眼球镜
     * @param rightCyl 右眼球镜
     * @return String
     */
    public String getMyopiaLevelDesc(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl) {
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
            Integer seriousLevel = ScreeningResultUtil.getSeriousLevel(leftMyopiaLevel, rightMyopiaLevel);
            if (Objects.nonNull(seriousLevel)) {
                if (WarningLevel.ONE.code.equals(seriousLevel)) {
                    return VisionLabelsEnum.MILD_MYOPIA.getName();
                }
                if (WarningLevel.TWO.code.equals(seriousLevel)) {
                    return VisionLabelsEnum.MODERATE_MYOPIA.getName();
                }
                if (WarningLevel.THREE.code.equals(seriousLevel)) {
                    return VisionLabelsEnum.HIGH_MYOPIA.getName();
                }
            }
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
        WarningLevel leftLevel = null;
        WarningLevel rightLevel = null;
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
            Integer seriousLevel = ScreeningResultUtil.getSeriousLevel(leftHyperopiaLevel, rightHyperopiaLevel);
            if (WarningLevel.ONE.code.equals(seriousLevel)) {
                return VisionLabelsEnum.MILD_HYPEROPIA.getName();
            }
            if (WarningLevel.TWO.code.equals(seriousLevel)) {
                return VisionLabelsEnum.MODERATE_HYPEROPIA.getName();
            }
            if (WarningLevel.THREE.code.equals(seriousLevel)) {
                return VisionLabelsEnum.HIGH_HYPEROPIA.getName();
            }
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
     * @param leftSpn  左眼球镜
     * @param leftCyl  左眼柱镜
     * @param rightSpn 右眼球镜
     * @param rightCyl 右眼球镜
     * @param age      年龄
     * @return 描述
     */
    public String getRefractiveResult(BigDecimal leftSpn, BigDecimal leftCyl, BigDecimal rightSpn, BigDecimal rightCyl, Integer age) {
        return getMyopiaLevelDesc(leftSpn, leftCyl, rightSpn, rightCyl)
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
        if (ObjectsUtil.allNull(left, right)) {
            return null;
        }
        if (ObjectsUtil.allNotNull(left, right)) {
            return left.code >= right.code ? left.code : right.code;
        }
        return Objects.isNull(left) ? right.code : left.code;
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
            return between6And7GetLevel(cyl, spn, nakedVision);
        }
        if (age > 8) {
            return moreThan8GetLevel(cyl, spn, nakedVision);
        }
        return null;
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
        if (BigDecimalUtil.isBetweenRight(se, "0", "1.5")) {
            return WarningLevel.ZERO_SP;
        }
        if (BigDecimalUtil.isBetweenNo(nakedVision, "4.7", "5") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.6", "4.7") || oneSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "3", "6")) || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "2", "5")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || (age < 4 && BigDecimalUtil.isBetweenRight(se, "6", "9") || (age >= 4 && BigDecimalUtil.isBetweenRight(se, "5", "8")) || twoAbsCyl(absCyl))) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (age < 4 && BigDecimalUtil.moreThan(se, "9")) || (age >= 4 && BigDecimalUtil.moreThan(se, "8")) || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
        }
        return null;
    }

    /**
     * 6到7岁预警级别
     *
     * @param cyl         柱镜
     * @param spn         球镜
     * @param nakedVision 裸眼视力
     * @return {@link WarningLevel}
     */
    private WarningLevel between6And7GetLevel(BigDecimal cyl, BigDecimal spn, BigDecimal nakedVision) {
        BigDecimal se = getSphericalEquivalent(spn, cyl);
        BigDecimal absCyl = cyl.abs();
        if (Objects.isNull(se)) {
            return null;
        }
        if (BigDecimalUtil.isBetweenRight(se, "0", "1")) {
            return WarningLevel.ZERO_SP;
        }
        if (BigDecimalUtil.isBetweenNo(nakedVision, "4.8", "5") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.8") || oneSE(se) || BigDecimalUtil.isBetweenRight(se, "1.5", "4.5") || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.6") || twoSE(se) || BigDecimalUtil.isBetweenRight(se, "4.5", "7.5") || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if ((BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5")) || threeSE(se) || BigDecimalUtil.moreThan(se, "7.5") || threeAbsCyl(absCyl)) {
            return WarningLevel.THREE;
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
        if (BigDecimalUtil.isBetweenNo(nakedVision, "4.9", "5") || zeroSE(se) || zeroAbsCyl(absCyl)) {
            return WarningLevel.ZERO;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.7", "4.9") || oneSE(se) || (BigDecimalUtil.isBetweenRight(se, "0.5", "3")) || oneAbsCyl(absCyl)) {
            return WarningLevel.ONE;
        }
        if (BigDecimalUtil.isBetweenRight(nakedVision, "4.5", "4.7") || twoSE(se) || (BigDecimalUtil.isBetweenRight(se, "3", "6")) || twoAbsCyl(absCyl)) {
            return WarningLevel.TWO;
        }
        if (BigDecimalUtil.lessThanAndEqual(nakedVision, "4.5") || threeSE(se) || (BigDecimalUtil.moreThan(se, "6") || threeAbsCyl(absCyl))) {
            return WarningLevel.THREE;
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
        return (BigDecimalUtil.isBetweenRight(se, "-0.5", "-0.25"));
    }

    /**
     * 1级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean oneSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenRight(se, "-3", "-0.5");
    }

    /**
     * 2级预警等效球镜
     *
     * @param se 等效球镜
     * @return 是否满足条件
     */
    private boolean twoSE(BigDecimal se) {
        return BigDecimalUtil.isBetweenRight(se, "-6", "-3");
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
}
