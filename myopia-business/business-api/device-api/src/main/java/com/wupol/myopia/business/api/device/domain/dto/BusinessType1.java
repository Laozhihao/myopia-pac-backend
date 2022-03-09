package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Getter
@Setter
public class BusinessType1 {

    private Integer planStudentId;

    private String leftNakedVision;

    private String rightNakedVision;

    private String leftCorrectedVision;

    private String rightCorrectedVision;

    private Long screeningTime;
}
