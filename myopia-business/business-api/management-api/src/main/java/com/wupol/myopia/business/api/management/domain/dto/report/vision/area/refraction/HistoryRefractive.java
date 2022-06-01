package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 历年屈光情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HistoryRefractive {

    /**
     * 视力低下
     */
    private String lowVisionProportion;

    /**
     * 远视储备不足
     */
    private String insufficientProportion;

    /**
     * 屈光不正率
     */
    private String refractiveErrorProportion;

    /**
     * 屈光参差率
     */
    private String anisometropiaProportion;

    /**
     * 幼儿园屈光图表
     */
    private HorizontalChart kindergartenHistoryRefractive;

    /**
     * 表格
     */
    private List<RefractiveTable> tables;
}
