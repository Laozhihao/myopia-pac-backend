package com.wupol.myopia.business.core.stat.domain.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 幼儿园视力分析
 *
 * @author hang.yuan 2022/4/13 15:13
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class KindergartenVisionAnalysisDO implements VisionAnalysis,FrontTableId {
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
     * 戴镜人数（默认0）
     */
    private Integer wearingGlassesNum;

    /**
     * 戴镜率（均为整数，如10.01%，数据库则是1001）
     */
    private String wearingGlassesRatio;

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
        return 2;
    }

}
