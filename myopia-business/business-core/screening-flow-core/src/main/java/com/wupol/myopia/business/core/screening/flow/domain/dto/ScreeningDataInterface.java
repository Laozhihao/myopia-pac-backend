package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;

/**
 * @Description
 * @Date 2021/1/26 11:53
 * @Author by Jacob
 */
public interface ScreeningDataInterface {
    /**
     * 构建筛查数据
     * @param visionScreeningResult
     * @return
     */
    VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult);

}