package com.wupol.myopia.business.common.utils.constant;

import java.util.Arrays;
import java.util.Objects;

/**
 * 远视等级
 *
 * @author Simple4H
 */
public enum HyperopiaLevelEnum {

    ZERO(0, "正常"),
    HYPEROPIA(1, "远视"),
    HYPEROPIA_LEVEL_LIGHT(2, "低度远视"),
    HYPEROPIA_LEVEL_MIDDLE(3, "中度远视"),
    HYPEROPIA_LEVEL_HIGH(4, "重度远视");

    public final Integer code;
    public final String desc;

    HyperopiaLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static HyperopiaLevelEnum get(int code) {
        return Arrays.stream(HyperopiaLevelEnum.values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        HyperopiaLevelEnum hyperopiaLevelEnum = get(code);
        return Objects.isNull(hyperopiaLevelEnum) ? "" : hyperopiaLevelEnum.desc;
    }
}
