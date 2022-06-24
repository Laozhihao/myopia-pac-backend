package com.wupol.myopia.base.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 脊柱弯曲
 *
 * @author Simple4H
 */
public enum SpineTypeEntiretyEnum {

    ONE_TYPE(1, "无前后弯曲异常"),
    TWO_TYPE(2, "平背"),
    THREE_TYPE(3, "前凸异常"),

    FOUR_TYPE(4, "后凸异常");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String name;

    SpineTypeEntiretyEnum(Integer type, String name) {
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
        return Arrays.stream(values())
                .filter(spineTypeEntiretyEnum -> spineTypeEntiretyEnum.getType().equals(type))
                .map(SpineTypeEntiretyEnum::getName)
                .findFirst().orElse(StringUtils.EMPTY);
    }
}
