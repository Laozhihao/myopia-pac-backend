package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 龋齿情况
 *
 * @author hang.yuan 2022/4/13 15:33
 */
@Data
public class SaprodontiaDO implements Serializable {

    /**
     * 小学及以上--无龋人数（默认0）
     */
    private Integer saprodontiaFreeNum;

    /**
     * 小学及以上--无龋率
     */
    private BigDecimal saprodontiaFreeRatio;

    /**
     * 小学及以上--龋均人数（默认0）
     */
    private Integer dmftNum;

    /**
     * 小学及以上--龋均率
     */
    private BigDecimal dmftRatio;

    /**
     * 小学及以上--龋患人数（默认0）
     */
    private Integer saprodontiaNum;

    /**
     * 小学及以上--龋患率
     */
    private BigDecimal saprodontiaRatio;

    /**
     * 小学及以上--龋失人数（默认0）
     */
    private Integer saprodontiaLossNum;

    /**
     * 小学及以上--龋失率
     */
    private BigDecimal saprodontiaLossRatio;

    /**
     * 小学及以上--龋补人数（默认0）
     */
    private Integer saprodontiaRepairNum;

    /**
     * 小学及以上--龋补率
     */
    private BigDecimal saprodontiaRepairRatio;

    /**
     * 小学及以上--龋患（失、补）人数（默认0）
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 小学及以上--龋患（失、补）率
     */
    private BigDecimal saprodontiaLossAndRepairRatio;

    /**
     * 小学及以上--龋患（失、补）构成比（默认0）
     */
    private Integer saprodontiaLossAndRepairTeethNum;

    /**
     * 小学及以上--龋患（失、补）构成比率
     */
    private BigDecimal saprodontiaLossAndRepairTeethRatio;
}
