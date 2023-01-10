package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Getter;
import lombok.Setter;

/**
 * 中小学视力报告
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimarySchoolVisionReportDTO {

    /**
     * 概述
     */
    private ScreeningSummaryDTO summary;

    /**
     * 学生近视情况
     */
    private MyopiaInfoDTO studentMyopia;

    /**
     * 学生视力情况
     */
    private VisionInfoDTO studentVision;

    /**
     * 视力矫正情况
     */
    private VisionCorrectionSituationDTO visionCorrectionSituation;

    /**
     * 屈光情况
     */
    private RefractiveSituationDTO refractiveSituation;

    /**
     * 预警情况
     */
    private WarningSituationDTO warningSituation;
}
