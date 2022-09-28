package com.wupol.myopia.business.core.school.constant;

import lombok.Getter;

/**
 * 学校员工类型
 *
 * @author Simple4H
 */
@Getter
public enum SchoolStaffTypeEnum {


    SCHOOL_DOCTOR(0, "校医");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String name;

    SchoolStaffTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
