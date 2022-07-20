package com.wupol.myopia.base.constant;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Arrays;

/**
 * 管理平台端用户类型
 *
 * @Author HaoHao
 * @Date 2021/1/25
 **/
@Getter
public enum UserType {
    /** 含超级管理员 */
    OTHER(-1, "其它"),
    PLATFORM_ADMIN(0, "平台管理员"),
    GOVERNMENT_ADMIN(1, "政府人员管理员"),
    SCREENING_ORGANIZATION_ADMIN(2, "筛查机构管理员"),
    HOSPITAL_ADMIN(3, "医院管理员"),
    OVERVIEW(4, "总览机构"),

    QUESTIONNAIRE_STUDENT(0, "问卷系统学生端"),
    QUESTIONNAIRE_SCHOOL(1, "问卷系统学校端"),

    ;

    private static final ImmutableMap<Integer, Integer> MANAGEMENT_MULTISYSTEM_USERTYPE_ROLETYPE_MAP;

    static {
        MANAGEMENT_MULTISYSTEM_USERTYPE_ROLETYPE_MAP = ImmutableMap.of(
                GOVERNMENT_ADMIN.getType(), RoleType.GOVERNMENT_DEPARTMENT.getType(),
                SCREENING_ORGANIZATION_ADMIN.getType(), RoleType.SCREENING_ORGANIZATION.getType(),
                HOSPITAL_ADMIN.getType(), RoleType.HOSPITAL_ADMIN.getType(),
                OVERVIEW.getType(), RoleType.OVERVIEW_ADMIN.getType());
    }

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;

    UserType(Integer type, String descr) {
        this.type = type;
        this.msg = descr;
    }

    /**
     * 根据类型获取对应UserType
     *
     * @param type 类型
     * @return com.wupol.myopia.base.constant.UserType
     **/
    public static UserType getByType(Integer type) {
        return Arrays.stream(values()).filter(item -> item.getType().equals(type)).findFirst().orElse(null);
    }

    /**
     * 是否为平台机构管理员用户
     *
     * @param userType 用户类型
     * @return boolean
     **/
    public static boolean isPlatformOrgAdminUser(Integer userType) {
        return SCREENING_ORGANIZATION_ADMIN.getType().equals(userType) || HOSPITAL_ADMIN.getType().equals(userType)
                || OVERVIEW.getType().equals(userType);
    }

    /**
     * 获取管理端其他系统的用户角色
     * @param userType
     * @return
     */
    public static Integer getRoleTypeByMultiSystemUserType(Integer userType) {
        return MANAGEMENT_MULTISYSTEM_USERTYPE_ROLETYPE_MAP.get(userType);
    }

}
