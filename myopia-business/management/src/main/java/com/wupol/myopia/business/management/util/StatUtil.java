package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.domain.dto.stat.StatConclusion;

public class StatUtil {
    /**
     * 获取统计结论数据
     * @param sphericalLens 球镜
     * @param cylinderLens 柱镜
     * @return
     */
    public static StatConclusion getStatVerdict(
            Float sphericalLens, Float cylinderLens, Boolean isWearingGlasses, Integer age) {
        return StatConclusion.builder().isWearingGlasses(isWearingGlasses).build();
    }

    /**
     * 获取统计结论数据
     * @param nakedVision 裸眼视力
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
     * @param sphericalLens 球镜
     * @return
     */
    public static boolean isMyopia(Float sphericalLens) {
        if (sphericalLens < -0.5f) {
            return true;
        }
        return false;
    }

    /**
     * 是否远视
     */
    public static boolean isHyperopia(Float sphericalLens, Integer age)
            throws NumberFormatException {
        if (age == null || age < 0) {
            throw new NumberFormatException("wrong number");
        }
        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (sphericalLens > 3.5f) {
                    return true;
                }
            case 4:
            case 5:
                if (sphericalLens > 2.5f) {
                    return true;
                }
            case 6:
            case 7:
                if (sphericalLens > 2.0f) {
                    return true;
                }
            case 8:
                if (sphericalLens > 1.0f) {
                    return true;
                }
            case 9:
                if (sphericalLens > 0.75f) {
                    return true;
                }
            case 10:
            case 11:
                if (sphericalLens > 0.5f) {
                    return true;
                }
            default:
                if (sphericalLens > 0.25f) {
                    return true;
                }
        }
        return false;
    }

    /**
     * 是否视力低下
     * @param nakedVision 裸眼视力
     * @return
     */
    public static boolean isLowVision(Float nakedVision, Integer age) {
        if (age < 3 && nakedVision <= 4.6) {
            return true;
        }
        if ((age <= 5 && age >= 3) && nakedVision <= 4.7) {
            return true;
        }
        if ((age == 7 || age == 6) && nakedVision <= 4.8) {
            return true;
        }
        if (age >= 8 && nakedVision <= 4.9) {
            return true;
        }
        return false;
    }

    /**
     * 是否屈光不正
     * @param sphericalLens
     * @param cylinderLens
     * @param age
     * @return
     */
    public static boolean isRefractiveError(Float sphericalLens, Float cylinderLens, Integer age) {
        if (Math.abs((double) cylinderLens) > 0.5) {
            return true;
        }
        if (age >= 3 || age <= 5) {
        }
        return false;
    }
}
