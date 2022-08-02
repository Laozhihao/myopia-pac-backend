package com.wupol.myopia.business.core.school.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 片区类型
 *
 * @author hang.yuan 2022/7/28 13:37
 */
public enum  AreaTypeEnum {
    GOOD(1,"好片"),
    MIDDLE(2,"中片"),
    BAD(3,"差片");

    /**
     * 类型
     **/
    @Getter
    private final Integer type;
    /**
     * 描述
     **/
    @Getter
    private final String name;

    AreaTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static AreaTypeEnum get(Integer type){
        return Arrays.stream(values())
                .filter(item-> Objects.equals(item.getType(),type))
                .findFirst().orElse(null);
    }
}
