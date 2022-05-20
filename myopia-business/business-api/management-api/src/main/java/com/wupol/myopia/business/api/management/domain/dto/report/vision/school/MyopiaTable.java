package com.wupol.myopia.business.api.management.domain.dto.report.vision.school;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class MyopiaTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 视力低下人数
     */
    private Long lowVision;

    /**
     * 视力低下人数-百分比
     */
    private String lowVisionProportion;

    /**
     * 近视人数
     */
    private Long myopia;

    /**
     * 近视-百分比
     */
    private String myopiaProportion;

    /**
     * 视力前期人数
     */
    private Long early;

    /**
     * 视力前期人数-百分比
     */
    private String earlyProportion;

    /**
     * 低度视力人数
     */
    private Long light;

    /**
     * 低度视力人数-百分比
     */
    private String lightProportion;

    /**
     * 中度视力人数
     */
    private Long middle;

    /**
     * 中度视力人数-百分比
     */
    private String middleProportion;

    /**
     * 高度视力人数
     */
    private Long high;

    /**
     * 高度视力人数-百分比
     */
    private String highProportion;


}
