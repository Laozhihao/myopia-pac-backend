package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 新增筛查学生对象
 *
 * @author hang.yuan 2022/9/13 18:02
 */
@Data
public class AddScreeningStudentDTO {

    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 筛查学生的年级ID集合
     */
    private List<Integer> gradeIds;
}
