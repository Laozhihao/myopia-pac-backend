package com.wupol.myopia.business.core.device.constant;

import lombok.Getter;

import java.util.Arrays;

/**
 * 统计报表数据对比类型
 */
@Getter
public enum OrgTypeEnum {
    SCREENING(0, "筛查机构"),
    HOSPITAL(1, "医院"),
    SCHOOL(2, "学校");

    /**
     * 代码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

    OrgTypeEnum(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static OrgTypeEnum get(Integer code) {
        return Arrays.stream(OrgTypeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
