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
     * 视力低下人数
     */
    private Long lowVisionCount;

    /**
     * 视力低下人数-百分比
     */
    private String lowVisionProportion;

    /**
     * 轻度视力低下人数
     */
    private Long lightVisionCount;

    /**
     * 轻度视力低下人数-百分比
     */
    private String lightVisionProportion;

    /**
     * 中度视力低下人数
     */
    private Long middleVisionCount;

    /**
     * 中度视力低下人数-百分比
     */
    private String middleVisionProportion;

    /**
     * 重度视力低下人数
     */
    private Long highVisionCount;

    /**
     * 重度视力低下人数-百分比
     */
    private String highVisionProportion;

    /**
     * 平均视力
     */
    private String avgVision;


}
