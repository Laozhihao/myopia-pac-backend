package com.wupol.myopia.business.parent.domain.dto;

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
public class NakedVisionItems {

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
