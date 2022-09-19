package com.wupol.myopia.business.api.school.management.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 筛查计划创建实体
 *
 * @author hang.yuan 2022/9/13 14:39
 */
@Data
public class ScreeningPlanDTO implements Serializable {

    private Integer id;

    /**
     * 筛查计划--标题
     */
    @NotBlank(message = "筛查计划标题不能为空")
    private String title;

    /**
     * 筛查计划--开始时间
     */
    @NotBlank(message = "筛查计划开始时间不能为空")
    private String startTime;

    /**
     * 筛查计划--结束时间
     */
    @NotBlank(message = "筛查计划结束时间不能为空")
    private String endTime;

    /**
     * 筛查计划--内容
     */
    private String content;

    /**
     * 筛查类型（0：视力筛查，1；常见病）
     */
    @NotNull(message = "筛查类型不能为空")
    private Integer screeningType;

    /**
     * 筛查学生的年级ID集合
     */
    @NotEmpty(message = "筛查学生年级不能为空")
    private List<Integer> gradeIds;
}
