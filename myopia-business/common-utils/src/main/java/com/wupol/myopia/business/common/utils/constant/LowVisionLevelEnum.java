package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 视力低下等级
 *
 * @author hang.yuan 2022/5/12 10:36
 */
public enum LowVisionLevelEnum {
    /**
     *
     */
    ZERO(0, "正常"),
    LOW_VISION(1, "视力低下"),
    LOW_VISION_LEVEL_LIGHT(2, "轻度视力低下"),
    LOW_VISION_LEVEL_MIDDLE(3, "中度视力低下"),
    LOW_VISION_LEVEL_HIGH(4, "重度视力低下");;


    public final Integer code;
    public final String desc;

    LowVisionLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static LowVisionLevelEnum get(int code) {
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        LowVisionLevelEnum lowVisionLevelEnum = get(code);
        return Objects.isNull(lowVisionLevelEnum) ? "" : lowVisionLevelEnum.desc;
    }

    public static List<LowVisionLevelEnum> lowVisionLevelList() {
        return Lists.newArrayList(LOW_VISION_LEVEL_LIGHT, LOW_VISION_LEVEL_MIDDLE, LOW_VISION_LEVEL_HIGH);
    }
}
