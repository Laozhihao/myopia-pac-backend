package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 筛查计划创建实体
 *
 * @author hang.yuan 2022/9/13 14:39
 */
@Data
public class SchoolScreeningPlanDTO implements Serializable {

    private Integer id;

    /**
     * 筛查计划--标题
     */
    @NotBlank(message = "筛查计划标题不能为空")
    @Size(min = 1,max = 25,message = "筛查计划标题最大长度25字符")
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

    /**
     * 所属的筛查源通知id ，自己创建时默认0
     */
    private Integer screeningNoticeId;

    /**
     * 所属的筛查任务id，自己创建时默认0
     */
    private Integer screeningTaskId;

    /**
     * 筛查年份
     */
    private Integer year;

    /**
     * 筛查次数
     */
    private Integer time;

}
