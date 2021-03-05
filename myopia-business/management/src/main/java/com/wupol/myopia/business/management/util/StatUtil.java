package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.WarningLevel;

/**
 * 筛查结论计算工具
 */
public class StatUtil {
    /**
     * 是否近视
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isMyopia(Float sphere, Float cylinder) {
        return getMyopiaWarningLevel(sphere, cylinder).code > 0 ? true : false;
    }

    /**
     * 是否近视
     * @param myopiaWarningLevel 近视预警级别
     * @return
     */
    public static boolean isMyopia(WarningLevel myopiaWarningLevel) {
        return isWarningLevelGreatThanZero(myopiaWarningLevel);
    }

    /**
     * 是否远视
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param age 年龄
     * @return
     */
    public static boolean isHyperopia(Float sphere, Float cylinder, Integer age) {
        return getHyperopiaWarningLevel(sphere, cylinder, age).code > 0 ? true : false;
    }

    /**
     * 是否远视
     * @param hyperopiaWarningLevel 远视预警级别
     * @return
     */
    public static boolean isHyperopia(WarningLevel hyperopiaWarningLevel) {
        return isWarningLevelGreatThanZero(hyperopiaWarningLevel);
    }

    /**
     * 是否散光
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isAstigmatism(Float cylinder) {
        return getAstigmatismWarningLevel(cylinder).code > 0 ? true : false;
    }

    /**
     * 是否散光
     * @param astigmatismWarningLevel 散光预警级别
     * @return
     */
    public static boolean isAstigmatism(WarningLevel astigmatismWarningLevel) {
        return isWarningLevelGreatThanZero(astigmatismWarningLevel);
    }

    /**
     * 是否视力低下
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static boolean isLowVision(Float nakedVision, Integer age) {
        return getNakedVisionWarningLevel(nakedVision, age).code > 0 ? true : false;
    }

    /**
     * 是否视力低下
     * @param lowVisionWarningLevel 视力低下预警级别
     * @return
     */
    public static boolean isLowVision(WarningLevel lowVisionWarningLevel) {
        return isWarningLevelGreatThanZero(lowVisionWarningLevel);
    }

    /**
     * 是否建议就诊
     * @param nakedVision 裸眼视力
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param isWearingGlasses
     * @param correctVision 矫正视力
     * @param age 年龄
     * @param schoolAge 学龄
     * @return
     */
    public static boolean isRecommendVisit(float nakedVision, float sphere, float cylinder,
            boolean isWearingGlasses, float correctVision, int age, SchoolAge schoolAge) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        if (nakedVision < 4.9) {
            if (isWearingGlasses) {
                if (correctVision < 4.9) return true;
            } else {
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
                    default:
                }
            }
        } else if (age >= 6 && se >= 2) {
            return true;
        }
        return false;
    }

    /**
     * 判断预警级别是否大于0
     * @param warningLevel 预警级别
     * @return
     */
    private static boolean isWarningLevelGreatThanZero(WarningLevel warningLevel) {
        return warningLevel.code > 0 ? true : false;
    }

    /**
     * 返回裸眼视力预警级别
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static WarningLevel getNakedVisionWarningLevel(Float nakedVision, Integer age) {
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
     * 返回远视预警级别
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param age 年龄
     * @return
     */
    public static WarningLevel getHyperopiaWarningLevel(Float sphere, Float cylinder, Integer age) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (se > 3f && se <= 3.5f) return WarningLevel.ZERO;
                if (se > 3.5f && se <= 6f) return WarningLevel.ONE;
                if (se > 6f && se <= 9f) return WarningLevel.TWO;
                if (se > 9f) return WarningLevel.THREE;
                break;
            case 4:
            case 5:
                if (se > 2.0f && se <= 2.5f) return WarningLevel.ZERO;
                if (se > 2.5f && se <= 5.0f) return WarningLevel.ONE;
                if (se > 5.0f && se <= 8.0f) return WarningLevel.TWO;
                if (se > 8.0f) return WarningLevel.THREE;
                break;
            case 6:
            case 7:
                if (se > 1.5f && se <= 2.0f) return WarningLevel.ZERO;
                if (se > 2.0f && se <= 4.5f) return WarningLevel.ONE;
                if (se > 4.5f && se <= 7.5f) return WarningLevel.TWO;
                if (se > 7.5f) return WarningLevel.THREE;
                break;
            case 8:
                if (se > 1.0f && se <= 1.5f) return WarningLevel.ZERO;
                if (se > 1.5f && se <= 4.0f) return WarningLevel.ONE;
                if (se > 4.0f && se <= 7.0f) return WarningLevel.TWO;
                if (se > 7.0f) return WarningLevel.THREE;
                break;
            case 9:
                if (se > 0.75f && se <= 1.25f) return WarningLevel.ZERO;
                if (se > 1.25f && se <= 3.75f) return WarningLevel.ONE;
                if (se > 3.75f && se <= 6.75f) return WarningLevel.TWO;
                if (se > 6.75f) return WarningLevel.THREE;
                break;
            case 10:
                if (se > 0.5f && se <= 1.0f) return WarningLevel.ZERO;
                if (se > 1.0f && se <= 3.5f) return WarningLevel.ONE;
                if (se > 3.5f && se <= 6.5f) return WarningLevel.TWO;
                if (se > 6.5f) return WarningLevel.THREE;
                break;
            case 11:
                if (se > 0.5f && se <= 0.75f) return WarningLevel.ZERO;
                if (se > 0.75f && se <= 3.25f) return WarningLevel.ONE;
                if (se > 3.25f && se <= 6.25f) return WarningLevel.TWO;
                if (se > 6.25f) return WarningLevel.THREE;
                break;
            default:
                if (se > 0.25f && se <= 0.5f) return WarningLevel.ZERO;
                if (se > 0.5f && se <= 3.0f) return WarningLevel.ONE;
                if (se > 3.0f && se <= 6.0f) return WarningLevel.TWO;
                if (se > 6.0f) return WarningLevel.THREE;
        }
        return WarningLevel.NORMAL;
    }

    /**
     * 返回近视预警级别
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static WarningLevel getMyopiaWarningLevel(Float sphere, Float cylinder) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        if (se >= -0.5f && se <= -0.25f) return WarningLevel.ZERO;
        if (se >= -3.0f && se < -0.5f) return WarningLevel.ONE;
        if (se >= -6.0f && se < -3.0f) return WarningLevel.TWO;
        if (se < -6.0f) return WarningLevel.THREE;
        return WarningLevel.NORMAL;
    }

    /**
     * 返回散光预警级别
     * @param cylinder 柱镜
     * @return
     */
    public static WarningLevel getAstigmatismWarningLevel(Float cylinder) {
        Float cylinderAbs = Math.abs(cylinder);
        if (cylinderAbs >= 0.25f && cylinderAbs <= 0.5f) return WarningLevel.ZERO;
        if (cylinderAbs > 0.5f && cylinderAbs <= 2.0f) return WarningLevel.ONE;
        if (cylinderAbs > 2.0f && cylinderAbs <= 4.0f) return WarningLevel.TWO;
        if (cylinderAbs > 4.0f) return WarningLevel.THREE;
        return WarningLevel.NORMAL;
    }

    /**
     * 是否屈光不正
     * @param isAstigmatism 是否散光
     * @param isMyopia 是否近视
     * @param isHyperopia 是否远视
     * @return
     */
    public static boolean isRefractiveError(
            boolean isAstigmatism, boolean isMyopia, boolean isHyperopia) {
        return isAstigmatism && isMyopia && isHyperopia;
    }

    /**
     * 是否屈光不正
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param age
     * @return
     */
    public static boolean isRefractiveError(Float sphere, Float cylinder, Integer age) {
        return isAstigmatism(cylinder) && isMyopia(sphere, cylinder)
                && isHyperopia(sphere, cylinder, age);
    }

    /**
     * 计算等效球镜
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static Float getSphericalEquivalent(Float sphere, Float cylinder) {
        return cylinder / 2 + sphere;
    }
}
