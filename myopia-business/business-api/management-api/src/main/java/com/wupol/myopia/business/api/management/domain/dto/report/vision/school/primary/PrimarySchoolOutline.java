package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.Outline;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionWarningSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 概述
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimarySchoolOutline {

    /**
     * 概述
     */
    private Outline outline;

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 屈光情况
     */
    private RefractionSituation refractionSituation;

    /**
     * 视力预警监测情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;

    /**
     * 历年视力情况
     */
    private PrimaryHistoryVision kindergartenHistoryVision;


}
