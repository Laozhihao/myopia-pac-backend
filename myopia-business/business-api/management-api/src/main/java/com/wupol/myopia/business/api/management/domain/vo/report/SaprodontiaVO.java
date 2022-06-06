package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 龋齿监测
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SaprodontiaVO extends SaprodontiaRatioVO {

    /**
     * 龋失补牙数
     */
    private Integer dmftNum;

    /**
     * 龋均
     */
    private String dmftRatio;

    /**
     * 有龋人数
     */
    private Integer saprodontiaNum;

    /**
     * 龋失人数
     */
    private Integer saprodontiaLossNum;

    /**
     * 龋补人数
     */
    private Integer saprodontiaRepairNum;

    /**
     * 龋患（失、补）人数
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 龋患（失、补）牙数
     */
    private Integer saprodontiaLossAndRepairTeethNum;

}