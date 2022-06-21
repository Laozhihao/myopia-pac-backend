package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * CommonTable
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CommonTable {

    /**
     * 名称
     */
    private String name;

    /**
     * 人数
     */
    private Long validCount;

    /**
     * 是否本次报告
     */
    private Boolean isSameReport;
}
