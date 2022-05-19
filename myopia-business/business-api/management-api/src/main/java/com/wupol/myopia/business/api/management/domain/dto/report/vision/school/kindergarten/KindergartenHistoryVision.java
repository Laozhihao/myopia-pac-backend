package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

/**
 * 历年视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class KindergartenHistoryVision {

    /**
     * 视力低下
     */
    private String lowVision;

    /**
     * 远视储备不足
     */
    private String insufficientFarsightednessReserve;

    /**
     * 屈光不正
     */
    private String refractiveError;

    /**
     * 屈光参差
     */
    private String anisometropia;
}
