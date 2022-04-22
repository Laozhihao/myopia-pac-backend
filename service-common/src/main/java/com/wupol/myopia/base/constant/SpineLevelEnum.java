package com.wupol.myopia.base.constant;

/**
 * 脊柱弯曲
 *
 * @author Simple4H
 */
public enum SpineLevelEnum {

    ONE_LEVEL(1, "Ⅰ"),
    TWO_LEVEL(2, "Ⅱ"),
    THREE_LEVEL(3, "Ⅲ");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String name;

    SpineLevelEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getLevelName(Integer type) {
        if (ONE_LEVEL.type.equals(type)) {
            return ONE_LEVEL.name;
        }
        if (TWO_LEVEL.type.equals(type)) {
            return TWO_LEVEL.name;
        }
        if (THREE_LEVEL.type.equals(type)) {
            return THREE_LEVEL.name;
        }
        return "";
    }
}
