package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.ChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSchoolScreeningMonitorVO;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 各个学校筛查情况
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class DistrictSchoolScreeningMonitorService {

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();

    /**
     * 各学校筛查情况
     */
    public void getDistrictSchoolScreeningMonitorVO(List<StatConclusion> statConclusionList,Map<Integer,String> schoolMap, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        map.put(0,statConclusionList.size());
        DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO = new DistrictSchoolScreeningMonitorVO();
        //说明变量
        getSchoolScreeningMonitorVariableVO(statConclusionList,schoolMap,districtSchoolScreeningMonitorVO);
        //表格数据
        getSchoolScreeningMonitorTableList(statConclusionList,schoolMap,districtSchoolScreeningMonitorVO);
        //图表
        getSchoolScreeningMonitorChart(statConclusionList,schoolMap,districtSchoolScreeningMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSchoolScreeningMonitorVO(districtSchoolScreeningMonitorVO);

    }

    private void getSchoolScreeningMonitorChart(List<StatConclusion> statConclusionList, Map<Integer, String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        Map<String,SchoolScreeningNum> schoolScreeningNumMap = Maps.newHashMap();
        schoolStatConclusionMap.forEach((schoolId,list)->{
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningNum(schoolName,list,schoolScreeningNumMap);
        });
        List<ChartVO.SchoolRatioExtremumChart> chartList =Lists.newArrayList(
                new ChartVO.SchoolRatioExtremumChart(ReportConst.SAPRODONTIA,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.SAPRODONTIA_LOSS_AND_REPAIR,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.OBESE,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.STUNTING,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.ABNORMAL_SPINE_CURVATURE,Lists.newArrayList()),
                new ChartVO.SchoolRatioExtremumChart(ReportConst.HIGH_BLOOD_PRESSURE,Lists.newArrayList())
                );

        AtomicBoolean flag = new AtomicBoolean(true);
        schoolScreeningNumMap.forEach((schoolName,schoolScreeningNum)->{
            if (flag.get()){
                flag.set(false);
                setChartData(chartList,0,schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaNum,SchoolScreeningNum::getSaprodontiaRatio);
                setChartData(chartList,1,schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaLossAndRepairNum,SchoolScreeningNum::getSaprodontiaLossAndRepairRatio);
                setChartData(chartList,2,schoolScreeningNumMap,SchoolScreeningNum::getOverweightNum,SchoolScreeningNum::getOverweightRatio);
                setChartData(chartList,3,schoolScreeningNumMap,SchoolScreeningNum::getObeseNum,SchoolScreeningNum::getObeseRatio);
                setChartData(chartList,4,schoolScreeningNumMap,SchoolScreeningNum::getMalnourishedNum,SchoolScreeningNum::getMalnourishedRatio);
                setChartData(chartList,5,schoolScreeningNumMap,SchoolScreeningNum::getStuntingNum,SchoolScreeningNum::getStuntingRatio);
                setChartData(chartList,6,schoolScreeningNumMap,SchoolScreeningNum::getAbnormalSpineCurvatureNum,SchoolScreeningNum::getAbnormalSpineCurvatureRatio);
                setChartData(chartList,7,schoolScreeningNumMap,SchoolScreeningNum::getHighBloodPressureNum,SchoolScreeningNum::getHighBloodPressureRatio);
            }
            chartList.get(0).getData().add(schoolScreeningNum.saprodontiaRatio);
            chartList.get(1).getData().add(schoolScreeningNum.saprodontiaLossAndRepairRatio);
            chartList.get(2).getData().add(schoolScreeningNum.overweightRatio);
            chartList.get(3).getData().add(schoolScreeningNum.obeseRatio);
            chartList.get(4).getData().add(schoolScreeningNum.malnourishedRatio);
            chartList.get(5).getData().add(schoolScreeningNum.stuntingRatio);
            chartList.get(6).getData().add(schoolScreeningNum.abnormalSpineCurvatureRatio);
            chartList.get(7).getData().add(schoolScreeningNum.highBloodPressureRatio);
        });
        districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorChart(chartList);
    }

    private void setChartData(List<ChartVO.SchoolRatioExtremumChart> chartList, Integer index, Map<String,SchoolScreeningNum> schoolScreeningNumMap, Function<SchoolScreeningNum,Integer> function, Function<SchoolScreeningNum,BigDecimal> mapper){
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum schoolRatioExtremum = getSchoolRatioExtremum(schoolScreeningNumMap, function, mapper);
        chartList.get(index).setMaxSchoolName(schoolRatioExtremum.getMaxSchoolName());
        chartList.get(index).setMaxRatio(schoolRatioExtremum.getMaxRatio());
        chartList.get(index).setMinSchoolName(schoolRatioExtremum.getMinSchoolName());
        chartList.get(index).setMinRatio(schoolRatioExtremum.getMinRatio());
    }

    /**
     * 各学校筛查情况-说明变量
     */
    private void getSchoolScreeningMonitorVariableVO(List<StatConclusion> statConclusionList,Map<Integer,String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        Map<String,SchoolScreeningNum> schoolScreeningNumMap = Maps.newHashMap();
        schoolStatConclusionMap.forEach((schoolId,list)->{
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningNum(schoolName,list,schoolScreeningNumMap);
        });

        if (schoolScreeningNumMap.size() >= 2){
            DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO variableVO = new DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO();
            variableVO.setSaprodontiaRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaNum,SchoolScreeningNum::getSaprodontiaRatio));
            variableVO.setSaprodontiaLossAndRepairRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaLossAndRepairNum,SchoolScreeningNum::getSaprodontiaLossAndRepairRatio));
            variableVO.setOverweightRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getOverweightNum,SchoolScreeningNum::getOverweightRatio));
            variableVO.setObeseRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getObeseNum,SchoolScreeningNum::getObeseRatio));
            variableVO.setStuntingRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getStuntingNum,SchoolScreeningNum::getStuntingRatio));
            variableVO.setMalnourishedRatioExtremum( getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getMalnourishedNum,SchoolScreeningNum::getMalnourishedRatio));
            variableVO.setAbnormalSpineCurvatureRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getAbnormalSpineCurvatureNum,SchoolScreeningNum::getAbnormalSpineCurvatureRatio));
            variableVO.setHighBloodPressureRatioExtremum(getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getHighBloodPressureNum,SchoolScreeningNum::getHighBloodPressureRatio));
            districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorVariableVO(variableVO);
        }
    }

    private DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum getSchoolRatioExtremum(Map<String,SchoolScreeningNum> schoolScreeningNumMap, Function<SchoolScreeningNum,Integer> function, Function<SchoolScreeningNum,BigDecimal> mapper){
        TwoTuple<String, BigDecimal> maxTuple = ReportUtil.getMaxMap(schoolScreeningNumMap, function,mapper);
        TwoTuple<String, BigDecimal> minTuple = ReportUtil.getMinMap(schoolScreeningNumMap, function,mapper);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum schoolRatioExtremum = new DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum();
        schoolRatioExtremum.setMaxSchoolName(maxTuple.getFirst());
        schoolRatioExtremum.setMinSchoolName(minTuple.getFirst());
        schoolRatioExtremum.setMaxRatio(maxTuple.getSecond());
        schoolRatioExtremum.setMinRatio(minTuple.getSecond());
        return schoolRatioExtremum;
    }
    private <K>void getSchoolScreeningNum(K key, List<StatConclusion> statConclusionList,Map<K, SchoolScreeningNum> schoolScreeningNumMap){
        SchoolScreeningNum build = new SchoolScreeningNum()
                .build(statConclusionList).ratioNotSymbol();
        schoolScreeningNumMap.put(key,build);
    }


    /**
     * 各学校筛查情况-表格数据
     */
    private void getSchoolScreeningMonitorTableList(List<StatConclusion> statConclusionList,Map<Integer,String> schoolMap, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable> tableList = Lists.newArrayList();
        Map<Integer, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        schoolStatConclusionMap.forEach((schoolId,list)->{
            String schoolName = schoolMap.get(schoolId);
            getSchoolScreeningTable(schoolName,list,tableList);
        });

        districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorTableList(tableList);
    }

    private void getSchoolScreeningTable(String schoolName, List<StatConclusion> statConclusionList, List<DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolScreeningNum schoolScreeningNum = new SchoolScreeningNum().build(statConclusionList).ratioNotSymbol();
        DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable schoolScreeningMonitorTable = buildTable(schoolScreeningNum);
        schoolScreeningMonitorTable.setItemName(schoolName);
        tableList.add(schoolScreeningMonitorTable);
    }

    public DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable buildTable(SchoolScreeningNum schoolScreeningNum){
        DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable schoolScreeningMonitorTable = new DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorTable();
        schoolScreeningMonitorTable.setValidScreeningNum(schoolScreeningNum.validScreeningNum);
        schoolScreeningMonitorTable.setSaprodontiaNum(schoolScreeningNum.saprodontiaNum);
        schoolScreeningMonitorTable.setSaprodontiaRatio(schoolScreeningNum.saprodontiaRatio);
        schoolScreeningMonitorTable.setSaprodontiaLossAndRepairNum(schoolScreeningNum.saprodontiaLossAndRepairNum);
        schoolScreeningMonitorTable.setSaprodontiaLossAndRepairRatio(schoolScreeningNum.saprodontiaLossAndRepairRatio);
        schoolScreeningMonitorTable.setOverweightNum(schoolScreeningNum.overweightNum);
        schoolScreeningMonitorTable.setOverweightRatio(schoolScreeningNum.overweightRatio);
        schoolScreeningMonitorTable.setObeseNum(schoolScreeningNum.obeseNum);
        schoolScreeningMonitorTable.setObeseRatio(schoolScreeningNum.obeseRatio);
        schoolScreeningMonitorTable.setStuntingNum(schoolScreeningNum.stuntingNum);
        schoolScreeningMonitorTable.setStuntingRatio(schoolScreeningNum.stuntingRatio);
        schoolScreeningMonitorTable.setMalnourishedNum(schoolScreeningNum.malnourishedNum);
        schoolScreeningMonitorTable.setMalnourishedRatio(schoolScreeningNum.malnourishedRatio);
        schoolScreeningMonitorTable.setAbnormalSpineCurvatureNum(schoolScreeningNum.abnormalSpineCurvatureNum);
        schoolScreeningMonitorTable.setAbnormalSpineCurvatureRatio(schoolScreeningNum.abnormalSpineCurvatureRatio);
        schoolScreeningMonitorTable.setHighBloodPressureNum(schoolScreeningNum.highBloodPressureNum);
        schoolScreeningMonitorTable.setHighBloodPressureRatio(schoolScreeningNum.highBloodPressureRatio);
        return schoolScreeningMonitorTable;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class SchoolScreeningNum extends EntityFunction{
        /**
         * 筛查人数
         */
        private Integer validScreeningNum;

        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 超重数
         */
        private Integer overweightNum;
        /**
         * 肥胖数
         */
        private Integer obeseNum;
        /**
         * 营养不良数
         */
        private Integer malnourishedNum;
        /**
         * 生长迟缓数据
         */
        private Integer stuntingNum;

        /**
         * 血压偏高人数
         */
        private Integer highBloodPressureNum;

        /**
         * 脊柱弯曲异常人数
         */
        private Integer abnormalSpineCurvatureNum;


        //=========== 不带% =============
        /**
         * 龋患率
         */
        private BigDecimal saprodontiaRatio;

        /**
         * 龋患（失、补）率
         */
        private BigDecimal saprodontiaLossAndRepairRatio;
        /**
         * 超重率
         */
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        private BigDecimal obeseRatio;

        /**
         * 营养不良率
         */
        private BigDecimal malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private BigDecimal stuntingRatio;

        /**
         * 血压偏高率
         */
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        private BigDecimal abnormalSpineCurvatureRatio;

        public SchoolScreeningNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();
            this.saprodontiaNum = getCount(statConclusionList,StatConclusion::getIsSaprodontia);
            this.saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                    .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();
            this.overweightNum = getCount(statConclusionList,StatConclusion::getIsOverweight);
            this.obeseNum = getCount(statConclusionList,StatConclusion::getIsObesity);
            this.malnourishedNum =getCount(statConclusionList,StatConclusion::getIsMalnutrition);
            this.stuntingNum =getCount(statConclusionList,StatConclusion::getIsStunting);
            this.abnormalSpineCurvatureNum =getCount(statConclusionList,StatConclusion::getIsSpinalCurvature);
            this.highBloodPressureNum = (int)statConclusionList.stream()
                    .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

            return this;
        }

        /**
         * 不带%
         */
        public SchoolScreeningNum ratioNotSymbol(){
            this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum,getTotal());
            this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum,getTotal());
            this.overweightRatio = getRatioNotSymbol(overweightNum,getTotal());
            this.obeseRatio = getRatioNotSymbol(obeseNum,getTotal());
            this.stuntingRatio = getRatioNotSymbol(stuntingNum,getTotal());
            this.malnourishedRatio = getRatioNotSymbol(malnourishedNum,getTotal());
            this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum,getTotal());
            this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum,getTotal());
            return this;
        }
    }

    private static Integer getTotal(){
        return map.get(0);
    }
}
