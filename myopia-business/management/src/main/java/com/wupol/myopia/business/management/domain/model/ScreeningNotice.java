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
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 筛查通知表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_notice")
public class ScreeningNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer TYPE_GOV_DEPT = 0;
    public static final Integer TYPE_ORG = 1;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查通知--标题（最大25个字符）
     */
    @Length(max = 25)
    @NotBlank
    private String title;

    /**
     * 筛查通知--通知内容（长度未知）
     */
    @NotBlank
    private String content;

    /**
     * 筛查通知--开始时间（时间戳）
     */
    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    /**
     * 筛查通知--结束时间（时间戳）
     */
    @NotNull(message = "结束时间不能为空")
    private Date endTime;

    /**
     * 筛查通知--通知类型（0是筛查通知-政府、1是筛查任务通知-筛查机构）
     */
    private Integer type;

    /**
     * 筛查通知--所处部门id
     */
    private Integer govDeptId;

    /**
     * 筛查通知--所处地区id
     */
    private Integer districtId;

    /**
     * 筛查通知--来源的筛查任务id（type为1有）
     */
    private Integer screeningTaskId;

    /**
     * 筛查通知--通知状态（0未发布、1已发布）
     */
    private Integer releaseStatus;

    /**
     * 筛查通知--发布时间（时间戳 ）
     */
    private Date releaseTime;

    /**
     * 筛查通知--操作人版本（版本自增，便于解决数据修改覆盖）
     */
    private Integer operationVersion;

    /**
     * 筛查通知--创建人id  
     */
    private Integer createUserId;

    /**
     * 筛查通知--创建时间（时间戳）
     */
    private Date createTime;

    /**
     * 筛查通知--最后操作人id  
     */
    private Integer operatorId;

    /**
     * 筛查通知--最后操作时间（时间戳）
     */
    private Date operateTime;
}