package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告图表
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@Service
public class ReportChartService {

    /**
     * 监测结果-不同性别-图表
     */
    public void getSexMonitorChart(List<StatConclusion> statConclusionList, SexChartVO sexChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        ChartVO.Chart chart = new ChartVO.Chart();
        List<String> x = Lists.newArrayList();
        getSexX(sexChartVO,x);
        List<ChartVO.ChartData> y = Lists.newArrayList(
                new ChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new ChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );

        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        switch (sexChartVO.type()){
            case 1:
                getSaprodontiaChartData(genderMap.get(GenderEnum.MALE.type), y,0);
                getSaprodontiaChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);
                break;
            case 2:
                getHeightAndWeightChartData(genderMap.get(GenderEnum.MALE.type), y,0);
                getHeightAndWeightChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureChartData(genderMap.get(GenderEnum.MALE.type), y,0);
                getBloodPressureAndSpinalCurvatureChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);
                break;
            default:
                break;
        }

        chart.setX(x);
        chart.setY(y);
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

    private void getSaprodontiaChartData(List<StatConclusion> statConclusionList,List<ChartVO.ChartData> y,Integer index) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getSaprodontiaRatio());
        y.get(index).getData().add(num.getSaprodontiaLossRatio());
        y.get(index).getData().add(num.getSaprodontiaRepairRatio());
    }

    private void getHeightAndWeightChartData(List<StatConclusion> statConclusionList, List<ChartVO.ChartData> y, Integer index) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getOverweightRatio());
        y.get(index).getData().add(num.getObeseRatio());
        y.get(index).getData().add(num.getMalnourishedRatio());
        y.get(index).getData().add(num.getStuntingRatio());
    }

    private void getBloodPressureAndSpinalCurvatureChartData(List<StatConclusion> statConclusionList,List<ChartVO.ChartData> y,Integer index) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        y.get(index).getData().add(num.getHighBloodPressureRatio());
        y.get(index).getData().add(num.getAbnormalSpineCurvatureRatio());
    }


    /**
     *  监测结果-不同年龄段-图表
     */
    public void getAgeMonitorChart(List<StatConclusion> statConclusionList, AgeChartVO ageChartVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        ChartVO.AgeChart ageChart = new ChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);

        List<String> y = Lists.newArrayList();
        List<ChartVO.AgeData> x = Lists.newArrayList();
        getAgeX(ageChartVO,x);

        dynamicAgeSegmentList.forEach(age-> {
            y.add(AgeSegmentEnum.get(age).getDesc());
            setAgeData(ageChartVO,x,ageMap.get(age));
        });
        ageChart.setY(y);
        ageChart.setX(x);
        setAgeChartVO(ageChartVO,ageChart);
    }

    private void getSexX(SexChartVO sexChartVO,List<String> x){
        switch (sexChartVO.type()) {
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
    private void getAgeX(AgeChartVO ageChartVO,List<ChartVO.AgeData> x){

        switch (ageChartVO.type()){
            case 1:
                x.addAll(Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA_LOSS,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.SAPRODONTIA_REPAIR,Lists.newArrayList())
                ));
                break;
            case 2:
                x.addAll(Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.OBESE,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.STUNTING,Lists.newArrayList())
                ));
                break;
            case 3:
                x.addAll( Lists.newArrayList(
                        new ChartVO.AgeData(ReportConst.HIGH_BLOOD_PRESSURE,Lists.newArrayList()),
                        new ChartVO.AgeData(ReportConst.ABNORMAL_SPINE_CURVATURE,Lists.newArrayList())
                ));
                break;
            default:
                break;
        }
    }

    private void setAgeData(AgeChartVO ageChartVO,List<ChartVO.AgeData> x,List<StatConclusion> statConclusionList){
        switch (ageChartVO.type()){
            case 1:
                getSaprodontiaAgeData(x,statConclusionList);
                break;
            case 2:
                getHeightAndWeightAgeData(x,statConclusionList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureAgeData(x,statConclusionList);
                break;
            default:
                break;
        }
    }

    private void setAgeChartVO(AgeChartVO ageChartVO,ChartVO.AgeChart ageChart){
        switch (ageChartVO.type()){
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

    private void getSaprodontiaAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList){
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getSaprodontiaRatio());
        data.get(1).getData().add(num.getSaprodontiaLossRatio());
        data.get(2).getData().add(num.getSaprodontiaRepairRatio());
    }

    private void getHeightAndWeightAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList){
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getOverweightRatio());
        data.get(1).getData().add(num.getObeseRatio());
        data.get(2).getData().add(num.getMalnourishedRatio());
        data.get(3).getData().add(num.getStuntingRatio());
    }

    private void getBloodPressureAndSpinalCurvatureAgeData(List<ChartVO.AgeData> data, List<StatConclusion> statConclusionList){
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        data.get(0).getData().add(num.getHighBloodPressureRatio());
        data.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
    }


}
