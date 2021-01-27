package com.wupol.myopia.business.management.domain.model;

import java.math.BigDecimal;
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
 * 
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_result")
public class ScreeningResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查结果--所属的学生id
     */
    private Integer screeningPlanSchoolStudentId;

    /**
     * 筛查结果--所属的任务id
     */
    private Integer taskId;

    /**
     * 筛查结果--学校id
     */
    private Integer schoolId;

    /**
     * 筛查结果--学生id
     */
    private Integer studentId;

    /**
     * 筛查结果--所属的计划id
     */
    private Integer planId;

    /**
     * 筛查结果--所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查结果--左眼裸视力（默认0.0）
     */
    private BigDecimal leftNakedVision;

    /**
     * 筛查结果--右眼裸视力 （默认0.0）
     */
    private BigDecimal rightNakedVision;

    /**
     * 筛查结果--左眼矫正视力 （默认0.0）
     */
    private BigDecimal leftCorrectedVision;

    /**
     * 筛查结果--右眼眼矫正视力 （默认0.0）
     */
    private BigDecimal rightCorrectedVision;

    /**
     * 筛查结果--戴镜情况：-1-默认、0-没有戴镜、1-佩戴框架眼镜、2-佩戴隐形眼镜、3-夜戴角膜塑形镜
     */
    private Integer glassesType;

    /**
     * 筛查结果-- 预警情况 ：-1-默认、0-是0级、1-是一级、2是二级、3是三级
     */
    private Integer visionLabel;

    /**
     * 筛查结果--是否视力低下：-1-默认、0-否、1-是
     */
    private Boolean isLowVision;

    /**
     * 筛查结果--是否近视：-1-默认、0-否、1-是
     */
    private Boolean isMyopia;

    /**
     * 筛查结果--是否属于重点视力人群：-1-默认、0-否、1-是
     */
    private Boolean isKeyWarningCrown;

    /**
     * 筛查结果--是否建议就诊：-1-默认、0-否、1-是
     */
    private Boolean isRecommendedVisits;

    /**
     * 筛查结果--是否复筛（0否，1是）
     */
    private Boolean isDoubleScreen;

    /**
     * 提交数据的筛查人员ID
     */
    private Integer createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
