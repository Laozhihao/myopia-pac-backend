package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotNull(message = "筛查计划ID")
    private Integer screeningPlanId;
    /**
     * 筛查学生的年级ID集合
     */
    @NotEmpty(message = "筛查学生年级不能为空")
    private List<Integer> gradeIds;

    /**
     * 学校ID
     */
    private Integer schoolId;
}
