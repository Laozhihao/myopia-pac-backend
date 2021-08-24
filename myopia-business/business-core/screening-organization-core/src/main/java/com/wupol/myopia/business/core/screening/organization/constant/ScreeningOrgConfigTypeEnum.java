package com.wupol.myopia.business.core.screening.organization.constant;


import java.util.Arrays;
import java.util.Objects;

/**
 * 机构相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum ScreeningOrgConfigTypeEnum {

    CONFIG_TYPE_0(0, "省级配置"),
    CONFIG_TYPE_1(1, "单点配置"),
    CONFIG_TYPE_2(2, "VS666"),
    CONFIG_TYPE_3(3, "单点配置+VS666");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String name;

    ScreeningOrgConfigTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 根据类型获取描述
     */
    public static String getTypeName(Integer type) {
        ScreeningOrgConfigTypeEnum h = Arrays.stream(ScreeningOrgConfigTypeEnum.values())
                .filter(item -> item.type.equals(type)).findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }

    public Integer getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}

