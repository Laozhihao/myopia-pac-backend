package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * 更新筛查学生学生
 *
 * @author Simple4H
 */
@Data
public class UpdatePlanStudentRequestDTO {

    private String name;

    private Integer gender;

    private Integer studentAge;

    private String parentPhone;

    private Date birthday;

    private String sno;

    private Integer planStudentId;

    private Integer studentId;

    public Date getBirthday() {
        if (Objects.nonNull(birthday)) {
            return birthday;
        }
        return new Date();
    }
}
