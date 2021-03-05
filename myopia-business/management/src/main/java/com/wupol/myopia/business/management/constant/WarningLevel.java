package com.wupol.myopia.business.management.constant;

import java.util.Arrays;

public enum WarningLevel {
    NORMAL(-1),
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3);

    public final int code;

    WarningLevel(int code) {
        this.code = code;
    }

    public static WarningLevel get(int code) {
        return Arrays.stream(WarningLevel.values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static boolean isExpectedCode(int actualCode, int expectedCode) {
        return actualCode == expectedCode;
    }
}
