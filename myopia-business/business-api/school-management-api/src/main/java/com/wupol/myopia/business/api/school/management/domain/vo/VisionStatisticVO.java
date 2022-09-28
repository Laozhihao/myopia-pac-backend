package com.wupol.myopia.business.api.school.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 视力统计
 *
 * @author hang.yuan 2022/9/26 23:17
 */
@Data
@Accessors(chain = true)
public class VisionStatisticVO implements Serializable {

    // 视力筛查情况
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


    //视力情况统计
    /**
     * 视力低下人数（默认0）
     */
    private Integer lowVisionNum;

    /**
     * 视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private String lowVisionRatio;

    /**
     * 平均左眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgLeftVision;

    /**
     * 平均右眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgRightVision;


    //视力监测预警
    /**
     * 视力预警人数
     */
    private Integer visionWarningNum;
    /**
     *  视力预警比例
     */
    private String visionWarningRatio;

    /**
     * 零级预警人数（默认0）
     */
    private Integer visionLabel0Num;

    /**
     * 零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel0Ratio;

    /**
     * 一级预警人数（默认0）
     */
    private Integer visionLabel1Num;

    /**
     * 一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel1Ratio;

    /**
     * 二级预警人数（默认0）
     */
    private Integer visionLabel2Num;

    /**
     * 二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel2Ratio;

    /**
     * 三级预警人数（默认0）
     */
    private Integer visionLabel3Num;

    /**
     * 三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel3Ratio;


    //视力异常

    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private String treatmentAdviceRatio;

    /**
     * 去医院就诊数
     */
    private Integer reviewNum;

    /**
     * 去医院就诊比例
     */
    private String reviewRatio;

    /**
     * 绑定公众号人数
     */
    private Integer bindMpNum;
    /**
     * 绑定公众号比例
     */
    private String bindMpRatio;
}
