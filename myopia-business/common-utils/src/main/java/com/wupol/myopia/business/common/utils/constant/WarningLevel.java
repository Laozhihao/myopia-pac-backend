package com.wupol.myopia.business.common.utils.constant;

import java.util.Arrays;
import java.util.Objects;

public enum WarningLevel {
    NORMAL(-1, ""),
    ZERO(0, "0级预警"),
    ONE(1, "1级预警"),
    TWO(2, "2级预警"),
    THREE(3, "3级预警");

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
