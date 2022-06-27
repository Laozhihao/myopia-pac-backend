package com.wupol.myopia.business.api.management.domain.dto.report.vision;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 中小学视力低下
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryLowVisionInfo {

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 轻度视力低下
     */
    private CountAndProportion lightLowVision;

    /**
     * 中度视力低下
     */
    private CountAndProportion middleLowVision;

    /**
     * 重度视力低下
     */
    private CountAndProportion highLowVision;
}
