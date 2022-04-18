package com.wupol.myopia.business.core.stat.domain.model;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.handler.VisionAnalysisTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 筛查结果统计表
 * @author hang.yuan
 * @date 2022/4/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_screening_result_statistic",autoResultMap = true)
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
     * 筛查机构id
     */
    private Integer screeningOrgId;

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
    private String finishRatio;

    /**
     * 纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNum;

    /**
     * 纳入统计的实际筛查学生比例
     */
    private String validScreeningRatio;


    /**
     * 是否合计数据
     */
    private Boolean isTotal;

    /**
     * 视力分析
     */
    @TableField(typeHandler = VisionAnalysisTypeHandler.class)
    private VisionAnalysis visionAnalysis;

//    private String visionAnalysis;

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


    /*public void setVisionAnalysis(String visionAnalysis) {
        if(visionAnalysisDO != null){
            this.visionAnalysis= JsonUtil.objectToJsonString(visionAnalysisDO);
        }
    }

    public VisionAnalysis getVisionAnalysis() {
        if(StrUtil.isNotBlank(visionAnalysis)){
            if (Objects.equals(8,schoolType)){
                this.visionAnalysisDO=JsonUtil.jsonToObject(visionAnalysis,KindergartenVisionAnalysisDO.class);
            }else {
                this.visionAnalysisDO=JsonUtil.jsonToObject(visionAnalysis,PrimarySchoolAndAboveVisionAnalysisDO.class);
            }
        }

        return visionAnalysisDO;

    }*/
}