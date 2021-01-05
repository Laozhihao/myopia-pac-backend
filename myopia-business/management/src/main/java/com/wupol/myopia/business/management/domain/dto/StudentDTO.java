package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生DTO
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentDTO extends Student {

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;
}
