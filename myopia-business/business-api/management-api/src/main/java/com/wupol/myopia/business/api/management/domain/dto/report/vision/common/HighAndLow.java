package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 最高视力低下与最高
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HighAndLow {

    /**
     * 项目名
     */
    private String name;

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
