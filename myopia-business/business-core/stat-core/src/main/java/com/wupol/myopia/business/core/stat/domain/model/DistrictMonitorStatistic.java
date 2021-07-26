package com.wupol.myopia.business.core.stat.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 地区层级某次筛查计划统计监控监测情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_district_monitor_statistic")
public class DistrictMonitorStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 监测情况--所属的通知id
     */
    private Integer screeningNoticeId;
    /**
     * 监测情况--完成率
     */
    private BigDecimal finishRatio;
    /**
     * 监测情况--关联的任务id
     */
    private Integer screeningTaskId;
    /**
     * 监测情况--是否 合计 0=否 1=是
     */
    private Integer isTotal;
    /**
     * 监测情况--所属的地区id（筛查范围）
     */
    private Integer districtId;
    /**
     * 监测情况--复测项数目
     */
    private Integer rescreeningItemNumbers;

    /**
     * 监测情况--戴镜人数（默认0）
     */
    private Integer investigationNumbers;

    /**
     * 监测情况--脱镜复测数量（默认0）
     */
    private Integer withoutGlassDsn;

    /**
     * 监测情况--脱镜复测指标数（dsin = double screening index numbers默认0）
     */
    private Integer withoutGlassDsin;

    /**
     * 监测情况--戴镜复测数量（默认0）
     */
    private Integer wearingGlassDsn;

    /**
     * 监测情况--戴镜复测指标数（dsin = double screening index numbers默认0）
     */
    private Integer wearingGlassDsin;

    /**
     * 监测情况--复测数量（默认0）
     */
    private Integer dsn;

    /**
     * 监测情况--筛查错误数（默认0）
     */
    private Integer errorNumbers;

    /**
     * 监测情况--筛查错误率（默认0，单位%）
     */
    private BigDecimal errorRatio;

    /**
     * 监测情况--计划的学生数量（默认0）
     */
    private Integer planScreeningNumbers;

    /**
     * 监测情况--实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNumbers;

    /**
     * 监测情况--更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
