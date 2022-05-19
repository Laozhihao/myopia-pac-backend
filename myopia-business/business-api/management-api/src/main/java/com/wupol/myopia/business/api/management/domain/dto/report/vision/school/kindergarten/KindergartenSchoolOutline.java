package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.Outline;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionWarningSituation;

/**
 * 概述
 *
 * @author Simple4H
 */
public class KindergartenSchoolOutline {

    /**
     * 概述
     */
    private Outline outline;

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 屈光异常情况
     */
    private RefractiveAbnormalities refractiveAbnormalities;

    /**
     * 视力预警监测情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 历年视力情况
     */
    private KindergartenHistoryVision kindergartenHistoryVision;


}
