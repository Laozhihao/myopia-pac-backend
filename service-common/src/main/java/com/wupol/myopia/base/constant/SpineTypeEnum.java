package com.wupol.myopia.base.constant;

/**
 * 脊柱弯曲
 *
 * @author Simple4H
 */
public enum SpineTypeEnum {

    ONE_TYPE(1, "无侧弯"),
    TWO_TYPE(2, "左低右高"),
    THREE_TYPE(3, "右低左高");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String name;

    SpineTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getTypeName(Integer type) {
        if (ONE_TYPE.type.equals(type)) {
            return ONE_TYPE.name;
        }
        if (TWO_TYPE.type.equals(type)) {
            return TWO_TYPE.name;
        }
        if (THREE_TYPE.type.equals(type)) {
            return THREE_TYPE.name;
        }
        return "";
    }
}
