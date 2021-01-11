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
    SUPER_ADMIN(0, "平台管理员"),
    GOVERNMENT_DEPARTMENT_ADMIN(1, "政府部门管理员"),
    GOVERNMENT_DEPARTMENT_NORMAL_USER(2, "政府部门人员"),
    SCREENING_ORGANIZATION_ADMIN(3, "筛查机构管理员"),
    SCREENING_ORGANIZATION_NORMAL_USER(4, "筛查人员");

    /**
     * 系统编号
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
        return Arrays.stream(values()).filter(roleType -> roleType.getType().equals(type)).findFirst().orElse(null);
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
