package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.management.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.management.handler.BigScreenScreeningDOHandler;
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
    @TableField(typeHandler = BigScreenScreeningDOHandler.class)
    private BigScreenScreeningDO realScreening;
    /**
     * lowVision
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class)
    private BigScreenScreeningDO lowVision;
    /**
     * myopia
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class)
    private BigScreenScreeningDO myopia;
    /**
     * ametropia
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class)
    private BigScreenScreeningDO ametropia;
    /**
     * focusOjects
     */
    @TableField(typeHandler = BigScreenScreeningDOHandler.class)
    private BigScreenScreeningDO focusObjects;
    /**
     * avgVision
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private AvgVisionDO avgVision;

    /**
     * 大屏展示--地图json
     */
    @TableField(value = "mapData",typeHandler = JacksonTypeHandler.class)
    private Object mapdata;

    /**
     * 大屏展示--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
