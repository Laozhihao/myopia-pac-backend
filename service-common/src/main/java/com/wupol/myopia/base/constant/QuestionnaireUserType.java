package com.wupol.myopia.base.constant;

import lombok.Getter;

/**
 * 问卷用户类型
 *
 * @author Simple4H
 */
@Getter
public enum QuestionnaireUserType {
    STUDENT(0, "学生"),
    SCHOOL(1, "学校"),
    GOVERNMENT_DEPARTMENT(2, "政府部门");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String name;

    QuestionnaireUserType(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
