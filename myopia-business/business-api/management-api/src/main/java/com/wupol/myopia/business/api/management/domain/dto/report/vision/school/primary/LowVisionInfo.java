package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 小学及以上视力低下信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowVisionInfo {

    /**
     * 视力低下人数
     */
    private HighLowProportion lowVision;
    
    /**
     * 轻度视力低下人数
     */
    private HighLowProportion lightVision;
    
    /**
     * 中度视力低下人数
     */
    private HighLowProportion middleVision;
    
    /**
     * 重度视力低下人数
     */
    private HighLowProportion highVision;

}
