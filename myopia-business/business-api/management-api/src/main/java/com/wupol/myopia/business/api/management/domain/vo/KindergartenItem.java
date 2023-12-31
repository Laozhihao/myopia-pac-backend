package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 幼儿园
 *
 * @author hang.yuan 2022/6/20 12:24
 */
@Data
public class KindergartenItem {
    /**
     * 查看的范围(地区或者学校名）
     */
    private String screeningRangeName;
    /**
     * 计划的学生数量（默认0）
     */
    private Integer planScreeningNum;

    /**
     * 实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNum;

    /**
     *  实际筛查的学生比例（完成率）
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
     * 视力低下人数（默认0）
     */
    private Integer lowVisionNum;

    /**
     * 视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private String lowVisionRatio;

    /**
     * 平均左眼视力（小数点后二位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgLeftVision;

    /**
     * 平均右眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgRightVision;

    /**
     * 幼儿园--屈光不正人数（默认0）
     */
    private Integer ametropiaNum;

    /**
     * 幼儿园--屈光不正比例（均为整数，如10.01%，数据库则是1001）
     */
    private String ametropiaRatio;

    /**
     * 幼儿园--屈光参差人数（默认0）
     */
    private Integer anisometropiaNum;

    /**
     * 幼儿园--屈光参差率
     */
    private String anisometropiaRatio;

    /**
     * 幼儿园--远视储备不足人数（默认0）
     */
    private Integer myopiaLevelInsufficientNum;

    /**
     * 幼儿园--远视储备不足率
     */
    private String myopiaLevelInsufficientRatio;


    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private String treatmentAdviceRatio;

    /**
     * 通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型
     */
    private Integer screeningType;
    /**
     * 区域ID
     */
    private Integer districtId;
    /**
     *  是否幼儿园
     */
    private Boolean isKindergarten;

    public void setHasRescreenReport(Boolean hasRescreenReport) {
        // 子类实现
    }
}
