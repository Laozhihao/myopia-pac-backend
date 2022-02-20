package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 筛查结果表
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("h_preschool_check_record")
public class PreschoolCheckRecord extends SpecialMedical implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 检查中 */
    public static final Integer STATUS_ABNORMAL = 0;
    /** 检查完成 */
    public static final Integer STATUS_NORMAL = 1;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 医院id
     */
    private Integer hospitalId;

    /**
     * 是否有检查前转诊信息
     */
    private Boolean isReferral;

    /**
     * 转诊前-转诊单
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ReferralDO fromReferral;

    /**
     * 月龄[0-新生儿；1-满月；2-3月龄；3-6月龄；4-8月龄；5-12月龄；6-18月龄；7-24月龄；8-30月龄；9-36月龄；10-4岁；11-5岁；12-6岁；]
     */
    private Integer monthAge;

    /**
     * 眼外观
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private OuterEye outerEye;

    /**
     * 主要眼病高危因素
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private EyeDiseaseFactor eyeDiseaseFactor;

    /**
     * 光照反应
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult lightReaction;

    /**
     * 瞬目反射
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult blinkReflex;

    /**
     * 红球试验
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult redBallTest;

    /**
     * 视物行为观察
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BaseMedicalResult visualBehaviorObservation;

    /**
     * 视力检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionMedicalRecord visionData;

    /**
     * 健康指导
     */
    private String guideContent;

    /**
     * 眼病筛查及视力评估
     */
    private String conclusion;

    /**
     * 总休情况[0 异常 ；1 正常]
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
