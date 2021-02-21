package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.constant.WarningLevel;

public class StatUtil {
    /**
     * 是否近视
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isMyopia(Float sphere, Float cylinder) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        if (se < -0.5f) {
            return true;
        }
        return false;
    }

    /**
     * 是否远视
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param age 年龄
     * @return
     */
    public static boolean isHyperopia(Float sphere, Float cylinder, Integer age) {
        return getHyperopiaWarningLevel(sphere, cylinder, age) > 0 ? true : false;
    }

    /**
     * 是否散光
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isAstigmatism(Float cylinder) {
        return getAstigmatismWarningLevel(cylinder) > 0 ? true : false;
    }

    /**
     * 是否视力低下
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static boolean isLowVision(Float nakedVision, Integer age) {
        return getNakedVisionWarningLevel(nakedVision, age) > 0 ? true : false;
    }

    /**
     * 返回裸眼视力预警级别
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static Integer getNakedVisionWarningLevel(Float nakedVision, Integer age) {
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                if (nakedVision > 4.7f && nakedVision < 5.0f) return WarningLevel.ZERO.code;
                if (nakedVision > 4.6f && nakedVision <= 4.7f) return WarningLevel.ONE.code;
                if (nakedVision > 4.5f && nakedVision <= 4.6f) return WarningLevel.TWO.code;
                if (nakedVision <= 4.5f) return WarningLevel.THREE.code;
                break;
            case 6:
            case 7:
                if (nakedVision > 4.8f && nakedVision < 5.0f) return WarningLevel.ZERO.code;
                if (nakedVision > 4.7f && nakedVision <= 4.8f) return WarningLevel.ONE.code;
                if (nakedVision > 4.5f && nakedVision <= 4.7f) return WarningLevel.TWO.code;
                if (nakedVision <= 4.5f) return WarningLevel.THREE.code;
                break;
            default:
                if (nakedVision > 4.9f && nakedVision < 5.0f) return WarningLevel.ZERO.code;
                if (nakedVision > 4.7f && nakedVision <= 4.9f) return WarningLevel.ONE.code;
                if (nakedVision > 4.5f && nakedVision <= 4.7f) return WarningLevel.TWO.code;
                if (nakedVision <= 4.5f) return WarningLevel.THREE.code;
        }
        return WarningLevel.NORMAL.code;
    }

    /**
     * 返回远视预警级别
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @param age 年龄
     * @return
     */
    public static Integer getHyperopiaWarningLevel(Float sphere, Float cylinder, Integer age) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (se > 3f && se <= 3.5f) return WarningLevel.ZERO.code;
                if (se > 3.5f && se <= 6f) return WarningLevel.ONE.code;
                if (se > 6f && se <= 9f) return WarningLevel.TWO.code;
                if (se > 9f) return WarningLevel.THREE.code;
                break;
            case 4:
            case 5:
                if (se > 2.0f && se <= 2.5f) return WarningLevel.ZERO.code;
                if (se > 2.5f && se <= 5.0f) return WarningLevel.ONE.code;
                if (se > 5.0f && se <= 8.0f) return WarningLevel.TWO.code;
                if (se > 8.0f) return WarningLevel.THREE.code;
                break;
            case 6:
            case 7:
                if (se > 1.5f && se <= 2.0f) return WarningLevel.ZERO.code;
                if (se > 2.0f && se <= 4.5f) return WarningLevel.ONE.code;
                if (se > 4.5f && se <= 7.5f) return WarningLevel.TWO.code;
                if (se > 7.5f) return WarningLevel.THREE.code;
                break;
            case 8:
                if (se > 1.0f && se <= 1.5f) return WarningLevel.ZERO.code;
                if (se > 1.5f && se <= 4.0f) return WarningLevel.ONE.code;
                if (se > 4.0f && se <= 7.0f) return WarningLevel.TWO.code;
                if (se > 7.0f) return WarningLevel.THREE.code;
                break;
            case 9:
                if (se > 0.75f && se <= 1.25f) return WarningLevel.ZERO.code;
                if (se > 1.25f && se <= 3.75f) return WarningLevel.ONE.code;
                if (se > 3.75f && se <= 6.75f) return WarningLevel.TWO.code;
                if (se > 6.75f) return WarningLevel.THREE.code;
                break;
            case 10:
                if (se > 0.5f && se <= 1.0f) return WarningLevel.ZERO.code;
                if (se > 1.0f && se <= 3.5f) return WarningLevel.ONE.code;
                if (se > 3.5f && se <= 6.5f) return WarningLevel.TWO.code;
                if (se > 6.5f) return WarningLevel.THREE.code;
                break;
            case 11:
                if (se > 0.5f && se <= 0.75f) return WarningLevel.ZERO.code;
                if (se > 0.75f && se <= 3.25f) return WarningLevel.ONE.code;
                if (se > 3.25f && se <= 6.25f) return WarningLevel.TWO.code;
                if (se > 6.25f) return WarningLevel.THREE.code;
                break;
            default:
                if (se > 0.25f && se <= 0.5f) return WarningLevel.ZERO.code;
                if (se > 0.5f && se <= 3.0f) return WarningLevel.ONE.code;
                if (se > 3.0f && se <= 6.0f) return WarningLevel.TWO.code;
                if (se > 6.0f) return WarningLevel.THREE.code;
        }
        return WarningLevel.NORMAL.code;
    }

    /**
     * 返回近视预警级别
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static Integer getMyopiaWarningLevel(Float sphere, Float cylinder) {
        Float se = getSphericalEquivalent(sphere, cylinder);
        if (se >= -0.5f && se <= -0.25f) return WarningLevel.ZERO.code;
        if (se >= -3.0f && se < -0.5f) return WarningLevel.ONE.code;
        if (se >= -6.0f && se < -3.0f) return WarningLevel.TWO.code;
        if (se < -6.0f) return WarningLevel.THREE.code;
        return WarningLevel.NORMAL.code;
    }

    /**
     * 返回散光预警级别
     * @param cylinder 柱镜
     * @return
     */
    public static Integer getAstigmatismWarningLevel(Float cylinder) {
        Float cylinderAbs = Math.abs(cylinder);
        if (cylinderAbs >= 0.25f && cylinderAbs <= 0.5f) return WarningLevel.ZERO.code;
        if (cylinderAbs > 0.5f && cylinderAbs <= 2.0f) return WarningLevel.ONE.code;
        if (cylinderAbs > 2.0f && cylinderAbs <= 4.0f) return WarningLevel.TWO.code;
        if (cylinderAbs > 4.0f) return WarningLevel.THREE.code;
        return WarningLevel.NORMAL.code;
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
