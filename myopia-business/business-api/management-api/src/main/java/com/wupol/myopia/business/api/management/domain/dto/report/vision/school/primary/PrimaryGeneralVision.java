package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningSituation;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.LowMyopiaInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力总体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryGeneralVision {

    /**
     * 视力低下情况
     */
    private LowMyopiaInfo lowMyopiaInfo;

    /**
     * 散光情况
     */
    private AstigmatismInfo astigmatismInfo;

    /**
     * 矫正戴镜情况
     */
    private WearingGlassesInfo wearingGlassesInfo;

    /**
     * 近视预警情况
     */
    private WarningSituation warningSituation;
}
