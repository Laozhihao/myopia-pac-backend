package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HeightAndWeightVO extends HeightAndWeightRatioVO {

    /**
     * 超重人数
     */
    private Integer overweightNum;

    /**
     * 肥胖人数
     */
    private Integer obeseNum;

    /**
     * 营养不良人数
     */
    private Integer malnourishedNum;
    /**
     * 生长迟缓人数
     */
    private Integer stuntingNum;

}