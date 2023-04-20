package com.wupol.myopia.business.common.utils.constant;

/**
 * 护眼宝绑定状态
 *
 * @author Simple4H
 */
public enum HybBindStatusEnum {

    UN_BIND(0, "未绑定"),
    BIND(1, "绑定"),
    UNTIE(2, "解绑");

    public final Integer type;
    public final String desc;

    HybBindStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
