package com.wupol.myopia.business.management.constant;

import java.util.Arrays;

public enum VisionCorrection {
    NORMAL(0, "正常"),
    UNCORRECTED(1, "未矫"),
    UNDER_CORRECTED(2, "欠矫"),
    ENOUGH_CORRECTED(3, "足矫"),
    OVER_CORRECTED(4, "过矫"),
    CORRECTED(5, "矫正");

    /** 学龄段ID */
    public final Integer code;

    /** 学龄段描述 */
    public final String desc;

    VisionCorrection(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static VisionCorrection get(Integer code) {
        return Arrays.stream(VisionCorrection.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
