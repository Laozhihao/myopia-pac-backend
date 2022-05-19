package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 屈光表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractiveTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 远视储备不足-有效人数
     */
    private Integer insufficientStudentCount;

    /**
     * 远视储备不足-百分比
     */
    private Integer insufficientPercentage;

    /**
     * 屈光不正-有效人数
     */
    private Integer refractiveErrorStudentCount;

    /**
     * 屈光不正-百分比
     */
    private Integer refractiveErrorPercentage;

    /**
     * 屈光参差-有效人数
     */
    private Integer anisometropiaStudentCount;

    /**
     * 屈光参差-百分比
     */
    private Integer anisometropiaPercentage;

    /**
     * 建议就诊-有效人数
     */
    private CountAndProportion recommendDoctorCount;

    /**
     * 建议就诊-百分比
     */
    private CountAndProportion recommendDoctorProportion;
}
