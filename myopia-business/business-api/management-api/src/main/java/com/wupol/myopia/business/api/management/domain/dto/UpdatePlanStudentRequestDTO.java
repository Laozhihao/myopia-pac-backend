package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 更新筛查学生学生
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UpdatePlanStudentRequestDTO {

    private String name;

    private Integer gender;

    private Integer studentAge;

    private String parentPhone;

    private Date birthday;

    private String sno;

    private Integer planStudentId;

    private Integer studentId;
}
