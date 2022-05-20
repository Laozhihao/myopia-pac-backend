package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
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
    private HighLowProportion notWearing;

    /**
     * 框架眼镜
     */
    private HighLowProportion glasses;

    /**
     * 隐形眼镜
     */
    private HighLowProportion contact;

    /**
     * 夜戴角膜塑形镜
     */
    private HighLowProportion night;

    /**
     * 足矫
     */
    private HighLowProportion enough;

    /**
     * 欠矫
     */
    private HighLowProportion under;

    /**
     * 未矫
     */
    private HighLowProportion uncorrected;
}
