package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.domain.dto.stat.StatVerdict;

public class StatUtil {
    /**
     * 获取统计结论数据
     * @param sphericalLens 球镜
     * @param cylinderLens 柱镜
     * @return
     */
    public static StatVerdict getStatVerdict(
            Float sphericalLens, Float cylinderLens, Boolean isWearingGlasses, Integer age) {
        return StatVerdict.builder().isWearingGlasses(isWearingGlasses).build();
    }

    /**
     * 获取统计结论数据
     * @param nakedVision 裸眼视力
     * @return
     */
    public static StatVerdict getStatVerdict(
            Float nakedVision, Boolean isWearingGlasses, Integer age) {
        return StatVerdict
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
     * 是否视力低下
     * @param nakedVision 裸眼视力
     * @return
     */
    public static boolean isLowVision(Float nakedVision, Integer age) {
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
