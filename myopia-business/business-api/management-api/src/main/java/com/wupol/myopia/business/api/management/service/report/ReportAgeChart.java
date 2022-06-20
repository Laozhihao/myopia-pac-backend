package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

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
        }
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);

        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList();
        getAgeY(ageChartVO, y);

        List<BigDecimal> valueList = Lists.newArrayList();

        dynamicAgeSegmentList.forEach(age -> {
            List<StatConclusion> statConclusions = ageMap.get(age);
            if (Objects.nonNull(statConclusions)) {
                x.add(AgeSegmentEnum.get(age).getDescEn());
                setAgeData(ageChartVO, y, statConclusions, valueList);
            }
        });

        if (Objects.equals(ageChartVO.type(),1) || Objects.equals(ageChartVO.type(),3)){
            ChartVO.Chart chart = new ChartVO.Chart();
            chart.setY(y);
            chart.setX(x);
            chart.setMaxValue(CollectionUtil.max(valueList));
            setAgeChartVO(ageChartVO, Lists.newArrayList(chart));
        }

        if (Objects.equals(ageChartVO.type(),2)){
            List<ChartVO.Chart> chartList =Lists.newArrayList();
            List<List<ChartVO.ChartData>> lists = CollectionUtil.splitList(y, 2);
            List<List<BigDecimal>> vList = CollectionUtil.splitList(valueList, valueList.size()/2);
            for (int i = 0; i < lists.size(); i++) {
                ChartVO.Chart chart = new ChartVO.Chart();
                chart.setY(lists.get(i));
                chart.setX(x);
                chart.setMaxValue(CollectionUtil.max(vList.get(i)));
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

    private static void setAgeData(AgeChartVO ageChartVO, List<ChartVO.ChartData> y, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        switch (ageChartVO.type()) {
            case 1:
                getSaprodontiaAgeData(y, statConclusionList, valueList);
                break;
            case 2:
                getHeightAndWeightAgeData(y, statConclusionList, valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureAgeData(y, statConclusionList, valueList);
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

    private static void getSaprodontiaAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getSaprodontiaRatio());
        data.get(1).getData().add(num.getSaprodontiaLossRatio());
        data.get(2).getData().add(num.getSaprodontiaRepairRatio());
        valueList.add(num.getSaprodontiaRatio());
        valueList.add(num.getSaprodontiaLossRatio());
        valueList.add(num.getSaprodontiaRepairRatio());
    }

    private static void getHeightAndWeightAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
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

    private static void getBloodPressureAndSpinalCurvatureAgeData(List<ChartVO.ChartData> data, List<StatConclusion> statConclusionList, List<BigDecimal> valueList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getHighBloodPressureRatio());
        data.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
        valueList.add(num.getHighBloodPressureRatio());
        valueList.add(num.getAbnormalSpineCurvatureRatio());
    }

}
