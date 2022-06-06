package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 龋齿监测占比
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class SaprodontiaRatioVO {
    /**
     * 龋患率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaRatio;
    /**
     * 龋失率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossRatio;

    /**
     * 龋补率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaRepairRatio;
    /**
     * 龋患（失、补）率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossAndRepairRatio;

    /**
     * 龋患（失、补）构成比
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossAndRepairTeethRatio;

}