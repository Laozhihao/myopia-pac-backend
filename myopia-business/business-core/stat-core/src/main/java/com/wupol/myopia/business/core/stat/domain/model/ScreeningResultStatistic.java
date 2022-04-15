package com.wupol.myopia.business.core.stat.domain.model;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 筛查结果统计表
 * @author hang.yuan
 * @date 2022/4/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_result_statistic")
public class ScreeningResultStatistic implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 关联的任务id（is_total情况下，可能为0）
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划Id
     */
    private Integer screeningPlanId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7-其他,8-幼儿园
     */
    private Integer schoolType;

    /**
     * 学校数
     */
    private Integer schoolNum;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 计划的学生数量（默认0）
     */
    private Integer planScreeningNum;

    /**
     * 实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNum;

    /**
     * 完成率
     */
    private BigDecimal finishRatio;

    /**
     * 纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNum;


    /**
     * 是否合计数据
     */
    private Boolean isTotal;

    /**
     * 视力分析
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionAnalysis visionAnalysis;

    /**
     * 复测情况
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private RescreenSituationDO rescreenSituation;

    /**
     * 视力预警
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionWarningDO visionWarning;

    /**
     * 龋齿情况
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SaprodontiaDO saprodontia;


    /**
     *  常见病分析
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private CommonDiseaseDO commonDisease;

    /**
     *  问卷情况
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QuestionnaireDO questionnaire;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date updateTime;


}