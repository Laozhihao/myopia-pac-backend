package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 学生DTO
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StudentDTO extends Student {

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 视力筛查次数
     */
    private Integer screeningCount;

    /**
     * 问卷数
     */
    private Integer questionnaireCount;


    /**
     * 就诊次数
     */
    private Integer seeDoctorCount;
}
