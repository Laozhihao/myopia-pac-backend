package com.wupol.myopia.business.core.stat.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.stat.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.core.stat.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.core.stat.handler.BigScreenScreeningDOHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 地区层级某次筛查计划统计监控监测情况表
 *
 * @Author HaoHao
 * @Date 2021-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_district_big_screen_statistic", autoResultMap = true)
public class DistrictBigScreenStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 大屏展示--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 大屏展示--地区的id
     */
    private Integer districtId;
    /**
     * 大屏展示--政府部门id
     */
    private Integer govDeptId;
    /**
     * validDataNum
     */
    private Long validDataNum;
    /**
     * planScreeningNum
     */
    private Long planScreeningNum;
    /**
     * progressRate
     */
    private Double progressRate;
    /**
     * realScreeningNum
     */
    private Long  realScreeningNum;
    /**
     * realScreening
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private BigScreenScreeningDO realScreening;
    /**
     * lowVision
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private BigScreenScreeningDO lowVision;
    /**
     * myopia
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private BigScreenScreeningDO myopia;
    /**
     * ametropia
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private BigScreenScreeningDO ametropia;
    /**
     * focusOjects
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private BigScreenScreeningDO focusObjects;
    /**
     * avgVision
     */
    @TableField(typeHandler = JacksonTypeHandler.class,updateStrategy = FieldStrategy.IGNORED)
    private AvgVisionDO avgVision;

    /**
     * 大屏展示--更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
