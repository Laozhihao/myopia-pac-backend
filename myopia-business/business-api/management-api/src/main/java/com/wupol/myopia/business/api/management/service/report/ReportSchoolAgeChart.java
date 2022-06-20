package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.ChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolAgeChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolAgeRatioVO;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报告图表-学龄
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@UtilityClass
public class ReportSchoolAgeChart {


    /**
     * 不同学龄-图表
     */
    public static void getSchoolAgeMonitorChart(List<TwoTuple<String, SchoolAgeRatioVO>> tupleList, SchoolAgeChartVO schoolAgeChartVO) {

        ChartVO.Chart chart = new ChartVO.Chart();
        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList();
        getSchoolAgeY(schoolAgeChartVO.type(), y);

        List<BigDecimal> valueList = Lists.newArrayList();
        tupleList.forEach(schoolAgeTuple -> {
            x.add(schoolAgeTuple.getFirst());
            setSchoolAgeData(schoolAgeChartVO.type(), y, schoolAgeTuple.getSecond(), valueList);
        });

        chart.setX(x);
        chart.setY(y);
        chart.setMaxValue(CollectionUtil.max(valueList));
        setSchoolAgeChartVO(schoolAgeChartVO, chart);
    }

    private static void getSchoolAgeY(Integer type, List<ChartVO.ChartData> y) {
        switch (type) {
            case 1:
                y.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA_LOSS, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA_REPAIR, Lists.newArrayList())
                ));
                break;
            case 2:
                y.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.OVERWEIGHT, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.OBESE, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.MALNOURISHED, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.STUNTING, Lists.newArrayList())
                ));
                break;
            case 3:
                y.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.HIGH_BLOOD_PRESSURE, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.ABNORMAL_SPINE_CURVATURE, Lists.newArrayList())
                ));
                break;
            default:
                break;
        }
    }


    private static void setSchoolAgeChartVO(SchoolAgeChartVO schoolAgeChartVO, ChartVO.Chart chart) {
        switch (schoolAgeChartVO.type()) {
            case 1:
                schoolAgeChartVO.setSaprodontiaSchoolAgeMonitorChart(chart);
                break;
            case 2:
                schoolAgeChartVO.setHeightAndWeightSchoolAgeMonitorChart(chart);
                break;
            case 3:
                schoolAgeChartVO.setBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(chart);
                break;
            default:
                break;
        }
    }

    private static void setSchoolAgeData(Integer type, List<ChartVO.ChartData> y, SchoolAgeRatioVO schoolAgeRatioVO, List<BigDecimal> valueList) {
        switch (type) {
            case 1:
                getSaprodontiaSchoolAgeData(y, schoolAgeRatioVO, valueList);
                break;
            case 2:
                getHeightAndWeightSchoolAgeData(y, schoolAgeRatioVO, valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureSchoolAgeData(y, schoolAgeRatioVO, valueList);
                break;
            default:
                break;
        }

    }

    private static void getSaprodontiaSchoolAgeData(List<ChartVO.ChartData> y, SchoolAgeRatioVO schoolAge, List<BigDecimal> valueList) {
        y.get(0).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRatio()));
        y.get(1).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaLossRatio()));
        y.get(2).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRepairRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaLossRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRepairRatio()));
    }

    private static void getHeightAndWeightSchoolAgeData(List<ChartVO.ChartData> y, SchoolAgeRatioVO schoolAge, List<BigDecimal> valueList) {
        y.get(0).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getOverweightRatio()));
        y.get(1).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getObeseRatio()));
        y.get(2).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getMalnourishedRatio()));
        y.get(3).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getStuntingRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getOverweightRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getObeseRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getMalnourishedRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getStuntingRatio()));
    }

    private static void getBloodPressureAndSpinalCurvatureSchoolAgeData(List<ChartVO.ChartData> y, SchoolAgeRatioVO schoolAge, List<BigDecimal> valueList) {
        y.get(0).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getHighBloodPressureRatio()));
        y.get(1).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getAbnormalSpineCurvatureRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getAbnormalSpineCurvatureRatio()));
        valueList.add(ReportUtil.getRatioNotSymbol(schoolAge.getAbnormalSpineCurvatureRatio()));
    }

}
