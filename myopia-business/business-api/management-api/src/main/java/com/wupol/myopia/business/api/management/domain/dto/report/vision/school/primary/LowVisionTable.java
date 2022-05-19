package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力低下通用表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowVisionTable {

    /**
     * 项目
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 轻度视力低下人数
     */
    private Integer lightVisionCount;

    /**
     * 轻度视力低下人数-百分比
     */
    private Integer lightVisionProportion;

    /**
     * 中度视力低下人数
     */
    private Integer middleVisionCount;

    /**
     * 中度视力低下人数-百分比
     */
    private Integer middleVisionProportion;

    /**
     * 重度视力低下人数
     */
    private Integer highVisionCount;

    /**
     * 重度视力低下人数-百分比
     */
    private Integer highVisionProportion;

    /**
     * 平均视力
     */
    private String avgVision;


}
