package com.wupol.myopia.base.constant;

import lombok.Getter;

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
    ;

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
}
