package com.wupol.myopia.business.core.school.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 学校角色类型
 *
 * @author Simple4H
 */
public enum SchoolAdminRoleTypeEnum {

    ADMIN(1, "管理员"),
    SCHOOL_DOCTOR(2, "校医");

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

    SchoolAdminRoleTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static SchoolAdminRoleTypeEnum get(Integer type) {
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getType(), type))
                .findFirst().orElse(null);
    }
}
