package com.wupol.myopia.business.core.screening.flow.domian.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.management.annotation.CheckTimeInterval;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 筛查通知任务或者计划表
 *
 * @author Alix
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_plan")
@CheckTimeInterval(beginTime = "startTime", endTime = "endTime", message = "开始时间不能晚于结束时间")
public class ScreeningPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0
     */
    private Integer srcScreeningNoticeId;

    /**
     * 筛查计划--所属的筛查任务id
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划--标题
     */
    @NotBlank(message = "筛查计划标题不能为空")
    private String title;

    /**
     * 筛查计划--内容
     */
    private String content;

    /**
     * 筛查计划--开始时间（时间戳）
     */
    @NotNull(message = "筛查计划开始时间不能为空")
    private Date startTime;

    /**
     * 筛查计划--结束时间（时间戳）
     */
    @NotNull(message = "筛查计划结束时间不能为空")
    private Date endTime;

    /**
     * 筛查计划--所处部门id
     */
    private Integer govDeptId;

    /**
     * 筛查计划--筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查计划--所处区域id
     */
    private Integer districtId;

    /**
     * 筛查计划--计划的学生总数
     */
    private Integer studentNumbers;

    /**
     * 筛查计划--发布状态 （0未发布 1已发布）
     */
    private Integer releaseStatus;

    /**
     * 筛查计划--发布时间（时间戳）
     */
    private Date releaseTime;

    /**
     * 筛查计划--创建者ID
     */
    private Integer createUserId;

    /**
     * 筛查计划--创建时间（时间戳）
     */
    private Date createTime;

    /**
     * 筛查计划--最后操作人id  
     */
    private Integer operatorId;

    /**
     * 筛查计划--最后操作时间（时间戳）
     */
    private Date operateTime;
}
