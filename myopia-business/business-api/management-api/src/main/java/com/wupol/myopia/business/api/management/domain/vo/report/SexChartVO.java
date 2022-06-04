package com.wupol.myopia.business.api.management.domain.vo.report;

/**
 * 不同性别图表
 *
 * @author hang.yuan 2022/6/2 10:59
 */
public interface SexChartVO {

    default void setSaprodontiaSexMonitorChart(ChartVO.Chart chart){}

    default void setHeightAndWeightSexMonitorChart(ChartVO.Chart chart){}

    default void setBloodPressureAndSpinalCurvatureSexMonitorChart(ChartVO.Chart chart){}

    Integer type();
}
