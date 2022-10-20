package com.wupol.myopia.base.constant;

import lombok.Getter;

/**
 * 总览机构配置类型
 *
 * @author Simple4H
 **/
@Getter
public enum OverviewConfigTypeKey {

    SCREENING_ORG("org", "筛查机构"),
    HOSPITAL("hospital", "医院"),
    SCHOOL("school", "学校");

    /**
     * key
     **/
    private final String key;
    /**
     * 描述
     **/
    private final String msg;

    OverviewConfigTypeKey(String key, String descr) {
        this.key = key;
        this.msg = descr;
    }

}
