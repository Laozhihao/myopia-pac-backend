package com.wupol.myopia.base.constant;

import lombok.Getter;

/**
 * 用户类型
 *
 * @Author HaoHao
 * @Date 2021/1/25
 **/
@Getter
public enum UserType {
    /** 用户类型 */
    PLATFORM_ADMIN(0, "平台管理员"),
    NOT_PLATFORM_ADMIN(1, "非平台管理员");

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
