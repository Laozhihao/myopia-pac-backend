package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
/**
 * 等级预警枚举
 *
 * @author hang.yuan
 * @date 2022/5/10
 */
public enum WarningLevel {
    /**
     *
     */
    NORMAL(-1, ""),
    ZERO(0, "0级预警"),
    ONE(1, "1级预警"),
    TWO(2, "2级预警"),
    THREE(3, "3级预警"),
    ZERO_SP(4, "0级预警 远视储备不足");

    @Getter
    public final Integer code;
    public final String desc;

    WarningLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WarningLevel get(int code) {
        return Arrays.stream(WarningLevel.values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        WarningLevel warningLevel = get(code);
        return Objects.isNull(warningLevel) ? "" : warningLevel.desc;
    }

    public static boolean isExpectedCode(int actualCode, int expectedCode) {
        return actualCode == expectedCode;
    }
}
