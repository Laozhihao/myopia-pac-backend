package com.wupol.myopia.business.core.hospital.constant;

/**
 * @Author wulizhou
 * @Date 2022/1/10 17:40
 */
public enum CheckReferralInfoEnum {

    NOT_REFERRAL(false, "未有检查前转诊"),
    HAS_REFERRAL(true, "检查前有转诊信息"),

    ;
    /**
     * 状态
     **/
    private final Boolean status;
    /**
     * 描述
     **/
    private final String name;

    CheckReferralInfoEnum(Boolean status, String name) {
        this.status = status;
        this.name = name;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public String getName() {
        return this.name;
    }


}
