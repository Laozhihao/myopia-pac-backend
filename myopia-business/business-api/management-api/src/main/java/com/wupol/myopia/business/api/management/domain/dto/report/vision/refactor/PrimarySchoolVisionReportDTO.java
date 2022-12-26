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
     * 视力矫正情况
     */
    private VisionCorrectionSituationDTO visionCorrectionSituationDTO;

    /**
     * 屈光情况
     */
    private RefractiveSituationDTO refractiveSituationDTO;

    /**
     * 预警情况
     */
    private WarningSituationDTO warningSituationDTO;
}
