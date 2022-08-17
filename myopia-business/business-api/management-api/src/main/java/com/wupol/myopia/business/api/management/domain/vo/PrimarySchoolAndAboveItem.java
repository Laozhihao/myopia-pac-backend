package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 按区域小学及以上
 *
 * @author hang.yuan 2022/6/20 11:22
 */
@Data
public class PrimarySchoolAndAboveItem {
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
     * 实际筛查的学生比例（完成率）
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
     * 平均左眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgLeftVision;

    /**
     * 平均右眼视力（小数点后一位，默认0.0）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal avgRightVision;

    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private String treatmentAdviceRatio;

    /**
     * 小学及以上--近视人数（默认0）
     */
    private Integer myopiaNum;

    /**
     * 小学及以上--近视比例（均为整数，如10.01%，数据库则是1001）
     */
    private String myopiaRatio;

    /**
     *  视力筛查项
     */
    private VisionItem visionItem;

    /**
     *  常见病筛查项
     */
    private CommonDiseaseItem commonDiseaseItem;
    /**
     * 区域ID
     */
    private Integer districtId;
    /**
     * 所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     *  是否幼儿园
     */
    private Boolean isKindergarten;

    public void setHasRescreenReport(Boolean hasRescreenReport) {
        //子类实现
    }

}
