package com.wupol.myopia.business.core.stat.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 龋齿情况
 *
 * @author hang.yuan 2022/4/13 15:33
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class SaprodontiaDO implements Serializable,FrontTableId {

    /**
     * 小学及以上--无龋人数（默认0）
     */
    private Integer saprodontiaFreeNum;

    /**
     * 小学及以上--无龋率
     */
    private String saprodontiaFreeRatio;

    /**
     * 小学及以上--龋均数（默认0）
     */
    private Integer dmftNum;

    /**
     * 小学及以上--龋均率
     */
    private String dmftRatio;

    /**
     * 小学及以上--龋患人数（默认0）
     */
    private Integer saprodontiaNum;

    /**
     * 小学及以上--龋患率
     */
    private String saprodontiaRatio;

    /**
     * 小学及以上--龋失人数（默认0）
     */
    private Integer saprodontiaLossNum;

    /**
     * 小学及以上--龋失率
     */
    private String saprodontiaLossRatio;

    /**
     * 小学及以上--龋补人数（默认0）
     */
    private Integer saprodontiaRepairNum;

    /**
     * 小学及以上--龋补率
     */
    private String saprodontiaRepairRatio;

    /**
     * 小学及以上--龋患（失、补）人数（默认0）
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 小学及以上--龋患（失、补）率
     */
    private String saprodontiaLossAndRepairRatio;

    /**
     * 小学及以上--龋患（失、补）构成比（默认0）
     */
    private Integer saprodontiaLossAndRepairTeethNum;

    /**
     * 小学及以上--龋患（失、补）构成比率
     */
    private String saprodontiaLossAndRepairTeethRatio;

    @Override
    public Integer getSerialVersionUID() {
        return 6;
    }
}
