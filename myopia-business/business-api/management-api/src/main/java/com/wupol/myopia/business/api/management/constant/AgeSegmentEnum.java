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
    AGE_6(6, "6岁以下","*<6"),
    AGE_7(7, "6岁","6≤*<7"),
    AGE_8(8, "7岁","7≤*<8"),
    AGE_9(9, "8岁","8≤*<9"),
    AGE_10(10, "9岁","9≤*<10"),
    AGE_11(11, "10岁","10≤*<11"),
    AGE_12(12, "11岁","11≤*<12"),
    AGE_13(13, "12岁","12≤*<13"),
    AGE_14(14, "13岁","13≤*<14"),
    AGE_15(15, "14岁","14≤*<15"),
    AGE_16(16, "15岁","15≤*<16"),
    AGE_17(17, "16岁","16≤*<17"),
    AGE_18(18, "17岁","17≤*<18"),
    AGE_19(19, "18岁及以上","*≥18");

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
    @Getter
    private final String descEn;

    AgeSegmentEnum(Integer code, String desc,String descEn) {
        this.desc = desc;
        this.code = code;
        this.descEn = descEn;
    }

    public static AgeSegmentEnum get(Integer code){
        return Arrays.stream(AgeSegmentEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
