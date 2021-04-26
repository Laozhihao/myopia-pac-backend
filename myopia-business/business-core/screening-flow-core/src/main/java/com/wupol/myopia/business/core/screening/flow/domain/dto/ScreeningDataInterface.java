package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;

/**
 * @Description
 * @Date 2021/1/26 11:53
 * @Author by Jacob
 */
public interface ScreeningDataInterface {
    VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult);

}