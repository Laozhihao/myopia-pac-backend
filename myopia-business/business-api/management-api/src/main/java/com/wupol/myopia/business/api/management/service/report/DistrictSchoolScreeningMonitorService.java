package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 各个学校筛查情况（按区域）
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class DistrictSchoolScreeningMonitorService {


    /**
     * 各学校筛查情况
     */
    public void getDistrictSchoolScreeningMonitorVO(List<StatConclusion> statConclusionList, Map<Integer, String> schoolMap, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        ScreeningNum.MAP.put(0, statConclusionList.size());
        DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO = new DistrictSchoolScreeningMonitorVO();
        //说明变量
        getSchoolScreeningMonitorVariableVO(statConclusionList, schoolMap, districtSchoolScreeningMonitorVO);
        //表格数据
        getSchoolScreeningMonitorTableList(statConclusionList, schoolMap, districtSchoolScreeningMonitorVO);
        //图表
        getSchoolScreeningMonitorChart(statConclusionList, schoolMap, districtSchoolScreeningMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSchoolScreeningMonitorVO(districtSchoolScreeningMonitorVO);

    }

    private void getSchoolScreeningMonitorChart(List<StatConclusion> statConclusionList, Map<Integer, String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        Map<String, ScreeningNum> schoolScreeningNumMap = Maps.newHashMap();
        schoolStatConclusionMap.forEach((schoolId, list) -> {
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningNum(schoolName, list, schoolScreeningNumMap);
        });
        List<ChartVO.SchoolRatioExtremumChart> chartList = Lists.newArrayList(
                new ChartVO.SchoolRatioExtremumChart(ReportConst.SAPRODONTIA, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.SAPRODONTIA_LOSS_AND_REPAIR, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.OVERWEIGHT, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.OBESE, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.MALNOURISHED, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.STUNTING, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.ABNORMAL_SPINE_CURVATURE, Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.HIGH_BLOOD_PRESSURE, Lists.newArrayList())
        );

        AtomicBoolean flag = new AtomicBoolean(true);
        schoolScreeningNumMap.forEach((schoolName, screeningNum) -> {
            if (flag.get()) {
                flag.set(false);
                setChartData(chartList, 0, schoolScreeningNumMap, ScreeningNum::getSaprodontiaNum, ScreeningNum::getSaprodontiaRatio);
                setChartData(chartList, 1, schoolScreeningNumMap, ScreeningNum::getSaprodontiaLossAndRepairNum, ScreeningNum::getSaprodontiaLossAndRepairRatio);
                setChartData(chartList, 2, schoolScreeningNumMap, ScreeningNum::getOverweightNum, ScreeningNum::getOverweightRatio);
                setChartData(chartList, 3, schoolScreeningNumMap, ScreeningNum::getObeseNum, ScreeningNum::getObeseRatio);
                setChartData(chartList, 4, schoolScreeningNumMap, ScreeningNum::getMalnourishedNum, ScreeningNum::getMalnourishedRatio);
                setChartData(chartList, 5, schoolScreeningNumMap, ScreeningNum::getStuntingNum, ScreeningNum::getStuntingRatio);
                setChartData(chartList, 6, schoolScreeningNumMap, ScreeningNum::getAbnormalSpineCurvatureNum, ScreeningNum::getAbnormalSpineCurvatureRatio);
                setChartData(chartList, 7, schoolScreeningNumMap, ScreeningNum::getHighBloodPressureNum, ScreeningNum::getHighBloodPressureRatio);
            }
            chartList.get(0).getData().add(screeningNum.getSaprodontiaRatio());
            chartList.get(1).getData().add(screeningNum.getSaprodontiaLossAndRepairRatio());
            chartList.get(2).getData().add(screeningNum.getOverweightRatio());
            chartList.get(3).getData().add(screeningNum.getObeseRatio());
            chartList.get(4).getData().add(screeningNum.getMalnourishedRatio());
            chartList.get(5).getData().add(screeningNum.getStuntingRatio());
            chartList.get(6).getData().add(screeningNum.getAbnormalSpineCurvatureRatio());
            chartList.get(7).getData().add(screeningNum.getHighBloodPressureRatio());
        });
        districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorChart(chartList);
    }

    private void setChartData(List<ChartVO.SchoolRatioExtremumChart> chartList, Integer index, Map<String, ScreeningNum> schoolScreeningNumMap, Function<ScreeningNum, Integer> function, Function<ScreeningNum, BigDecimal> mapper) {
        SchoolRatioExtremum schoolRatioExtremum = getSchoolRatioExtremum(schoolScreeningNumMap, function, mapper);
        chartList.get(index).setMaxSchoolName(schoolRatioExtremum.getMaxSchoolName());
        chartList.get(index).setMaxRatio(schoolRatioExtremum.getMaxRatio());
        chartList.get(index).setMinSchoolName(schoolRatioExtremum.getMinSchoolName());
        chartList.get(index).setMinRatio(schoolRatioExtremum.getMinRatio());
    }

    /**
     * 各学校筛查情况-说明变量
     */
    private void getSchoolScreeningMonitorVariableVO(List<StatConclusion> statConclusionList, Map<Integer, String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        Map<String, ScreeningNum> schoolScreeningNumMap = Maps.newHashMap();
        schoolStatConclusionMap.forEach((schoolId, list) -> {
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningNum(schoolName, list, schoolScreeningNumMap);
        });

        if (schoolScreeningNumMap.size() >= 2) {
            DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO variableVO = new DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO();
            variableVO.setSaprodontiaRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getSaprodontiaNum, ScreeningNum::getSaprodontiaRatio));
            variableVO.setSaprodontiaLossAndRepairRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getSaprodontiaLossAndRepairNum, ScreeningNum::getSaprodontiaLossAndRepairRatio));
            variableVO.setOverweightRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getOverweightNum, ScreeningNum::getOverweightRatio));
            variableVO.setObeseRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getObeseNum, ScreeningNum::getObeseRatio));
            variableVO.setStuntingRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getStuntingNum, ScreeningNum::getStuntingRatio));
            variableVO.setMalnourishedRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getMalnourishedNum, ScreeningNum::getMalnourishedRatio));
            variableVO.setAbnormalSpineCurvatureRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getAbnormalSpineCurvatureNum, ScreeningNum::getAbnormalSpineCurvatureRatio));
            variableVO.setHighBloodPressureRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap, ScreeningNum::getHighBloodPressureNum, ScreeningNum::getHighBloodPressureRatio));
            districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorVariableVO(variableVO);
        }
    }

    private SchoolRatioExtremum getSchoolRatioExtremum(Map<String, ScreeningNum> schoolScreeningNumMap, Function<ScreeningNum, Integer> function, Function<ScreeningNum, BigDecimal> mapper) {
        TwoTuple<String, BigDecimal> maxTuple = ReportUtil.getMaxMap(schoolScreeningNumMap, function, mapper);
        TwoTuple<String, BigDecimal> minTuple = ReportUtil.getMinMap(schoolScreeningNumMap, function, mapper);
        SchoolRatioExtremum schoolRatioExtremum = new SchoolRatioExtremum();
        schoolRatioExtremum.setMaxSchoolName(maxTuple.getFirst());
        schoolRatioExtremum.setMinSchoolName(minTuple.getFirst());
        schoolRatioExtremum.setMaxRatio(maxTuple.getSecond());
        schoolRatioExtremum.setMinRatio(minTuple.getSecond());
        return schoolRatioExtremum;
    }

    private <K> void getSchoolScreeningNum(K key, List<StatConclusion> statConclusionList, Map<K, ScreeningNum> schoolScreeningNumMap) {
        ScreeningNum build = new ScreeningNum()
                .build(statConclusionList).ratioNotSymbol();
        schoolScreeningNumMap.put(key, build);
    }


    /**
     * 各学校筛查情况-表格数据
     */
    private void getSchoolScreeningMonitorTableList(List<StatConclusion> statConclusionList, Map<Integer, String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<ScreeningMonitorTable> tableList = Lists.newArrayList();
        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        schoolStatConclusionMap.forEach((schoolId, list) -> {
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningTable(schoolName, list, tableList);
        });

        districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorTableList(tableList);
    }

    private void getSchoolScreeningTable(String schoolName, List<StatConclusion> statConclusionList, List<ScreeningMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        ScreeningMonitorTable schoolScreeningMonitorTable = new ScreeningNum().build(statConclusionList).ratioNotSymbol().buildTable();
        schoolScreeningMonitorTable.setItemName(schoolName);
        tableList.add(schoolScreeningMonitorTable);
    }
}
