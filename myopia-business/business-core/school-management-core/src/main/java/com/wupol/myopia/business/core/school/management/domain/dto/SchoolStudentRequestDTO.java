package com.wupol.myopia.business.core.school.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校端-学生列表请求DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolStudentRequestDTO {

    private String sno;

    private String name;

    private Integer gradeId;

    private Integer classId;

    private Integer visionLabel;
}
