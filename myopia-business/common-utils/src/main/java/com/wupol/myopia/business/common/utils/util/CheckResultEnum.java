package com.wupol.myopia.business.common.utils.util;

/**
 * 筛查模式
 *
 * @author Simple4H
 */
public enum CheckResultEnum {


    UNKNOWN(-1, "未知"),
    EXCELLENT(1, "优"),
    GOOD(2, "良"),
    POOR(3, "差");

    /**
     * 类型
     **/
    public final Integer type;
    /**
     * 描述
     **/
    public final String desc;

    CheckResultEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getName(Integer type) {
        if (EXCELLENT.type.equals(type)) return EXCELLENT.desc;
        if (GOOD.type.equals(type)) return GOOD.desc;
        if (POOR.type.equals(type)) return POOR.desc;
        return UNKNOWN.desc;
    }
}
