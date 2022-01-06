package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class PreschoolCheckRecord implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 检查前-转诊id
     */
    private Integer fromReferralId;

    /**
     * 月龄[0-新生儿；1-满月；2-3月龄；3-6月龄；4-8月龄；5-12月龄；6-18月龄；7-24月龄；8-30月龄；9-36月龄；10-4岁；11-5岁；12-6岁；]
     */
    private Integer monthAge;

    /**
     * 眼外观
     */
    private String outerEye;

    /**
     * 主要眼病高危因素
     */
    private String eyeDiseaseFactor;

    /**
     * 光照反应
     */
    private String lightReaction;

    /**
     * 瞬目反射
     */
    private String blinkReflex;

    /**
     * 红球试验
     */
    private String redBallTest;

    /**
     * 视物行为观察
     */
    private String visualBehaviorObservation;

    /**
     * 红光反射
     */
    private String redReflex;

    /**
     * 眼位检查
     */
    private String ocularInspection;

    /**
     * 视力检查
     */
    private String visionData;

    /**
     * 单眼遮盖厌恶试验
     */
    private String monocularMaskingAversionTest;

    /**
     * 屈光检查
     */
    private String refractionData;

    /**
     * 健康指导
     */
    private String guideContent;

    /**
     * 检查后转诊id
     */
    private Integer toReferralId;

    /**
     * 回执单id
     */
    private Integer receiptId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
