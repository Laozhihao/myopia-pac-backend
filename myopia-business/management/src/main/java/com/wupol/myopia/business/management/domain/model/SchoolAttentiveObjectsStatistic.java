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
 * 某个学校最新统计的重点视力对象情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_attentive_objects_statistic")
public class SchoolAttentiveObjectsStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 重点视力对象--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 重点视力对象--关联的计划id
     */
    private Integer screeningPlanId;

    /**
     * 重点视力对象--所属的地区id
     */
    private Integer districtId;

    /**
     * 重点视力对象--零级预警人数（默认0）
     */
    private Integer visionLabel0Numbers;

    /**
     * 重点视力对象--一级预警人数（默认0）
     */
    private Integer visionLabel1Numbers;

    /**
     * 重点视力对象--二级预警人数（默认0）
     */
    private Integer visionLabel2Numbers;

    /**
     * 重点视力对象--三级预警人数（默认0）
     */
    private Integer visionLabel3Numbers;

    /**
     * 重点视力对象--重点视力对象数量（默认0）
     */
    private Integer keyWarningNumbers;

    /**
     * 重点视力对象--统计时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
