package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.domain.dto.stat.StatConclusion;

public class StatUtil {
    /**
     * 获取统计结论数据
     * @param sphere 球镜
     * @param cylinder 柱镜
     * @return
     */
    public static StatConclusion getStatVerdict(
            Float sphere, Float cylinder, Boolean isWearingGlasses, Integer age) {
        return StatConclusion.builder().isWearingGlasses(isWearingGlasses).build();
    }

    /**
     * 获取统计结论数据
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static StatConclusion getStatVerdict(
            Float nakedVision, Boolean isWearingGlasses, Integer age) {
        return StatConclusion
                .builder()
                // .isAstigmatism(isAstigmatism)
                .isWearingGlasses(isWearingGlasses)
                .build();
    }

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
        Float se = getSphericalEquivalent(sphere, cylinder);
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (se > 3.5f) return true;
            case 4:
            case 5:
                if (se > 2.5f) return true;
            case 6:
            case 7:
                if (se > 2.0f) return true;
            case 8:
                if (se > 1.0f) return true;
            case 9:
                if (se > 0.75f) return true;
            case 10:
            case 11:
                if (se > 0.5f) return true;
            default:
                if (se > 0.25f) return true;
        }
        return false;
    }

    /**
     * 是否散光
     * @param cylinder 柱镜
     * @return
     */
    public static boolean isAstigmatism(Float cylinder) {
        if (cylinder > 0.5f || cylinder < -0.5f) {
            return true;
        }
        return false;
    }


    /**
     * 是否视力低下
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static boolean isLowVision(Float nakedVision, Integer age) {
        switch (age) {
            case 0:
            case 1:
            case 2:
                if (nakedVision <= 4.6) return true;
            case 3:
            case 4:
            case 5:
                if (nakedVision <= 4.7) return true;
            case 6:
            case 7:
                if (nakedVision <= 4.8) return true;
            default:
                if (nakedVision <= 4.9) return true;
        }
        return false;
    }

    /**
     * 是否屈光不正
     * @param sphere
     * @param cylinder
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
