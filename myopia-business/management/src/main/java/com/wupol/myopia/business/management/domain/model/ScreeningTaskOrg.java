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
 * 筛查任务关联的机构表
 *
 * @Author HaoHao
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
    private Integer screeningOrgId;

    /**
     * 筛查任务--机构质控员名字（长度限制未知）
     */
    private String qualityControllerName;

    /**
     * 筛查任务--机构质控员联系方式（长度限制未知）
     */
    private String qualityControllerContact;

    /**
     * 筛查任务--机构质控员队长（长度限制未知）
     */
    private String qualityControllerCommander;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
