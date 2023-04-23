package com.wupol.myopia.business.common.utils.constant;

/**
 * 绑定状态
 *
 * @author Simple4H
 */
public enum BindStatusEnum {

    UN_BIND(0, "未绑定"),
    WAIT_BIND(1, "等待绑定"),
    BIND(2, "绑定"),
    UNTIE(3, "解绑");

    public final Integer type;
    public final String desc;

    BindStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
