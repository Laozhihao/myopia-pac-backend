package com.wupol.myopia.business.core.school.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 学校端-学生列表请求DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolStudentRequestDTO {

    @NotNull(message = "学校Id不能为空")
    private Integer schoolId;

    private String sno;

    private String name;

    private Integer gradeId;

    private Integer classId;

    private Integer visionLabel;
}
