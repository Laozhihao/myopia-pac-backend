package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 班级和年级
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolGradeClassResponseDTO {

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级Id
     */
    private Integer classId;

    /**
     * 班级名称
     */
    private String className;
}
