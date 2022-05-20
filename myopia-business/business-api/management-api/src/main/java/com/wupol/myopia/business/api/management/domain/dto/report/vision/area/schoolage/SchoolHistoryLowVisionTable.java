package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import lombok.Getter;
import lombok.Setter;

/**
 * 历年视力情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolHistoryLowVisionTable {

    /**
     * 时间
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 视力低常人数
     */
    private Long kLowVisionCount;

    /**
     * 视力低常占比
     */
    private String kLowVisionProportion;

    /**
     * 视力低下人数
     */
    private Long lowVisionCount;

    /**
     * 视力低下占比
     */
    private String lowVisionProportion;

    /**
     * 轻度-视力低下人数
     */
    private Long lightLowVisionCount;

    /**
     * 轻度-视力低下占比
     */
    private String lightLowVisionProportion;

    /**
     * 中度-视力低下人数
     */
    private Long middleLowVisionCount;

    /**
     * 中度-视力低下占比
     */
    private String middleLowVisionProportion;

    /**
     * 重度-视力低下人数
     */
    private Long highLowVisionCount;

    /**
     * 重度-视力低下占比
     */
    private String highLowVisionProportion;
}
