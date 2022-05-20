package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 预警表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningTable {

    /**
     * 年级名称
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 0级预警人数
     */
    private Long zeroWarningCount;

    /**
     * 0级预警人数百分比
     */
    private String zeroWarningProportion;

    /**
     * 1级预警人数
     */
    private Long oneWarningCount;

    /**
     * 1级预警人数百分比
     */
    private String oneWarningProportion;

    /**
     * 2级预警人数
     */
    private Long twoWarningCount;

    /**
     * 2级预警人数百分比
     */
    private String twoWarningProportion;

    /**
     * 3级预警人数
     */
    private Long threeWarningCount;

    /**
     * 3级预警人数百分比
     */
    private String threeWarningProportion;

    /**
     * 建议就诊人数
     */
    private Long recommendDoctorCount;

    /**
     * 建议就诊百分比
     */
    private String recommendDoctorProportion;
}
