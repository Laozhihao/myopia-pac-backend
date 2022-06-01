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
    Age_6(6, "年龄<6"),
    Age_7(7, "6≤年龄<7"),
    Age_8(8, "7≤年龄<8"),
    Age_9(9, "8≤年龄<9"),
    Age_10(10, "9≤年龄<10"),
    Age_11(11, "10≤年龄<11"),
    Age_12(12, "11≤年龄<12"),
    Age_13(13, "12≤年龄<13"),
    Age_14(14, "13≤年龄<14"),
    Age_15(15, "14≤年龄<15"),
    Age_16(16, "15≤年龄<16"),
    Age_17(17, "16≤年龄<17"),
    Age_18(18, "17≤年龄<18"),
    Age_19(19, "年龄≥18");

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
