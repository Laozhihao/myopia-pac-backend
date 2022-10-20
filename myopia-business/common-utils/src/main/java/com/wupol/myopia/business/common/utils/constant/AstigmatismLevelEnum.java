package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 散光等级
 *
 * @author Simple4H
 */
@Getter
public enum AstigmatismLevelEnum {

    ZERO(0, "正常"),
    ASTIGMATISM_LEVEL_LIGHT(1, "低度散光"),
    ASTIGMATISM_LEVEL_MIDDLE(2, "中度散光"),
    ASTIGMATISM_LEVEL_HIGH(3, "高度散光");

    public final Integer code;
    public final String desc;

    AstigmatismLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AstigmatismLevelEnum get(int code) {
        return Arrays.stream(AstigmatismLevelEnum.values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(code)
                .map(AstigmatismLevelEnum::get)
                .map(AstigmatismLevelEnum::getDesc)
                .orElse(StrUtil.EMPTY);
    }
}
