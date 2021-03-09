package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
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
     * realScreening
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object realScreening;
    /**
     * lowVision
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object lowVision;
    /**
     * myopia
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object myopia;
    /**
     * ametropia
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object ametropia;
    /**
     * focusOjects
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object focusObjects;
    /**
     * avgVision
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object avgVision;

    /**
     * 大屏展示--地图json
     */
    @TableField("mapData")
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
