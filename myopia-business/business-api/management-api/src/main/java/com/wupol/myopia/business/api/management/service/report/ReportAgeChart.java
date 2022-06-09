package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 报告图表-年龄
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@Service
public class ReportAgeChart {


    /**
     * 不同年龄段-图表
     */
    public static void getAgeMonitorChart(List<StatConclusion> statConclusionList, AgeChartVO ageChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        ChartVO.AgeChart ageChart = new ChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);

        List<String> y = Lists.newArrayList();
        List<ChartVO.AgeData> x = Lists.newArrayList();
        getAgeX(ageChartVO, x);

        List<BigDecimal> valueList = Lists.newArrayList();

        dynamicAgeSegmentList.forEach(age -> {
            List<StatConclusion> statConclusions = ageMap.get(age);
            if (Objects.nonNull(statConclusions)) {
                y.add(AgeSegmentEnum.get(age).getDesc());
                setAgeData(ageChartVO, x, statConclusions, valueList);
            }
        });
        ageChart.setY(y);
        ageChart.setX(x);
        ageChart.setMaxValue(CollectionUtil.max(valueList));
        setAgeChartVO(ageChartVO, ageChart);
    }


    private static void getAgeX(AgeChartVO ageChartVO, List<ChartVO.AgeData> x) {

        switch (ageChartVO.type()) {
            case 1:
                x.addAll(Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA_LOSS, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA_REPAIR, Lists.newArrayList())
                ));
                break;
            case 2:
                x.addAll(Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.OVERWEIGHT, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.OBESE, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.MALNOURISHED, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.STUNTING, Lists.newArrayList())
                ));
                break;
            case 3:
                x.addAll(Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.HIGH_BLOOD_PRESSURE, Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.ABNORMAL_SPINE_CURVATURE, Lists.newArrayList())
                ));
                break;
            default:
                break;
        }
    }

    private static void setAgeData(AgeChartVO ageChartVO, List<ChartVO.AgeData> x, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        switch (ageChartVO.type()) {
            case 1:
                getSaprodontiaAgeData(x, statConclusionList, valueList);
                break;
            case 2:
                getHeightAndWeightAgeData(x, statConclusionList, valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureAgeData(x, statConclusionList, valueList);
                break;
            default:
                break;
        }
    }

    private static void setAgeChartVO(AgeChartVO ageChartVO, ChartVO.AgeChart ageChart) {
        switch (ageChartVO.type()) {
            case 1:
                ageChartVO.setSaprodontiaAgeMonitorChart(ageChart);
                break;
            case 2:
                ageChartVO.setHeightAndWeightAgeMonitorChart(ageChart);
                break;
            case 3:
                ageChartVO.setBloodPressureAndSpinalCurvatureAgeMonitorChart(ageChart);
                break;
            default:
                break;
        }
    }

    private static void getSaprodontiaAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getSaprodontiaRatio());
        data.get(1).getData().add(num.getSaprodontiaLossRatio());
        data.get(2).getData().add(num.getSaprodontiaRepairRatio());
        valueList.add(num.getSaprodontiaRatio());
        valueList.add(num.getSaprodontiaLossRatio());
        valueList.add(num.getSaprodontiaRepairRatio());
    }

    private static void getHeightAndWeightAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getOverweightRatio());
        data.get(1).getData().add(num.getObeseRatio());
        data.get(2).getData().add(num.getMalnourishedRatio());
        data.get(3).getData().add(num.getStuntingRatio());
        valueList.add(num.getOverweightRatio());
        valueList.add(num.getObeseRatio());
        valueList.add(num.getMalnourishedRatio());
        valueList.add(num.getStuntingRatio());
    }

    private static void getBloodPressureAndSpinalCurvatureAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getHighBloodPressureRatio());
        data.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
        valueList.add(num.getHighBloodPressureRatio());
        valueList.add(num.getAbnormalSpineCurvatureRatio());
    }

}
