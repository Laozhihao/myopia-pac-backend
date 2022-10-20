package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseaseNumDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 筛查数据结论
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_stat_conclusion", autoResultMap = true)
public class StatConclusion implements Serializable {
    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 源筛查数据id */
    private Integer resultId;

    /** 源筛查通知id */
    private Integer srcScreeningNoticeId;

    /** 任务id */
    private Integer taskId;

    /** 计划id */
    private Integer planId;

    /** 所属地区id */
    private Integer districtId;

    /** 学龄 */
    private Integer schoolAge;

    /** 性别 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 预警级别 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer warningLevel;

    /** 左眼视力 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal visionL;

    /** 右眼视力 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal visionR;

    /** 是否视力低下 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isLowVision;

    /** 是否屈光不正 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isRefractiveError;

    /** 是否近视 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isMyopia;

    /** 是否远视 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isHyperopia;

    /** 是否散光 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isAstigmatism;

    /** 是否建议就诊 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isRecommendVisit;

    /** 是否复测 */
    private Boolean isRescreen;

    /** 复测错误项次 */
    private Integer rescreenErrorNum;

    /** 复测项次 */
    private Integer rescreenItemNum;

    /** 是否有效数据 */
    private Boolean isValid;

    /** 筛查计划学生ID */
    private Integer screeningPlanSchoolStudentId;

    /** 筛查学生ID */
    private Integer studentId;

    /**
     * 近视等级
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer myopiaLevel;

    /**
     * 远视等级
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer hyperopiaLevel;

    /**
     * 散光等级
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer astigmatismLevel;

    /**
     * 是否绑定公众号
     */
    private Boolean isBindMp;

    /**
     * 报告Id
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer reportId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** --------------------- 新增导出报告所需属性 --------------------- */

    /** 学校Id */
    private Integer schoolId;

    /** 学校年级代码 */
    private String schoolGradeCode;

    /** 学校班级名称 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String schoolClassName;

    /** 近视预警级别 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer myopiaWarningLevel;

    /** 裸眼视力预警级别 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer nakedVisionWarningLevel;

    /** 眼镜类型 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer glassesType;

    /** 视力矫正状态 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer visionCorrection;

    /**
     * 筛查计划--指定的筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * Vision异常(6岁及以上,4.0及以下)
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isVisionWarning;

    /**
     * 更新时间
     */
    private Date visionWarningUpdateTime;


    /** --------------------- 常见病 --------------------- */
    /**
     * 筛查类型：0-视力筛查、1-常见病
     */
    private Integer screeningType;
    /**
     * 是否龋患
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isSaprodontia;
    /**
     * 是否超重
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isOverweight;
    /**
     * 是否肥胖
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isObesity;
    /**
     * 是否营养不良
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isMalnutrition;
    /**
     * 是否生长迟缓
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isStunting;
    /**
     * 是否脊柱弯曲
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isSpinalCurvature;
    /**
     * 是否血压正常
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isNormalBloodPressure;
    /**
     * 是否有疾病史
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isDiseasesHistory;
    /**
     * 是否遗精
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isNocturnalEmission;
    /**
     * 是否初潮
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isMenarche;

    /**
     * 体格复测错误项次
     */
    private Integer physiqueRescreenErrorNum;

    /**
     * 是否复查
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isReview;

    /**
     *  是否屈光参差
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isAnisometropia;

    /**
     * 龋患牙齿数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer saprodontiaTeeth;
    /**
     * 是否龋失
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isSaprodontiaLoss;
    /**
     * 龋失牙齿数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer saprodontiaLossTeeth;
    /**
     * 是否龋补
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isSaprodontiaRepair;
    /**
     * 龋补牙齿数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer saprodontiaRepairTeeth;

    /**
     * 是否戴镜
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isWearingGlasses;

    /**
     * 视力低下等级 {@link com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum}
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer lowVisionLevel;

    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    /**
     * 筛查性近视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer screeningMyopia;

    /**
     * 疾病统计数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED,typeHandler = JacksonTypeHandler.class)
    private DiseaseNumDO diseaseNum;

    /**
     * 是否正常（等效球镜判断）
     */
    private Boolean isNormal;

}
