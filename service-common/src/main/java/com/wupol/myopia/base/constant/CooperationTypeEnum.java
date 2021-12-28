package com.wupol.myopia.base.constant;

/**
 * 合作类型
 *
 * @author Simple4H
 */
public enum CooperationTypeEnum {

    COOPERATION_TYPE_COOPERATE(0, "合作"),
    COOPERATION_TYPE_TRY_OUT(1, "试用");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String name;

    CooperationTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String getCooperationTypeName(Integer type) {
        if (type.equals(COOPERATION_TYPE_COOPERATE.type)) {
            return COOPERATION_TYPE_COOPERATE.name;
        }
        if (type.equals(COOPERATION_TYPE_TRY_OUT.type)) {
            return COOPERATION_TYPE_TRY_OUT.name;
        }
        return "";
    }
}
