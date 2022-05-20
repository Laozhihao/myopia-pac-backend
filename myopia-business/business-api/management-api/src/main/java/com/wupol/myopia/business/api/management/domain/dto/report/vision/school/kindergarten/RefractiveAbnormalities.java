package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 屈光异常
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractiveAbnormalities {

    /**
     * 屈光不正
     */
    private CountAndProportion refractiveError;

    /**
     * 屈光参差
     */
    private CountAndProportion anisometropia;

    /**
     * 远视储备不足
     */
    private CountAndProportion insufficient;
}
