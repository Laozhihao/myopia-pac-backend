package com.wupol.myopia.business.core.stat.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.base.util.BigDecimalUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 小学及以上视力分析
 *
 * @author hang.yuan 2022/4/13 15:23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class PrimarySchoolAndAboveVisionAnalysisDO implements VisionAnalysis,FrontTableId {

    /**
     * 视力低下人数（默认0）
     */
    private Integer lowVisionNum;

    /**
     * 视力低下比例（均为整数，如10.01%，数据库则是1001）
     */
    private String lowVisionRatio;

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
    private String myopiaRatio;

    /**
     * 小学及以上--近视前期人数（默认0）
     */
    private Integer myopiaLevelEarlyNum;

    /**
     * 小学及以上--近视前期率
     */
    private String myopiaLevelEarlyRatio;

    /**
     * 小学及以上--低度近视人数（默认0）
     */
    private Integer lowMyopiaNum;

    /**
     * 小学及以上--低度近视率
     */
    private String lowMyopiaRatio;

    /**
     * 小学及以上--高度近视人数（默认0）
     */
    private Integer highMyopiaNum;
    /**
     * 小学及以上--高度近视率
     */
    private String highMyopiaRatio;

    /**
     * 小学及以上--散光数人数（默认0）
     */
    private Integer astigmatismNum;

    /**
     * 小学及以上--散光数率
     */
    private String astigmatismRatio;

    /**
     * 戴镜人数（默认0）
     */
    private Integer wearingGlassesNum;

    /**
     * 戴镜率（均为整数，如10.01%，数据库则是1001）
     */
    private String wearingGlassesRatio;


    /**
     * 小学及以上--夜戴角膜塑形镜人数（默认0）
     */
    private Integer nightWearingOrthokeratologyLensesNum;

    /**
     * 小学及以上--夜戴角膜塑形镜率
     */
    private String nightWearingOrthokeratologyLensesRatio;

    /**
     * 建议就诊数量（默认0）
     */
    private Integer treatmentAdviceNum;

    /**
     * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
     */
    private String treatmentAdviceRatio;

    /**
     * 学校类型
     */
    private Integer schoolType;

    @Override
    public Integer getSerialVersionUID() {
        return 3;
    }

    public BigDecimal getAvgLeftVision() {
        if(Objects.nonNull(avgLeftVision)){
            return BigDecimalUtil.keepDecimalPlaces(avgLeftVision,1);
        }
        return null;
    }

    public BigDecimal getAvgRightVision() {
        if(Objects.nonNull(avgRightVision)){
            return BigDecimalUtil.keepDecimalPlaces(avgRightVision,1);
        }
        return null;
    }
}
