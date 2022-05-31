package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolClassScreeningMonitorVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
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
public class SchoolClassScreeningMonitorService {


    /**
     * 各班级筛查情况
     */
    public void getSchoolClassScreeningMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO = new SchoolClassScreeningMonitorVO();
        //说明变量
        getSchoolClassScreeningMonitorVariableVO(statConclusionList,schoolClassScreeningMonitorVO);
        //表格数据
        getSchoolClassScreeningMonitorTableList(statConclusionList,schoolClassScreeningMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolClassScreeningMonitorVO(schoolClassScreeningMonitorVO);

    }

    /**
     * 各班级筛查情况-说明变量
     */
    private void getSchoolClassScreeningMonitorVariableVO(List<StatConclusion> statConclusionList,SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String,SchoolClassScreeningNum> schoolScreeningNumMap = Maps.newHashMap();
        schoolStatConclusionMap.forEach((schoolGradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
            getSchoolClassScreeningNum(gradeCodeEnum.getName(),list,schoolScreeningNumMap);
        });

        if (schoolScreeningNumMap.size() >= 2){
            SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorVariableVO variableVO = new SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorVariableVO();
            variableVO.setSaprodontiaRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getSaprodontiaNum, SchoolClassScreeningNum::getSaprodontiaRatio));
            variableVO.setSaprodontiaLossAndRepairRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getSaprodontiaLossAndRepairNum, SchoolClassScreeningNum::getSaprodontiaLossAndRepairRatio));
            variableVO.setOverweightRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getOverweightNum, SchoolClassScreeningNum::getOverweightRatio));
            variableVO.setObeseRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getObeseNum, SchoolClassScreeningNum::getObeseRatio));
            variableVO.setStuntingRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getStuntingNum, SchoolClassScreeningNum::getStuntingRatio));
            variableVO.setMalnourishedRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getMalnourishedNum, SchoolClassScreeningNum::getMalnourishedRatio));
            variableVO.setAbnormalSpineCurvatureRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getAbnormalSpineCurvatureNum, SchoolClassScreeningNum::getAbnormalSpineCurvatureRatio));
            variableVO.setHighBloodPressureRatioExtremum(getGradeRatioExtremum(schoolScreeningNumMap, SchoolClassScreeningNum::getHighBloodPressureNum, SchoolClassScreeningNum::getHighBloodPressureRatio));
            schoolClassScreeningMonitorVO.setSchoolClassScreeningMonitorVariableVO(variableVO);
        }
    }

    private SchoolClassScreeningMonitorVO.GradeRatioExtremum getGradeRatioExtremum(Map<String,SchoolClassScreeningNum> schoolScreeningNumMap, Function<SchoolClassScreeningNum,Integer> function, Function<SchoolClassScreeningNum,BigDecimal> mapper){
        TwoTuple<String, BigDecimal> maxTuple = getMaxMap(schoolScreeningNumMap, function,mapper);
        TwoTuple<String, BigDecimal> minTuple = getMinMap(schoolScreeningNumMap, function,mapper);
        SchoolClassScreeningMonitorVO.GradeRatioExtremum schoolRatioExtremum = new SchoolClassScreeningMonitorVO.GradeRatioExtremum();
        schoolRatioExtremum.setMaxGradeName(maxTuple.getFirst());
        schoolRatioExtremum.setMinGradeName(minTuple.getFirst());
        schoolRatioExtremum.setMaxRatio(maxTuple.getSecond());
        schoolRatioExtremum.setMinRatio(minTuple.getSecond());
        return schoolRatioExtremum;
    }
    private <K>void getSchoolClassScreeningNum(K key, List<StatConclusion> statConclusionList,Map<K, SchoolClassScreeningNum> schoolScreeningNumMap){
        SchoolClassScreeningNum build = new SchoolClassScreeningNum()
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
     * 各班级筛查情况-表格数据
     */
    private void getSchoolClassScreeningMonitorTableList(List<StatConclusion> statConclusionList, SchoolClassScreeningMonitorVO schoolClassScreeningMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable> tableList = Lists.newArrayList();
        Map<String, List<StatConclusion>> schoolStatConclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        schoolStatConclusionMap.forEach((schoolGradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
            getSchoolClassScreeningTable(gradeCodeEnum.getName(),list,tableList);
        });

        schoolClassScreeningMonitorVO.setSchoolClassScreeningMonitorTableList(tableList);
    }

    private void getSchoolClassScreeningTable(String schoolName, List<StatConclusion> statConclusionList, List<SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolClassScreeningNum schoolScreeningNum = new SchoolClassScreeningNum().build(statConclusionList).ratioNotSymbol();
        SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable schoolScreeningMonitorTable = buildTable(schoolScreeningNum);
        schoolScreeningMonitorTable.setItemName(schoolName);
        tableList.add(schoolScreeningMonitorTable);
    }

    public SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable buildTable(SchoolClassScreeningNum schoolScreeningNum){
        SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable schoolScreeningMonitorTable = new SchoolClassScreeningMonitorVO.SchoolClassScreeningMonitorTable();
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
    private static class SchoolClassScreeningNum extends EntityFunction{
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

        public SchoolClassScreeningNum build(List<StatConclusion> statConclusionList){
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
        public SchoolClassScreeningNum ratioNotSymbol(){
            this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum,validScreeningNum);
            this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            this.overweightRatio = getRatioNotSymbol(overweightNum,validScreeningNum);
            this.obeseRatio = getRatioNotSymbol(obeseNum,validScreeningNum);
            this.stuntingRatio = getRatioNotSymbol(stuntingNum,validScreeningNum);
            this.malnourishedRatio = getRatioNotSymbol(malnourishedNum,validScreeningNum);
            this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum,validScreeningNum);
            this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum,validScreeningNum);
            return this;
        }
    }
}
