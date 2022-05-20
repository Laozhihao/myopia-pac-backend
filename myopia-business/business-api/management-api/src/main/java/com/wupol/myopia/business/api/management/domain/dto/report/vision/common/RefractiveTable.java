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
    private Long insufficientStudentCount;

    /**
     * 远视储备不足-百分比
     */
    private String insufficientProportion;

    /**
     * 屈光不正-有效人数
     */
    private Long refractiveErrorStudentCount;

    /**
     * 屈光不正-百分比
     */
    private String refractiveErrorProportion;

    /**
     * 屈光参差-有效人数
     */
    private Long anisometropiaStudentCount;

    /**
     * 屈光参差-百分比
     */
    private String anisometropiaProportion;

    /**
     * 建议就诊-有效人数
     */
    private Long recommendDoctorCount;

    /**
     * 建议就诊-百分比
     */
    private String recommendDoctorProportion;

    /**
     * 视力低下-有效人数
     */
    private Long lowVisionStudentCount;

    /**
     * 视力低下-百分比
     */
    private String lowVisionProportion;
}
