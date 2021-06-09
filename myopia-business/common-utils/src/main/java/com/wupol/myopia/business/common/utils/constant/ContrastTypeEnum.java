package com.wupol.myopia.business.common.utils.constant;

import java.util.Arrays;

/**
 * 统计报表数据对比类型
 */
public enum ContrastTypeEnum {
    NOTIFICATION(0, "通知"),
    PLAN(1, "计划"),
    TASK(2, "任务");

    /**
     * 代码
     */
    public final Integer code;

    /**
     * 描述
     */
    public final String desc;

    ContrastTypeEnum(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static ContrastTypeEnum get(Integer code) {
        return Arrays.stream(ContrastTypeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
