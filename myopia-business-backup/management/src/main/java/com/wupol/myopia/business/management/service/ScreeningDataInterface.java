package com.wupol.myopia.business.management.service;

import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;

/**
 * @Description
 * @Date 2021/1/26 11:53
 * @Author by Jacob
 */
public interface ScreeningDataInterface {
    VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult);

}