package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 筛查通知任务或者计划表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_task")
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 筛查任务--结束时间（时间戳）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    /**
     * 筛查任务--创建者ID
     */
    private Integer creatorId;

    /**
     * 筛查任务--创建时间（时间戳）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 筛查任务--最后操作人id  
     */
    private Integer operatorId;

    /**
     * 筛查任务--最后操作时间（时间戳）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;


}
