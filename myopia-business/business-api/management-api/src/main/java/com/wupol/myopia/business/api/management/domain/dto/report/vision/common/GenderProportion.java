package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 性别百分比
 *
 * @author Simple4H
 */
@Setter
@Getter
public class GenderProportion {

    /**
     * 百分比
     */
    private String proportion;

    /**
     * 男百分比
     */
    private String mPercentage;

    /**
     * 女百分比
     */
    private String fPercentage;
}
