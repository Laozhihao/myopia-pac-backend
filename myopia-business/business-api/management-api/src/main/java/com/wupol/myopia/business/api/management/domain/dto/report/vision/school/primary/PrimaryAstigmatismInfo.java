package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 小学及以上散光信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryAstigmatismInfo {

    /**
     * 近视
     */
    private HighLowProportion myopia;

    /**
     * 近视前期
     */
    private HighLowProportion earlyMyopia;

    /**
     * 低度近视
     */
    private HighLowProportion lightMyopia;

    /**
     * 高度近视
     */
    private HighLowProportion highMyopia;

    /**
     * 散光
     */
    private HighLowProportion astigmatism;
}
