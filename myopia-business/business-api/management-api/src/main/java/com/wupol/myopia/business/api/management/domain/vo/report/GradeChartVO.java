package com.wupol.myopia.business.api.management.domain.vo.report;

/**
 * 不同班级图表
 *
 * @author hang.yuan 2022/6/8 15:49
 */
public interface GradeChartVO {

    default void setSaprodontiaGradeMonitorChart(ChartVO.ReverseChart chart){}

    default void setHeightAndWeightGradeMonitorChart(ChartVO.ReverseChart chart){}

    default void setBloodPressureAndSpinalCurvatureGradeMonitorChart(ChartVO.ReverseChart chart){}

    Integer type();
}
