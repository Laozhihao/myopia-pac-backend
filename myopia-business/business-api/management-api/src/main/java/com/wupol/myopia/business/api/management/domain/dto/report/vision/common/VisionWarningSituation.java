package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力预警监测情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionWarningSituation {

    /**
     * 0级预警
     */
    private CountAndProportion zeroWarning;

    /**
     * 1级预警
     */
    private CountAndProportion oneWarning;

    /**
     * 2级预警
     */
    private CountAndProportion twoWarning;

    /**
     * 3级预警
     */
    private CountAndProportion threeWarning;
}
