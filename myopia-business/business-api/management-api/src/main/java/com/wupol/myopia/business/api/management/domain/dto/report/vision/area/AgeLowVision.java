package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄不同程度视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeLowVision {

    /**
     * 学龄段
     */
    private String ageRange;

    /**
     * 视力低下
     */
    private HighLowProportion low;

    /**
     * 轻度-视力低下
     */
    private HighLowProportion light;

    /**
     * 中度-视力低下
     */
    private HighLowProportion middle;

    /**
     * 重度-视力低下
     */
    private HighLowProportion high;

    /**
     * 图表
     */
    private HorizontalChart ageLowVisionChart;

    /**
     * 视力低下等级图表
     */
    private HorizontalChart ageLowVisionLevelChart;

    /**
     * 表格
     */
    private List<CommonLowVisionTable> tables;


}
