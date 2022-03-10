package com.wupol.myopia.base.constant;

import lombok.Getter;

/**
 * 总览机构配置类型
 *
 * @Author wulizhou
 * @Date 2022/02/17 20:52
 **/
@Getter
public enum OverviewConfigType {

    SCREENING_ORG(0, "配置筛查机构"),
    HOSPITAL(1, "配置医院"),
    SCREENING_ORG_HOSPITAL(2, "配置筛查机构+医院");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;

    OverviewConfigType(Integer type, String descr) {
        this.type = type;
        this.msg = descr;
    }

}
