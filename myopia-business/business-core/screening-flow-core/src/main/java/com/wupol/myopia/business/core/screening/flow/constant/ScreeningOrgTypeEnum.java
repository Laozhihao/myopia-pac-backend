package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.Getter;

/**
 * 筛查机构类型
 *
 * @author hang.yuan 2022/9/14 18:49
 */
public enum ScreeningOrgTypeEnum {
    ORG(0,"筛查机构"),
    SCHOOL(1,"学校"),
    HOSPITAL(2,"医院");
    /**
     * 类型
     **/
    @Getter
    private final Integer type;
    /**
     * 描述
     **/
    @Getter
    private final String name;

    ScreeningOrgTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
