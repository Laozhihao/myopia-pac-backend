package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.LowVisionLevelTable;
import lombok.Getter;
import lombok.Setter;

/**
 * 历年视力情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaHistoryLowVisionTable extends LowVisionLevelTable {

    /**
     * 视力低常人数
     */
    private Long kLowVisionCount;

    /**
     * 视力低常占比
     */
    private String kLowVisionProportion;
}
