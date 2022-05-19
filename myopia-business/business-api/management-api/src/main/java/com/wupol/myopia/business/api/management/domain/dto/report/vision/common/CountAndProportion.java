package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 基础百分比
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CountAndProportion {

    /**
     * 人数
     */
    private Integer count;

    /**
     * 百分比
     */
    private String proportion;
}
