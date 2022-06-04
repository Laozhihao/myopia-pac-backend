package com.wupol.myopia.business.api.management.domain.vo.report;

/**
 * 年龄段图表
 *
 * @author hang.yuan 2022/6/2 11:15
 */
public interface AgeChartVO {

    default void setSaprodontiaAgeMonitorChart(ChartVO.AgeChart ageChart){}

    default void setHeightAndWeightAgeMonitorChart(ChartVO.AgeChart ageChart){}

    default void setBloodPressureAndSpinalCurvatureAgeMonitorChart(ChartVO.AgeChart ageChart){}

    Integer type();
}
