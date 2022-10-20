package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

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
    HYPEROPIA_LEVEL_HIGH(4, "高度远视");

    @Getter
    public final Integer code;
    @Getter
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

    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(code)
                .map(HyperopiaLevelEnum::get)
                .map(HyperopiaLevelEnum::getDesc)
                .orElse(StrUtil.EMPTY);
    }
}
