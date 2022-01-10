package com.wupol.myopia.business.core.hospital.constant;

/**
 * @Author wulizhou
 * @Date 2022/1/7 17:45
 */
public enum MonthAgeStatusEnum {

    AGE_STAGE_STATUS_DISABLE(1, "无法进行检查（非检查年龄段）"),
    AGE_STAGE_STATUS_NOT_DATA(2, "可检查，还没有检查数据"),
    AGE_STAGE_STATUS_CAN_UPDATE(3, "可检查，有检查数据且可修改"),
    AGE_STAGE_STATUS_CURRENT(4, "当前检查"),
    AGE_STAGE_STATUS_INVISIBLE(5, "未使用状态"),
    AGE_STAGE_STATUS_CANNOT_UPDATE(6, "已检查，有检查数据且不可修改"),


    ;
    /**
     * 状态
     **/
    private final Integer status;
    /**
     * 描述
     **/
    private final String name;

    MonthAgeStatusEnum(Integer status, String name) {
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
