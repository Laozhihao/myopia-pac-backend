package com.wupol.myopia.base.constant;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 角色类型
 *
 * @Author HaoHao
 * @Date 2020/12/25 14:32
 **/
@Getter
public enum RoleType {
    /** 超级管理员 */
    SUPER_ADMIN(0, "平台管理员类型角色"),
    GOVERNMENT_DEPARTMENT(1, "政府部门类型角色"),
    SCREENING_ORGANIZATION(2, "筛查人员类型角色");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;

    RoleType(Integer type, String descr) {
        this.type = type;
        this.msg = descr;
    }

    /**
     * 根据类型获取对应RoleType
     *
     * @param type 类型
     * @return com.wupol.myopia.base.constan.RoleType
     **/
    public static RoleType getByType(Integer type) {
        return Arrays.stream(values()).filter(roleType -> roleType.getType().equals(type))
                .findFirst().orElse(null);
    }

    /**
     * 获取所有Type
     *
     * @return java.util.List<java.lang.Object>
     **/
    public static List<Object> getAllType() {
        return EnumUtil.getFieldValues(RoleType.class, "type");
    }
}
