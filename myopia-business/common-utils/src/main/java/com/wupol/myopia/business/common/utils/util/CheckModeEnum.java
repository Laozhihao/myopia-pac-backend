package com.wupol.myopia.business.common.utils.util;

/**
 * 筛查模式
 *
 * @author Simple4H
 */
public enum CheckModeEnum {


    UNKNOWN(-1, "未知"),
    EYES(0, "双眼模式"),
    LEFT(1, "左眼模式"),
    RIGHT(2, "右眼模式");

    /**
     * 类型
     **/
    public final Integer type;
    /**
     * 描述
     **/
    public final String desc;

    CheckModeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getName(Integer type) {
        if (EYES.type.equals(type)) return EYES.desc;
        if (LEFT.type.equals(type)) return LEFT.desc;
        if (RIGHT.type.equals(type)) return RIGHT.desc;
        return UNKNOWN.desc;
    }
}
