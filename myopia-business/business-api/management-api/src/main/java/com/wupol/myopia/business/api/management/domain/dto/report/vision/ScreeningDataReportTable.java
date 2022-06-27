package com.wupol.myopia.business.api.management.domain.dto.report.vision;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力详情表
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningDataReportTable {
    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 戴镜情况
     */
    private String glassesType;

    /**
     * 裸眼视力
     */
    private String nakedVision;

    /**
     * 矫正视力
     */
    private String correctedVision;

    /**
     * 球镜
     */
    private String sph;

    /**
     * 柱镜
     */
    private String cyl;

    /**
     * 轴位
     */
    private String axsi;

    /**
     * 等效球镜
     */
    private String se;

    /**
     * 视力分析
     */
    private Boolean visionInfo;

    /**
     * 屈光分析
     */
    private String refractiveInfo;

    /**
     * 近视矫正
     */
    private String myopiaCorrection;

    /**
     * 视力预警
     */
    private String visionWarning;

    /**
     * 建议就诊
     */
    private Boolean isRecommendDoctor;

    /**
     * 备注
     */
    private String remark;
}
