package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.AreaHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.ConvertRatio;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 历年视力情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowVisionHistory {

    /**
     * 幼儿园
     */
    private ConvertRatio kConvertRatio;

    /**
     * 小学及以上
     */
    private ConvertRatio pConvertRatio;

    /**
     * 图表
     */
    private HorizontalChart historyLowVisionChart;

    /**
     * 表格
     */
    private List<AreaHistoryLowVisionTable> tables;
}
