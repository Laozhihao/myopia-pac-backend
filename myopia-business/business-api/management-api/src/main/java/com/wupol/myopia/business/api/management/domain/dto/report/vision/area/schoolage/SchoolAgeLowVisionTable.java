package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import lombok.Getter;
import lombok.Setter;

/**
 * 年龄段视力低下表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolAgeLowVisionTable {

    /**
     * 年级
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 平均视力
     */
    private String avgVision;

    /**
     * 视力低下人数
     */
    private Integer lowVisionCount;

    /**
     * 视力低下占比
     */
    private Integer lowVisionProportion;

    /**
     * 轻度-视力低下人数
     */
    private Integer lightLowVisionCount;

    /**
     * 轻度-视力低下占比
     */
    private Integer lightLowVisionProportion;

    /**
     * 中度-视力低下人数
     */
    private Integer middleLowVisionCount;

    /**
     * 中度-视力低下占比
     */
    private Integer middleLowVisionProportion;

    /**
     * 重度-视力低下人数
     */
    private Integer highLowVisionCount;

    /**
     * 重度-视力低下占比
     */
    private Integer highLowVisionProportion;


}
