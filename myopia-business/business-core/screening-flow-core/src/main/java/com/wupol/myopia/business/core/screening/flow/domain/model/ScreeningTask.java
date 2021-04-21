package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.common.utils.annotation.CheckTimeInterval;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 筛查通知任务表
 *
 * @author Alix
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_task")
@CheckTimeInterval(beginTime = "startTime", endTime = "endTime", message = "开始时间不能晚于结束时间")
public class ScreeningTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查任务--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查任务--标题
     */
    private String title;

    /**
     * 筛查任务--内容
     */
    private String content;

    /**
     * 筛查任务--开始时间（时间戳）
     */
    @NotNull(message = "筛查计划开始时间不能为空")
    private Date startTime;

    /**
     * 筛查任务--结束时间（时间戳）
     */
    @NotNull(message = "筛查计划开始时间不能为空")
    private Date endTime;

    /**
     * 筛查任务--所处部门id
     */
    private Integer govDeptId;

    /**
     * 筛查任务--所处区域id
     */
    private Integer districtId;

    /**
     * 筛查任务--发布状态 （0未发布 1已发布）
     */
    private Integer releaseStatus;

    /**
     * 筛查任务--发布时间（时间戳）
     */
    private Date releaseTime;

    /**
     * 筛查任务--创建者ID
     */
    private Integer createUserId;

    /**
     * 筛查任务--创建时间（时间戳）
     */
    private Date createTime;

    /**
     * 筛查任务--最后操作人id  
     */
    private Integer operatorId;

    /**
     * 筛查任务--最后操作时间（时间戳）
     */
    private Date operateTime;


}
