package com.wupol.myopia.business.api.school.management.domain.dto;

import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 学生列表查询条件对象
 *
 * @author hang.yuan 2022/9/13 17:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentListDTO extends PageRequest {

    /**
     * 名称
     */
    private String name;
    /**
     * 学号
     */
    private String sno;

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 班级Id
     */
    private Integer classId;
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 筛查计划Id
     */
    @NotNull(message = "筛查计划ID不能为空")
    private Integer screeningPlanId;
}
