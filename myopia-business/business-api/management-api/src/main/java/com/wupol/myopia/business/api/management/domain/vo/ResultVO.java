package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;

/**
 * 结果
 *
 * @author hang.yuan 2022/6/20 10:41
 */
public interface ResultVO {


    default void setDistrictId(Integer districtId) {}

    default void setRangeName(String rangeName) {}

    default void setBasicData(Integer districtId, String currentRangeName) {
        setDistrictId(districtId);
        setRangeName(currentRangeName);
    }

    default void  setCurrentData(ScreeningResultStatistic currentVisionStatistic){}
}
