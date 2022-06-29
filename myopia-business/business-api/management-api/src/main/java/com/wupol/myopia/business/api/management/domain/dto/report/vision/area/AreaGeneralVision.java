package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.AreaRefraction;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力总体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaGeneralVision {

    /**
     * 视力低下
     */
    private AreaLowVision areaLowVision;

    /**
     * 屈光整体情况
     */
    private AreaRefraction areaRefraction;

    /**
     * 近视预警情况
     */
    private WarningSituation warningSituation;
}
