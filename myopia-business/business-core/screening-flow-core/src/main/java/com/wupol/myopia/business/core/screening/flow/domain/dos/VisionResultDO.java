package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 视力检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionResultDO {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;

    /**
     * 矫正视力
     */
    private BigDecimal correctedVision;

    /**
     * 裸眼视力
     */
    private BigDecimal nakedVision;

}
