package com.wupol.myopia.business.core.hospital.constant;

/**
 * @Author wulizhou
 * @Date 2022/1/10 17:40
 */
public enum CheckReferralInfoEnum {

    NOT_REFERRAL(0, "未有检查前转诊"),
    HAS_REFERRAL(1, "检查前有转诊信息"),

    ;
    /**
     * 状态
     **/
    private final Integer status;
    /**
     * 描述
     **/
    private final String name;

    CheckReferralInfoEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getName() {
        return this.name;
    }


}
