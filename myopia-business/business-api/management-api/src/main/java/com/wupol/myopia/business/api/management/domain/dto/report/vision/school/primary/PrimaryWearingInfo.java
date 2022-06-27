package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 小学以上戴镜信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryWearingInfo {

    /**
     * 不戴镜
     */
    private MaxMinProportion notWearing;

    /**
     * 框架眼镜
     */
    private MaxMinProportion glasses;

    /**
     * 隐形眼镜
     */
    private MaxMinProportion contact;

    /**
     * 夜戴角膜塑形镜
     */
    private MaxMinProportion night;

    /**
     * 足矫
     */
    private MaxMinProportion enough;

    /**
     * 欠矫
     */
    private MaxMinProportion under;

    /**
     * 未矫
     */
    private MaxMinProportion uncorrected;
}
