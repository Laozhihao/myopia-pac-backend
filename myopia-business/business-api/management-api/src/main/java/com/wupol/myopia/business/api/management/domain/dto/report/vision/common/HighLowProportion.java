package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 最高最低
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HighLowProportion {

    /**
     * 最高描述
     */
    private String maxName;

    /**
     * 最高百分比
     */
    private String maxProportion;

    /**
     * 最低描述
     */
    private String minName;

    /**
     * 最低百分比
     */
    private String minProportion;
}
