package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.domain.dto.stat.StatVerdict;

public class StatUtil {
    /**
     *
     * @param sphericalLens 球镜
     * @param cylinderLens 柱镜
     * @return
     */
    public static StatVerdict getStatVerdict(
            Float sphericalLens, Float cylinderLens, Boolean isWearingGlasses, Integer age) {
        return StatVerdict.builder().isWearingGlasses(isWearingGlasses).build();
    }

    /**
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
     * 是否近视
     * @param nakedVision 裸眼视力
     * @param age 年龄
     * @return
     */
    public static boolean isMyopia(Float nakedVision, Integer age) {
        if ((age >= 3 || age <= 5) && nakedVision <= 4.7) {
            return true;
        }
        if ((age >= 6 || age <= 7) && nakedVision <= 4.8) {
            return true;
        }
        if (age >= 8 && nakedVision <= 4.8) {
            return true;
        }
        return false;
    }
}
