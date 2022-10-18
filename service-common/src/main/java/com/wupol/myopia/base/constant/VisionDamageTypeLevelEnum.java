package com.wupol.myopia.base.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 盲及视力损害分类
 * @author hang.yuan 2022/10/17 19:15
 */
public enum VisionDamageTypeLevelEnum {

    NOTHING(0,"无"),
    LIGHT(1,"轻度"),
    MIDDLE (2,"中度"),
    HIGH(3,"重度"),
    BLINDNESS(4,"盲症");

    /**
     * 类型
     **/
    @Getter
    private Integer code;
    /**
     * 描述
     **/
    @Getter
    private String desc;

    VisionDamageTypeLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getByCode(Object code,String defaultValue){
        return Arrays.stream(values())
                .filter(item-> Objects.equals(code,item.getCode()))
                .findFirst()
                .map(VisionDamageTypeLevelEnum::getDesc)
                .orElse(defaultValue);
    }
}
