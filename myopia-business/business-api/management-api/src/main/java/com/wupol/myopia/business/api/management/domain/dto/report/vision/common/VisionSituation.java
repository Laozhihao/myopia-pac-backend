package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionSituation {

    /**
     * 视力低下人数
     */
    private Integer lowTotal;

    /**
     * 视力低下比例
     */
    private String lowProportion;

    /**
     * 平均视力
     */
    private String avgVision;
}
