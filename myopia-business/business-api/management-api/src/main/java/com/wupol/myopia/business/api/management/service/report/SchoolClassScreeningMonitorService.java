package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 各个班级筛查情况（按学校）
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class SchoolClassScreeningMonitorService {


    /**
     * 各班级筛查情况
     */
    public void getSchoolClassScreeningMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        ScreeningNum.MAP.put(0, statConclusionList.size());

        Map<String, List<StatConclusion>> gradeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeMap = MapUtil.sort(gradeMap);
        List<SchoolClassScreeningMonitorVO> schoolClassScreeningMonitorVOList = Lists.newArrayList();

        boolean isShow = gradeMap.size() >= 2;
        gradeMap.forEach((gradeCode, list) -> {
            SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO = new SchoolClassScreeningMonitorVO();
            //表格数据
            getSchoolClassScreeningMonitorTableList(list, schoolClassScreeningMonitorVO);
            if (Objects.equals(schoolClassScreeningMonitorVO.notEmpty(),Boolean.TRUE)) {
                schoolClassScreeningMonitorVOList.add(schoolClassScreeningMonitorVO);
                schoolClassScreeningMonitorVO.setGrade(GradeCodeEnum.getByCode(gradeCode).getName());
            }
            if (isShow){
                //图表数据
                getSchoolClassScreeningMonitorChart(list, schoolClassScreeningMonitorVO);
            }

        });

        schoolCommonDiseasesAnalysisVO.setSchoolClassScreeningMonitorVOList(schoolClassScreeningMonitorVOList);
    }

    private void getSchoolClassScreeningMonitorChart(List<StatConclusion> statConclusionList, SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        ScreeningNum gradeScreeningNum = new ScreeningNum().build(statConclusionList).ratioNotSymbol();

        Map<String, List<StatConclusion>> classStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
        if (classStatConclusionMap.size() <= 1){
            return;
        }
        Map<String, ScreeningNum> classScreeningNumMap = Maps.newHashMap();
        classStatConclusionMap.forEach((schoolClassName, list) -> getSchoolClassScreeningNum(schoolClassName, list, classScreeningNumMap));

        List<ChartVO.GradeRatioExtremumChart> chartList = Lists.newArrayList(
                new ChartVO.GradeRatioExtremumChart(ReportConst.SAPRODONTIA, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.SAPRODONTIA_LOSS_AND_REPAIR, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.OVERWEIGHT, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.OBESE, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.MALNOURISHED, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.STUNTING, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.ABNORMAL_SPINE_CURVATURE, Lists.newArrayList()),
                new ChartVO.GradeRatioExtremumChart(ReportConst.HIGH_BLOOD_PRESSURE, Lists.newArrayList())
        );

        AtomicBoolean flag = new AtomicBoolean(true);
        classScreeningNumMap.forEach((schoolName, screeningNum) -> {
            if (flag.get()) {
                flag.set(false);
                setChartData(chartList, 0, classScreeningNumMap, ScreeningNum::getSaprodontiaNum, ScreeningNum::getSaprodontiaRatio, gradeScreeningNum);
                setChartData(chartList, 1, classScreeningNumMap, ScreeningNum::getSaprodontiaLossAndRepairNum, ScreeningNum::getSaprodontiaLossAndRepairRatio, gradeScreeningNum);
                setChartData(chartList, 2, classScreeningNumMap, ScreeningNum::getOverweightNum, ScreeningNum::getOverweightRatio, gradeScreeningNum);
                setChartData(chartList, 3, classScreeningNumMap, ScreeningNum::getObeseNum, ScreeningNum::getObeseRatio, gradeScreeningNum);
                setChartData(chartList, 4, classScreeningNumMap, ScreeningNum::getMalnourishedNum, ScreeningNum::getMalnourishedRatio, gradeScreeningNum);
                setChartData(chartList, 5, classScreeningNumMap, ScreeningNum::getStuntingNum, ScreeningNum::getStuntingRatio, gradeScreeningNum);
                setChartData(chartList, 6, classScreeningNumMap, ScreeningNum::getAbnormalSpineCurvatureNum, ScreeningNum::getAbnormalSpineCurvatureRatio, gradeScreeningNum);
                setChartData(chartList, 7, classScreeningNumMap, ScreeningNum::getHighBloodPressureNum, ScreeningNum::getHighBloodPressureRatio, gradeScreeningNum);
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

        schoolClassScreeningMonitorVO.setSchoolClassScreeningMonitorChart(chartList);
    }

    private void setChartData(List<ChartVO.GradeRatioExtremumChart> chartList, Integer index, Map<String, ScreeningNum> classScreeningNumMap,
                              Function<ScreeningNum, Integer> function, Function<ScreeningNum, BigDecimal> mapper, ScreeningNum num) {
        GradeRatioExtremum gradeRatioExtremum = getClassRatioExtremum(classScreeningNumMap, function, mapper);
        chartList.get(index).setMaxClassName(gradeRatioExtremum.getMaxClassName());
        chartList.get(index).setMaxRatio(gradeRatioExtremum.getMaxRatio());
        chartList.get(index).setMinClassName(gradeRatioExtremum.getMinClassName());
        chartList.get(index).setMinRatio(gradeRatioExtremum.getMinRatio());
        chartList.get(index).setRatio(mapper.apply(num));
    }


    private GradeRatioExtremum getClassRatioExtremum(Map<String, ScreeningNum> classScreeningNumMap, Function<ScreeningNum, Integer> function, Function<ScreeningNum, BigDecimal> mapper) {
        TwoTuple<String, BigDecimal> maxTuple = ReportUtil.getMaxMap(classScreeningNumMap, function, mapper);
        TwoTuple<String, BigDecimal> minTuple = ReportUtil.getMinMap(classScreeningNumMap, function, mapper);
        GradeRatioExtremum gradeRatioExtremum = new GradeRatioExtremum();
        gradeRatioExtremum.setMaxClassName(maxTuple.getFirst());
        gradeRatioExtremum.setMinClassName(minTuple.getFirst());
        gradeRatioExtremum.setMaxRatio(maxTuple.getSecond());
        gradeRatioExtremum.setMinRatio(minTuple.getSecond());
        return gradeRatioExtremum;
    }

    private <K> void getSchoolClassScreeningNum(K key, List<StatConclusion> statConclusionList, Map<K, ScreeningNum> classScreeningNumMap) {
        ScreeningNum build = new ScreeningNum()
                .build(statConclusionList).ratioNotSymbol();
        classScreeningNumMap.put(key, build);
    }


    /**
     * 各班级筛查情况-表格数据
     */
    private void getSchoolClassScreeningMonitorTableList(List<StatConclusion> statConclusionList, SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<ScreeningMonitorTable> tableList = Lists.newArrayList();
        Map<String, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
        schoolStatConclusionMap.forEach((schoolClassName, list) -> getSchoolClassScreeningTable(schoolClassName, list, tableList));

        CollUtil.sort(tableList, Comparator.comparing(ScreeningMonitorTable::getSaprodontiaRatio).reversed());

        schoolClassScreeningMonitorVO.setSchoolClassScreeningMonitorTableList(tableList);
    }

    private void getSchoolClassScreeningTable(String schoolClassName, List<StatConclusion> statConclusionList, List<ScreeningMonitorTable> tableList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        ScreeningMonitorTable schoolScreeningMonitorTable = new ScreeningNum().build(statConclusionList).ratioNotSymbol().buildTable();
        schoolScreeningMonitorTable.setItemName(schoolClassName);
        tableList.add(schoolScreeningMonitorTable);
    }
}
