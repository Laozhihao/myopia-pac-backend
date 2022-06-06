package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 身高体重监测-占比
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class HeightAndWeightRatioVO {
    /**
     * 超重率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal overweightRatio;
    /**
     * 肥胖率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal obeseRatio;

    /**
     * 营养不良率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal malnourishedRatio;

    /**
     * 生长迟缓率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal stuntingRatio;

}