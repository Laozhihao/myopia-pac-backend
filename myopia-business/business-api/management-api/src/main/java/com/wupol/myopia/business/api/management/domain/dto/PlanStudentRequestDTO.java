package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查学生请求入参
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PlanStudentRequestDTO {

    private Integer screeningPlanId;

    private Integer screeningOrgId;

    private Integer schoolId;

    private Integer gradeId;

    private Integer classId;
}
