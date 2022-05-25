package com.wupol.myopia.business.api.management.constant;

import lombok.Getter;

import java.util.Arrays;

/**
 * 年龄段枚举
 *
 * @author hang.yuan 2022/5/25 12:08
 */
public enum AgeSegmentEnum {
    /** **/
    SIX(6, "年龄<6"),
    EIGHT(8, "6≤年龄<8"),
    TEN(10, "8≤年龄<10"),
    TWELVE(12, "10≤年龄<12"),
    FOURTEEN(14, "12≤年龄<14"),
    SIXTEEN(16, "14≤年龄<16"),
    EIGHTEEN(18, "16≤年龄<18"),
    MORE_EIGHTEEN(19, "年龄≥18");

    /**
     * 学龄段临界点
     */
    @Getter
    private final Integer code;

    /**
     * 学龄段描述
     */
    @Getter
    private final String desc;

    AgeSegmentEnum(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static AgeSegmentEnum get(Integer code){
        return Arrays.stream(AgeSegmentEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
