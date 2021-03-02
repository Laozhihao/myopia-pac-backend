package com.wupol.myopia.business.management.constant;

import java.util.Arrays;

public enum StatClassLabel {
    LOW_VISION(0, "视力低下"),
    REFRACTIVE_ERROR(1, "屈光不正"),
    WEARING_GLASSES(2, "戴镜情况"),
    MYOPIA(3, "近视情况");

    /** 学龄段ID */
    public final Integer code;

    /** 学龄段描述 */
    public final String desc;

    StatClassLabel(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static StatClassLabel get(Integer code) {
        return Arrays.stream(StatClassLabel.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
