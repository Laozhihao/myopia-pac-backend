package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSchoolScreeningMonitorVO;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 各个学校筛查情况
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class DistrictSchoolScreeningMonitorService {


    /**
     * 各学校筛查情况
     */
    public void getDistrictSchoolScreeningMonitorVO(List<StatConclusion> statConclusionList,Map<Integer,String> schoolMap, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO = new DistrictSchoolScreeningMonitorVO();
        //说明变量
        getSchoolScreeningMonitorVariableVO(statConclusionList,schoolMap,districtSchoolScreeningMonitorVO);
        //表格数据
        getSchoolScreeningMonitorTableList(statConclusionList,schoolMap,districtSchoolScreeningMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSchoolScreeningMonitorVO(districtSchoolScreeningMonitorVO);

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

        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum saprodontia = getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaNum,SchoolScreeningNum::getSaprodontiaRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum saprodontiaLossAndRepair =  getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getSaprodontiaLossAndRepairNum,SchoolScreeningNum::getSaprodontiaLossAndRepairRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum overweight = getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getOverweightNum,SchoolScreeningNum::getOverweightRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum obese = getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getObeseNum,SchoolScreeningNum::getObeseRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum stunting = getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getStuntingNum,SchoolScreeningNum::getStuntingRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum malnourished = getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getMalnourishedNum,SchoolScreeningNum::getMalnourishedRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum abnormalSpineCurvature =getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getAbnormalSpineCurvatureNum,SchoolScreeningNum::getAbnormalSpineCurvatureRatio);
        DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum highBloodPressure =getSchoolRatioExtremum(schoolScreeningNumMap,SchoolScreeningNum::getHighBloodPressureNum,SchoolScreeningNum::getHighBloodPressureRatio);

        DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO variableVO = new DistrictSchoolScreeningMonitorVO.SchoolScreeningMonitorVariableVO();
        variableVO.setSaprodontiaRatioExtremum(saprodontia);
        variableVO.setSaprodontiaLossAndRepairRatioExtremum(saprodontiaLossAndRepair);
        variableVO.setOverweightRatioExtremum(overweight);
        variableVO.setObeseRatioExtremum(obese);
        variableVO.setStuntingRatioExtremum(stunting);
        variableVO.setMalnourishedRatioExtremum(malnourished);
        variableVO.setAbnormalSpineCurvatureRatioExtremum(abnormalSpineCurvature);
        variableVO.setHighBloodPressureRatioExtremum(highBloodPressure);

        districtSchoolScreeningMonitorVO.setSchoolScreeningMonitorVariableVO(variableVO);

    }

    private DistrictSchoolScreeningMonitorVO.SchoolRatioExtremum getSchoolRatioExtremum(Map<String,SchoolScreeningNum> schoolScreeningNumMap, Function<SchoolScreeningNum,Integer> function, Function<SchoolScreeningNum,BigDecimal> mapper){
        TwoTuple<String, BigDecimal> maxTuple = getMaxMap(schoolScreeningNumMap, function,mapper);
        TwoTuple<String, BigDecimal> minTuple = getMinMap(schoolScreeningNumMap, function,mapper);
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
     * 获取map中Value最大值及对应的Key
     */
    private <T,K> TwoTuple<K,BigDecimal> getMaxMap(Map<K, T> map, Function<T,Integer> function , Function<T,BigDecimal> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    private <T,K>TwoTuple<K,BigDecimal> getMinMap(Map<K, T> map, Function<T,Integer> function,Function<T,BigDecimal> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries, Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }


    /**
     * 各学校筛查情况-说明变量
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

    @Data
    private static class SchoolScreeningNum{
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

            this.saprodontiaNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsSaprodontia)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                    .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

            this.overweightNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsOverweight)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.obeseNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsObesity)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.malnourishedNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsMalnutrition)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.stuntingNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsStunting)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

            this.abnormalSpineCurvatureNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsSpinalCurvature)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.highBloodPressureNum = (int)statConclusionList.stream()
                    .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

            return this;
        }

        /**
         * 不带%
         */
        public SchoolScreeningNum ratioNotSymbol(){

            if (Objects.nonNull(saprodontiaNum)){
                this.saprodontiaRatio = MathUtil.ratioNotSymbol(saprodontiaNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossAndRepairNum)){
                this.saprodontiaLossAndRepairRatio = MathUtil.ratioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            }

            if (Objects.nonNull(overweightNum)){
                this.overweightRatio = MathUtil.ratioNotSymbol(overweightNum,validScreeningNum);
            }
            if (Objects.nonNull(obeseNum)){
                this.obeseRatio = MathUtil.ratioNotSymbol(obeseNum,validScreeningNum);
            }
            if (Objects.nonNull(stuntingNum)){
                this.stuntingRatio = MathUtil.ratioNotSymbol(stuntingNum,validScreeningNum);
            }
            if (Objects.nonNull(malnourishedNum)){
                this.malnourishedRatio = MathUtil.ratioNotSymbol(malnourishedNum,validScreeningNum);
            }
            if (Objects.nonNull(abnormalSpineCurvatureNum)){
                this.abnormalSpineCurvatureRatio = MathUtil.ratioNotSymbol(abnormalSpineCurvatureNum,validScreeningNum);
            }
            if (Objects.nonNull(highBloodPressureNum)){
                this.highBloodPressureRatio = MathUtil.ratioNotSymbol(highBloodPressureNum,validScreeningNum);
            }

            return this;
        }
    }
}
