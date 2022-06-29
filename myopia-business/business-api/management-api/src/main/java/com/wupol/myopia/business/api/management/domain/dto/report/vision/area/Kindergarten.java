package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 幼儿园统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Kindergarten {

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 远视储备不足
     */
    private CountAndProportion insufficientFarsightednessReserve;

    /**
     * 屈光参差
     */
    private CountAndProportion anisometropia;

    /**
     * 屈光不正
     */
    private CountAndProportion refractiveError;
}
