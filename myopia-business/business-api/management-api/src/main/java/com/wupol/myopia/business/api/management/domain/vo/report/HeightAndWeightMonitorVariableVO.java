package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

@Data
public class HeightAndWeightMonitorVariableVO {
    /**
     * 超重率
     */
    private String overweightRatio;
    /**
     * 肥胖率
     */
    private String obeseRatio;

    /**
     * 营养不良率
     */
    private String malnourishedRatio;

    /**
     * 生长迟缓率
     */
    private String stuntingRatio;

}