package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力低下
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionSituation {

    /**
     * 视力低下人数
     */
    private Long count;

    /**
     * 视力低下比例
     */
    private String proportion;

    /**
     * 平均视力
     */
    private String avgVision;
}
