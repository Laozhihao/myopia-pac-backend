package com.wupol.myopia.business.core.screening.flow.domian.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 筛查任务关联的机构表
 *
 * @author Alix
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_task_org")
public class ScreeningTaskOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查任务--筛查任务id
     */
    private Integer screeningTaskId;

    /**
     * 筛查任务--筛查机构id
     */
    @NotNull
    private Integer screeningOrgId;

    /**
     * 筛查任务--机构质控员名字（长度限制未知）
     */
    @NotNull
    private String qualityControllerName;

    /**
     * 筛查任务--机构质控员联系方式（长度限制未知）
     */
    @NotNull
    private String qualityControllerContact;

    /**
     * 筛查任务--机构质控员队长（长度限制未知）
     */
    @NotNull
    private String qualityControllerCommander;

    /**
     * 创建时间
     */
    private Date createTime;


}
