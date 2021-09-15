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

    /**
     * 筛查计划
     */
    private Integer screeningPlanId;

    /**
     * 筛查机构
     */
    private Integer screeningOrgId;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 年级
     */
    private Integer gradeId;

    /**
     * 班级
     */
    private Integer classId;
}
