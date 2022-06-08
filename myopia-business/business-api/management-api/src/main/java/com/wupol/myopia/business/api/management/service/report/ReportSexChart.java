package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告图表-性别
 *
 * @author hang.yuan 2022/6/4 00:56
 */

public class ReportSexChart {

    /**
     * 不同性别-图表
     */
    public static void getSexMonitorChart(List<StatConclusion> statConclusionList, SexChartVO sexChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        ChartVO.Chart chart = new ChartVO.Chart();
        List<String> x = Lists.newArrayList();
        getSexX(sexChartVO.type(),x);
        List<ChartVO.ChartData> y = Lists.newArrayList(
                new ChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new ChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );
        List<BigDecimal> valueList =Lists.newArrayList();
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        switch (sexChartVO.type()){
            case 1:
                getSaprodontiaChartData(genderMap.get(GenderEnum.MALE.type), y,0,valueList);
                getSaprodontiaChartData(genderMap.get(GenderEnum.FEMALE.type), y,1,valueList);
                break;
            case 2:
                getHeightAndWeightChartData(genderMap.get(GenderEnum.MALE.type), y,0,valueList);
                getHeightAndWeightChartData(genderMap.get(GenderEnum.FEMALE.type), y,1,valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureChartData(genderMap.get(GenderEnum.MALE.type), y,0,valueList);
                getBloodPressureAndSpinalCurvatureChartData(genderMap.get(GenderEnum.FEMALE.type), y,1,valueList);
                break;
            default:
                break;
        }

        chart.setX(x);
        chart.setY(y);
        chart.setMaxValue(CollectionUtil.max(valueList));
        setSexChartVO(sexChartVO,chart);

    }

    private static void getSexX(Integer type,List<String> x){
        switch (type) {
            case 1:
                x.addAll(Lists.newArrayList(ReportConst.SAPRODONTIA,ReportConst.SAPRODONTIA_LOSS,ReportConst.SAPRODONTIA_REPAIR));
                break;
            case 2:
                x.addAll(Lists.newArrayList(ReportConst.OVERWEIGHT,ReportConst.OBESE,ReportConst.MALNOURISHED,ReportConst.STUNTING));
                break;
            case 3:
                x.addAll(Lists.newArrayList(ReportConst.HIGH_BLOOD_PRESSURE,ReportConst.ABNORMAL_SPINE_CURVATURE));
                break;
            default:
                break;
        }
    }

    private static void setSexChartVO(SexChartVO sexChartVO,ChartVO.Chart chart){
        switch (sexChartVO.type()){
            case 1:
                sexChartVO.setSaprodontiaSexMonitorChart(chart);
                break;
            case 2:
                sexChartVO.setHeightAndWeightSexMonitorChart(chart);
                break;
            case 3:
                sexChartVO.setBloodPressureAndSpinalCurvatureSexMonitorChart(chart);
                break;
            default:
                break;
        }
    }


    private static void getSaprodontiaChartData(List<StatConclusion> statConclusionList,List<ChartVO.ChartData> y,Integer index,List<BigDecimal> valueList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getSaprodontiaRatio());
        y.get(index).getData().add(num.getSaprodontiaLossRatio());
        y.get(index).getData().add(num.getSaprodontiaRepairRatio());

        valueList.add(num.getSaprodontiaRatio());
        valueList.add(num.getSaprodontiaLossRatio());
        valueList.add(num.getSaprodontiaRepairRatio());
    }

    private static void getHeightAndWeightChartData(List<StatConclusion> statConclusionList, List<ChartVO.ChartData> y, Integer index,List<BigDecimal> valueList) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getOverweightRatio());
        y.get(index).getData().add(num.getObeseRatio());
        y.get(index).getData().add(num.getMalnourishedRatio());
        y.get(index).getData().add(num.getStuntingRatio());

        valueList.add(num.getOverweightRatio());
        valueList.add(num.getObeseRatio());
        valueList.add(num.getMalnourishedRatio());
        valueList.add(num.getStuntingRatio());
    }

    private static void getBloodPressureAndSpinalCurvatureChartData(List<StatConclusion> statConclusionList,List<ChartVO.ChartData> y,Integer index,List<BigDecimal> valueList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getHighBloodPressureRatio());
        y.get(index).getData().add(num.getAbnormalSpineCurvatureRatio());
        valueList.add(num.getHighBloodPressureRatio());
        valueList.add(num.getAbnormalSpineCurvatureRatio());
    }



}
