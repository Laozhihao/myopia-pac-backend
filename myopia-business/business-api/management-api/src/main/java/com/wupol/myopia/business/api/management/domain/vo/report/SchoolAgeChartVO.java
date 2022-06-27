package com.wupol.myopia.business.api.management.domain.vo.report;

/**
 * 不同学龄图表
 *
 * @author hang.yuan 2022/6/8 15:49
 */
public interface SchoolAgeChartVO {

    default void setSaprodontiaSchoolAgeMonitorChart(ChartVO.Chart chart){}

    default void setHeightAndWeightSchoolAgeMonitorChart(ChartVO.Chart chart){}

    default void setBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(ChartVO.Chart chart){}

    Integer type();
}
