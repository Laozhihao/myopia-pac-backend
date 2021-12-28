package com.wupol.myopia.business.core.school.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolGradeClassVO {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;
}
