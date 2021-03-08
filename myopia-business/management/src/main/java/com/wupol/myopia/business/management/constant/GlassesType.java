package com.wupol.myopia.business.management.constant;

import java.util.Arrays;

public enum GlassesType {
    NOT_WEARING(0, "不戴镜"),
    FRAME_GLASSES(1, "框架眼镜"),
    CONTACT_LENS(2, "隐形眼镜"),
    ORTHOKERATOLOGY(3, "夜戴角膜塑形镜");

    /** 学龄段ID */
    public final Integer code;

    /** 学龄段描述 */
    public final String desc;

    GlassesType(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static GlassesType get(Integer code) {
        return Arrays.stream(GlassesType.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
