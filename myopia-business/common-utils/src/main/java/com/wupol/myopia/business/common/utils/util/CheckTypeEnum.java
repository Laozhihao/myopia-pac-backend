package com.wupol.myopia.business.common.utils.util;

/**
 * 筛查模式
 *
 * @author Simple4H
 */
public enum CheckTypeEnum {

    UNKNOWN(-1, "未知"),
    INDIVIDUAL(0, "个体筛查"),
    BATCH(1, "批量筛查");

    /**
     * 类型
     **/
    public final Integer type;
    /**
     * 描述
     **/
    public final String desc;

    CheckTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getName(Integer type) {
        if (INDIVIDUAL.type.equals(type)) return INDIVIDUAL.desc;
        if (BATCH.type.equals(type)) return BATCH.desc;
        return UNKNOWN.desc;
    }
}
