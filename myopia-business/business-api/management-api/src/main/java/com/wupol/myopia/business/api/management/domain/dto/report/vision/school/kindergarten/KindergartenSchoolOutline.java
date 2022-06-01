package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.HistoryRefractive;
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
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;

    /**
     * 历年屈光情况趋势分析
     */
    private HistoryRefractive historyRefractiveInfo;


}
