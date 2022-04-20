package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("m_stat_conclusion")
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
    private BigDecimal visionL;

    /** 右眼视力 */
    private BigDecimal visionR;

    /** 是否视力低下 */
    private Boolean isLowVision;

    /** 是否屈光不正 */
    private Boolean isRefractiveError;

    /** 是否近视 */
    private Boolean isMyopia;

    /** 是否远视 */
    private Boolean isHyperopia;

    /** 是否散光 */
    private Boolean isAstigmatism;

    /** 是否建议就诊 */
    private Boolean isRecommendVisit;

    /** 是否复测 */
    private Boolean isRescreen;

    /** 复测错误项次 */
    private Integer rescreenErrorNum;

    /** 是否有效数据 */
    private Boolean isValid;

    /** 筛查计划学生ID */
    private Integer screeningPlanSchoolStudentId;

    /** 筛查学生ID */
    private Integer studentId;

    /**
     * 近视预警等级
     */
    private Integer myopiaLevel;

    /**
     * 远视预警等级
     */
    private Integer hyperopiaLevel;

    /**
     * 散光预警等级
     */
    private Integer astigmatismLevel;

    /**
     * 是否绑定公众号
     */
    private Boolean isBindMp;

    /**
     * 报告Id
     */
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
    private String schoolClassName;

    /** 近视预警级别 */
    private Integer myopiaWarningLevel;

    /** 裸眼视力预警级别 */
    private Integer nakedVisionWarningLevel;

    /** 眼镜类型 */
    private Integer glassesType;

    /** 视力矫正状态 */
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
     * 是否龋齿
     */
    private Boolean isSaprodontia;
    /**
     * 是否超重
     */
    private Boolean isOverweight;
    /**
     * 是否肥胖
     */
    private Boolean isObesity;
    /**
     * 是否营养不良
     */
    private Boolean isMalnutrition;
    /**
     * 是否生长迟缓
     */
    private Boolean isStunting;
    /**
     * 是否脊柱弯曲
     */
    private Boolean isSpinalCurvature;
    /**
     * 是否血压正常
     */
    private Boolean isNormalBloodPressure;
    /**
     * 是否有疾病史
     */
    private Boolean isDiseasesHistory;
    /**
     * 是否遗精
     */
    private Boolean isNocturnalEmission;
    /**
     * 是否初潮
     */
    private Boolean isMenarche;

    /**
     * 体格复测错误项次
     */
    private Integer physiqueRescreeningErrorNum;
}
