package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 身高体重监测-变量说明
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
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