package com.wupol.myopia.business.core.school.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 监测点类型
 *
 * @author hang.yuan 2022/7/28 13:41
 */
public enum  MonitorTypeEnum {

    URBAN_AREA(1,"城区"),
    SUBURBAN_COUNTY(2,"郊县");

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

    MonitorTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static MonitorTypeEnum get(Integer type){
        return Arrays.stream(values())
                .filter(item-> Objects.equals(item.getType(),type))
                .findFirst().orElse(null);
    }
}
