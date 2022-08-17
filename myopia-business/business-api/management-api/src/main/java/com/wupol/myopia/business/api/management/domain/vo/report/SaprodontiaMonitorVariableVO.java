package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 龋齿监测-变量说明
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class SaprodontiaMonitorVariableVO {
    /**
     * 龋均
     */
    private String dmftRatio;

    /**
     * 龋患率
     */
    private String saprodontiaRatio;

    /**
     * 龋补率
     */
    private String saprodontiaRepairRatio;
    /**
     * 龋患（失、补）率
     */
    private String saprodontiaLossAndRepairRatio;

    /**
     * 龋患（失、补）构成比
     */
    private String saprodontiaLossAndRepairTeethRatio;


}