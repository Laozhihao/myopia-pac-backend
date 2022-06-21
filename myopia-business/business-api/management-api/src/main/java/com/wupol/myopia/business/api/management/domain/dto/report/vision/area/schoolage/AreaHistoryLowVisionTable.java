package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 历年视力情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaHistoryLowVisionTable extends CommonTable {

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
