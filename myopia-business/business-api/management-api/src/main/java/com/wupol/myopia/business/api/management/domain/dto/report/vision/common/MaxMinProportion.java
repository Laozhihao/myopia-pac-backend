package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 最高最低描述
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MaxMinProportion extends HighLowProportion {

    /**
     * 百分比
     */
    private String proportion;

    public MaxMinProportion(String proportion, String maxName, String maxProportion, String minName, String minProportion) {
        super(maxName, maxProportion, minName, minProportion);
        this.proportion = proportion;
    }

    public MaxMinProportion(String proportion) {
        this.proportion = proportion;
    }
}
