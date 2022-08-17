package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄视力低下情况
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
     * 信息
     */
    private LowVisionInfo info;

    /**
     * 图格
     */
    private HorizontalChart ageLowVisionChart;

    /**
     * 表格
     */
    private List<CommonLowVisionTable> tables;
}
