package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 年级班级返回体
 *
 * @author Alix
 */
@Setter
@Getter
public class GradeClassesDTO {

    private Integer gradeId;

    private String gradeName;

    private Integer classId;

    private String className;
}
