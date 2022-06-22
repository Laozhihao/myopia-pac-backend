package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 报告图表-班级
 *
 * @author hang.yuan 2022/6/4 00:56
 */
@UtilityClass
public class ReportGradeChart {


    /**
     * 不同班级
     */
    public static void getGradeMonitorChart(List<StatConclusion> statConclusionList, GradeChartVO gradeChartVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        if (gradeCodeMap.size() <= 1){
            setGradeChartVO(gradeChartVO,null);
            return;
        }

        ChartVO.ReverseChart chart = new ChartVO.ReverseChart();
        List<String> y = Lists.newArrayList();
        getY(gradeChartVO.type(),y);

        List<ChartVO.ChartData> x = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();

        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        gradeCodeMap.forEach((gradeCode, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            x.add(new ChartVO.ChartData(gradeCodeEnum.getName(),Lists.newArrayList()));
        });

        Set<Map.Entry<String, List<StatConclusion>>> entrySet = gradeCodeMap.entrySet();
        int index = 0;
        for (Map.Entry<String, List<StatConclusion>> entry : entrySet) {
            setGradeData(gradeChartVO.type(), x, entry.getValue(),index, valueList);
            index++;
        }

        chart.setX(x);
        chart.setY(y);
        chart.setMaxValue(CollectionUtil.max(valueList));
        setGradeChartVO(gradeChartVO, chart);
    }

    private static void getY(Integer type, List<String> y) {
        switch (type) {
            case 1:
                y.addAll(Lists.newArrayList(ReportConst.SAPRODONTIA_REPAIR, ReportConst.SAPRODONTIA_LOSS,ReportConst.SAPRODONTIA));
                break;
            case 2:
                y.addAll(Lists.newArrayList(ReportConst.STUNTING,ReportConst.MALNOURISHED,ReportConst.OBESE,ReportConst.OVERWEIGHT ));
                break;
            case 3:
                y.addAll(Lists.newArrayList(ReportConst.ABNORMAL_SPINE_CURVATURE,ReportConst.HIGH_BLOOD_PRESSURE));
                break;
            default:
                break;
        }
    }

    private static void setGradeData(Integer type, List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList,Integer index, List<BigDecimal> valueList) {
        switch (type) {
            case 1:
                getSaprodontiaGradeData(x, statConclusionList,index ,valueList);
                break;
            case 2:
                getHeightAndWeightGradeData(x, statConclusionList,index, valueList);
                break;
            case 3:
                getBloodPressureAndSpinalCurvatureGradeData(x, statConclusionList,index, valueList);
                break;
            default:
                break;
        }

    }

    private static void getSaprodontiaGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList,Integer index, List<BigDecimal> valueList) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(index).getData().add(num.getSaprodontiaRepairRatio());
        x.get(index).getData().add(num.getSaprodontiaLossRatio());
        x.get(index).getData().add(num.getSaprodontiaRatio());
        valueList.add(num.getSaprodontiaRepairRatio());
        valueList.add(num.getSaprodontiaLossRatio());
        valueList.add(num.getSaprodontiaRatio());
    }

    private static void getHeightAndWeightGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList,Integer index, List<BigDecimal> valueList) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(index).getData().add(num.getStuntingRatio());
        x.get(index).getData().add(num.getMalnourishedRatio());
        x.get(index).getData().add(num.getObeseRatio());
        x.get(index).getData().add(num.getOverweightRatio());
        valueList.add(num.getStuntingRatio());
        valueList.add(num.getMalnourishedRatio());
        valueList.add(num.getObeseRatio());
        valueList.add(num.getOverweightRatio());
    }

    private static void getBloodPressureAndSpinalCurvatureGradeData(List<ChartVO.ChartData> x, List<StatConclusion> statConclusionList,Integer index, List<BigDecimal> valueList) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().ratio();
        x.get(index).getData().add(num.getAbnormalSpineCurvatureRatio());
        x.get(index).getData().add(num.getHighBloodPressureRatio());
        valueList.add(num.getAbnormalSpineCurvatureRatio());
        valueList.add(num.getHighBloodPressureRatio());
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
