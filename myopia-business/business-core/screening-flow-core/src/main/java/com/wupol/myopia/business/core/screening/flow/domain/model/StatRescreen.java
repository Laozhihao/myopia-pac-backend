package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2021/5/20 10:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_stat_rescreen")
public class StatRescreen implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查结果--所属的机构id
     */
    private Integer screeningOrgId;

    /**
     * 通知id
     */
    private Integer srcScreeningNoticeId;

    /**
     * 筛查结果--所属的任务id
     */
    private Integer taskId;

    /**
     * 筛查结果--所属的计划id
     */
    private Integer planId;

    /**
     * 筛查结果--执行的学校id
     */
    private Integer schoolId;

    /**
     * 筛查日期
     */
    private Date screeningTime;

    /**
     * 复测人数
     */
    private Long rescreenNum;

    /**
     * 戴镜复测人数
     */
    private Long wearingGlassesRescreenNum;

    /**
     * 戴镜复测指标数
     */
    private Long wearingGlassesRescreenIndexNum;

    /**
     * 非戴镜复测人数
     */
    private Long withoutGlassesRescreenNum;

    /**
     * 非戴镜复测指标数
     */
    private Long withoutGlassesRescreenIndexNum;

    /**
     * 复测项次
     */
    private Long rescreenItemNum;

    /**
     * 错误项次数
     */
    private Long incorrectItemNum;

    /**
     * 错误率/发生率
     */
    private Float incorrectRatio;

    /**
     * 统计时间
     */
    private Date createTime;

}
