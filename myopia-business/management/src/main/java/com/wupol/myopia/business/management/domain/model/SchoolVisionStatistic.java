package com.wupol.myopia.business.management.domain.model;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 学校某次筛查计划统计视力情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_vision_statistic")
public class SchoolVisionStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视力情况--所属的学校id
     */
    private Integer schoolId;

    /**
     * 视力情况--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 视力情况--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 视力情况--关联的筛查计划id
     */
    private Integer screeningPlanId;

    /**
     * 视力情况--所属的地区id
     */
    private Integer districtId;

    /**
     * 视力情况--平均左眼视力（小数点后一位，默认0.0）
     */
    private BigDecimal avgLeftVision;

    /**
     * 视力情况--平均右眼视力（小数点后一位，默认0.0）
     */
    private BigDecimal avgRightVision;

    /**
     * 视力情况--视力低下人数（默认0）
     */
    private Integer lowVisionNumbers;

    /**
     * 视力情况--视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer lowVisionRatio;

    /**
     * 视力情况--戴镜人数（默认0）
     */
    private Integer wearingGlassesNumbers;

    /**
     * 视力情况--戴镜人数（均为整数，如10.01%，数据库则是1001）
     */
    private Integer wearingGlassesRatio;

    /**
     * 视力情况--近视人数（默认0）
     */
    private Integer myopiaNumbers;

    /**
     * 视力情况--近视比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer myopiaRatio;

    /**
     * 视力情况--屈光不正人数（默认0）
     */
    private Integer ametropiaNumbers;

    /**
     * 视力情况--屈光不正比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer ametropiaRatio;

    /**
     * 视力情况--零级预警人数（默认0）
     */
    @TableField("vision_label_0_numbers")
    private Integer visionLabel0Numbers;

    /**
     * 视力情况--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_0_ratio")
    private Integer visionLabel0Ratio;

    /**
     * 视力情况--一级预警人数（默认0）
     */
    @TableField("vision_label_1_numbers")
    private Integer visionLabel1Numbers;

    /**
     * 视力情况--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_1_ratio")
    private Integer visionLabel1Ratio;

    /**
     * 视力情况--二级预警人数（默认0）
     */
    @TableField("vision_label_2_numbers")
    private Integer visionLabel2Numbers;

    /**
     * 视力情况--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_2_ratio")
    private Integer visionLabel2Ratio;

    /**
     * 视力情况--三级预警人数（默认0）
     */
    @TableField("vision_label_3_numbers")
    private Integer visionLabel3Numbers;

    /**
     * 视力情况--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    @TableField("vision_label_3_ratio")
    private Integer visionLabel3Ratio;

    /**
     * 视力情况--重点视力对象数量（默认0）
     */
    private Integer keyWarningNumbers;

    /**
     * 视力情况--建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNumbers;

    /**
     * 视力情况--建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer treatmentAdviceRatio;

    /**
     * 视力情况--计划的学生数量（默认0）
     */
    private Integer planScreeningNumbers;

    /**
     * 视力情况--实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNumners;

    /**
     * 视力情况--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
