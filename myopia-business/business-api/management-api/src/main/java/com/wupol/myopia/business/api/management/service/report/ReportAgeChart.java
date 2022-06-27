package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告图表-年龄
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@UtilityClass
public class ReportAgeChart {


    /**
     * 不同年龄段-图表
     */
    public static void getAgeMonitorChart(List<StatConclusion> statConclusionList, AgeChartVO ageChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        if (ageMap.size() <= 1){
            setAgeChartVO(ageChartVO,null);
            return;
        }
        ageMap = CollectionUtil.sort(ageMap, Comparator.comparing(Integer::intValue));

        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList();
        getAgeY(ageChartVO, y);


        ageMap.forEach((age,list) -> {
            x.add(AgeSegmentEnum.get(age).getDesc());
            setAgeData(ageChartVO, y, list);
        });

        if (Objects.equals(ageChartVO.type(),1) || Objects.equals(ageChartVO.type(),3)){
            ChartVO.Chart chart = new ChartVO.Chart();
            chart.setY(y);
            chart.setX(x);
            List<BigDecimal> collect = y.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
            chart.setMaxValue(CollectionUtil.max(collect));
            setAgeChartVO(ageChartVO, Lists.newArrayList(chart));
        }

        if (Objects.equals(ageChartVO.type(),2)){
            List<ChartVO.Chart> chartList =Lists.newArrayList();
            List<List<ChartVO.ChartData>> lists = CollectionUtil.splitList(y, 2);
            for (int i = 0; i < lists.size(); i++) {
                ChartVO.Chart chart = new ChartVO.Chart();
                List<ChartVO.ChartData> chartDataList = lists.get(i);
                chart.setY(chartDataList);
                chart.setX(x);
                List<BigDecimal> collect = chartDataList.stream().flatMap(data -> data.getData().stream()).collect(Collectors.toList());
                chart.setMaxValue(CollectionUtil.max(collect));
                chartList.add(chart);
            }
            setAgeChartVO(ageChartVO,chartList);
        }

    }


    private static void getAgeY(AgeChartVO ageChartVO, List<ChartVO.ChartData> y) {

        switch (ageChartVO.type()) {
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

    private static void setAgeData(AgeChartVO ageChartVO, List<ChartVO.ChartData> y, List<StatConclusion> statConclusionList) {
        switch (ageChartVO.type()) {
            case 1:
                getSaprodontiaAgeData(y, statConclusionList);
                break;
            case 2:
                getHeightAndWeightAgeData(y, statConclusionList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureAgeData(y, statConclusionList);
                break;
            default:
                break;
        }
    }

    private static void setAgeChartVO(AgeChartVO ageChartVO, List<ChartVO.Chart> chart) {
        switch (ageChartVO.type()) {
            case 1:
                ageChartVO.setSaprodontiaAgeMonitorChart(Objects.isNull(chart)?null:chart.get(0));
                break;
            case 2:
                ageChartVO.setHeightAndWeightAgeMonitorChart(Objects.isNull(chart)?null:chart);
                break;
            case 3:
                ageChartVO.setBloodPressureAndSpinalCurvatureAgeMonitorChart(Objects.isNull(chart)?null:chart.get(0));
                break;
            default:
                break;
        }
    }

    private static void getSaprodontiaAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getSaprodontiaRatio());
        data.get(1).getData().add(num.getSaprodontiaLossRatio());
        data.get(2).getData().add(num.getSaprodontiaRepairRatio());
    }

    private static void getHeightAndWeightAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getOverweightRatio());
        data.get(1).getData().add(num.getObeseRatio());
        data.get(2).getData().add(num.getMalnourishedRatio());
        data.get(3).getData().add(num.getStuntingRatio());
    }

    private static void getBloodPressureAndSpinalCurvatureAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getHighBloodPressureRatio());
        data.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
    }

}
