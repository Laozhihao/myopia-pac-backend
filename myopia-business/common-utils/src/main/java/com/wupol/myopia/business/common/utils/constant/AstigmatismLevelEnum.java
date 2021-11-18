package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

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
    ASTIGMATISM_LEVEL_HIGH(3, "重度散光");

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

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        AstigmatismLevelEnum astigmatismLevelEnum = get(code);
        return Objects.isNull(astigmatismLevelEnum) ? "" : astigmatismLevelEnum.desc;
    }
}
