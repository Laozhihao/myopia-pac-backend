package com.wupol.myopia.business.parent.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 家长端-家长绑定的学生列表
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ParentStudentVO {

    /**
     * 学生ID
     */
    private Integer id;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer gender;
}
