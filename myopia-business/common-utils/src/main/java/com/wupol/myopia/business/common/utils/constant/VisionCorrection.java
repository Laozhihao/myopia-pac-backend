package com.wupol.myopia.business.common.utils.constant;

import java.util.Arrays;
import java.util.Objects;

public enum VisionCorrection {
    NORMAL(0, "正常"),
    UNCORRECTED(1, "未矫"),
    UNDER_CORRECTED(2, "欠矫"),
    ENOUGH_CORRECTED(3, "足矫"),
    OVER_CORRECTED(4, "过矫");

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

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        VisionCorrection visionCorrection = get(code);
        return Objects.isNull(visionCorrection) ? "" : visionCorrection.desc;
    }
}
