package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告图表-班级
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@Service
public class ReportGradeChart {


    /**
     * 不同班级
     */
    public static void getGradeMonitorChart(List<StatConclusion> statConclusionList, GradeChartVO gradeChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        ChartVO.ReverseChart chart = new ChartVO.ReverseChart();
        List<String> y = Lists.newArrayList();
        List<ChartVO.ChartData> x = Lists.newArrayList();
        getGradeX(gradeChartVO.type(), x);

        List<BigDecimal> valueList = Lists.newArrayList();
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        gradeCodeMap.forEach((gradeCode, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            y.add(gradeCodeEnum.getName());
            setGradeData(gradeChartVO.type(), x, list, valueList);
        });
        chart.setX(x);
        chart.setY(y);
        chart.setMaxValue(CollectionUtil.max(valueList));
        setGradeChartVO(gradeChartVO, chart);
    }

    private static void getGradeX(Integer type, List<ChartVO.ChartData> x) {
        switch (type) {
            case 1:
                x.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA_LOSS, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.SAPRODONTIA_REPAIR, Lists.newArrayList())
                ));
                break;
            case 2:
                x.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.OVERWEIGHT, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.OBESE, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.MALNOURISHED, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.STUNTING, Lists.newArrayList())
                ));
                break;
            case 3:
                x.addAll(Lists.newArrayList(
                        new ChartVO.ChartData(ReportConst.HIGH_BLOOD_PRESSURE, Lists.newArrayList()),
                        new ChartVO.ChartData(ReportConst.ABNORMAL_SPINE_CURVATURE, Lists.newArrayList())
                ));
                break;
            default:
                break;
        }
    }

    private static void setGradeData(Integer type, List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        switch (type) {
            case 1:
                getSaprodontiaGradeData(x, statConclusionList, valueList);
                break;
            case 2:
                getHeightAndWeightGradeData(x, statConclusionList, valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureGradeData(x, statConclusionList, valueList);
                break;
            default:
                break;
        }

    }

    private static void getSaprodontiaGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(0).getData().add(num.getSaprodontiaRatio());
        x.get(1).getData().add(num.getSaprodontiaLossRatio());
        x.get(2).getData().add(num.getSaprodontiaRepairRatio());
        valueList.add(num.getSaprodontiaRatio());
        valueList.add(num.getSaprodontiaLossRatio());
        valueList.add(num.getSaprodontiaRepairRatio());
    }

    private static void getHeightAndWeightGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(0).getData().add(num.getOverweightRatio());
        x.get(1).getData().add(num.getObeseRatio());
        x.get(2).getData().add(num.getMalnourishedRatio());
        x.get(3).getData().add(num.getStuntingRatio());
        valueList.add(num.getOverweightRatio());
        valueList.add(num.getObeseRatio());
        valueList.add(num.getMalnourishedRatio());
        valueList.add(num.getStuntingRatio());
    }

    private static void getBloodPressureAndSpinalCurvatureGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(0).getData().add(num.getHighBloodPressureRatio());
        x.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
        valueList.add(num.getHighBloodPressureRatio());
        valueList.add(num.getAbnormalSpineCurvatureRatio());
    }

    private static void setGradeChartVO(GradeChartVO gradeChartVO, ChartVO.ReverseChart chart) {
        switch (gradeChartVO.type()) {
            case 1:
                gradeChartVO.setSaprodontiaGradeMonitorChart(chart);
                break;
            case 2:
                gradeChartVO.setHeightAndWeightGradeMonitorChart(chart);
                break;
            case 3:
                gradeChartVO.setBloodPressureAndSpinalCurvatureGradeMonitorChart(chart);
                break;
            default:
                break;
        }
    }
}
