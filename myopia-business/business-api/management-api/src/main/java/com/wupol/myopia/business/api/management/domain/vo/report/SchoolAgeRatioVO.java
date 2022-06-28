package com.wupol.myopia.business.api.management.domain.vo.report;

/**
 * 学龄
 *
 * @author hang.yuan 2022/6/8 17:34
 */
public interface SchoolAgeRatioVO {

    default String getSaprodontiaRatio() {
        return null;
    }

    default String getSaprodontiaLossRatio() {
        return null;
    }

    default String getSaprodontiaRepairRatio() {
        return null;
    }

    default String getOverweightRatio() {
        return null;
    }

    default String getObeseRatio() {
        return null;
    }

    default String getMalnourishedRatio() {
        return null;
    }

    default String getStuntingRatio() {
        return null;
    }

    default String getHighBloodPressureRatio() {
        return null;
    }

    default String getAbnormalSpineCurvatureRatio() {
        return null;
    }

    Integer type();

}
