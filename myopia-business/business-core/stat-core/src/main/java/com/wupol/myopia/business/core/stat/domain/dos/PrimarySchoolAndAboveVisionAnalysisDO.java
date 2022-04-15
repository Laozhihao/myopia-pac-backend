package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小学及以上视力分析
 *
 * @author hang.yuan 2022/4/13 15:23
 */
@Data
@Accessors(chain = true)
public class PrimarySchoolAndAboveVisionAnalysisDO implements VisionAnalysis {
    /**
     * 视力低下人数（默认0）
     */
    private Integer lowVisionNum;

    /**
     * 视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal lowVisionRatio;

    /**
     * 平均左眼视力（小数点后二位，默认0.00）
     */
    private BigDecimal avgLeftVision;

    /**
     * 平均右眼视力（小数点后二位，默认0.00）
     */
    private BigDecimal avgRightVision;

    /**
     * 小学及以上--近视人数（默认0）
     */
    private Integer myopiaNum;

    /**
     * 小学及以上--近视比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal myopiaRatio;

    /**
     * 小学及以上--近视前期人数（默认0）
     */
    private Integer myopiaLevelEarlyNum;

    /**
     * 小学及以上--近视前期率
     */
    private BigDecimal myopiaLevelEarlyRatio;

    /**
     * 小学及以上--低度近视人数（默认0）
     */
    private Integer lowMyopiaNum;

    /**
     * 小学及以上--低度近视率
     */
    private BigDecimal lowMyopiaRatio;

    /**
     * 小学及以上--高度近视人数（默认0）
     */
    private Integer highMyopiaNum;
    /**
     * 小学及以上--高度近视率
     */
    private BigDecimal highMyopiaRatio;

    /**
     * 小学及以上--散光数人数（默认0）
     */
    private Integer astigmatismNum;

    /**
     * 小学及以上--散光数率
     */
    private BigDecimal astigmatismRatio;

    /**
     * 戴镜人数（默认0）
     */
    private Integer wearingGlassesNum;

    /**
     * 戴镜率（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal wearingGlassesRatio;


    /**
     * 小学及以上--夜戴角膜塑形镜人数（默认0）
     */
    private Integer nightWearingOrthokeratologyLensesNum;

    /**
     * 小学及以上--夜戴角膜塑形镜率
     */
    private BigDecimal nightWearingOrthokeratologyLensesRatio;

    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private BigDecimal treatmentAdviceRatio;

}
