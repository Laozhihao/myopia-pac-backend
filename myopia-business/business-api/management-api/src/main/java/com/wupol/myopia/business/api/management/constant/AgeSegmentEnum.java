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
    Age_6(6, "6岁以下","*<6"),
    Age_7(7, "6岁","6≤*<7"),
    Age_8(8, "7岁","7≤*<8"),
    Age_9(9, "8岁","8≤*<9"),
    Age_10(10, "9岁","9≤*<10"),
    Age_11(11, "10岁","10≤*<11"),
    Age_12(12, "11岁","11≤*<12"),
    Age_13(13, "12岁","12≤*<13"),
    Age_14(14, "13岁","13≤*<14"),
    Age_15(15, "14岁","14≤*<15"),
    Age_16(16, "15岁","15≤*<16"),
    Age_17(17, "16岁","16≤*<17"),
    Age_18(18, "17岁","17≤*<18"),
    Age_19(19, "18岁及以上","*≥18");

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
