package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictDiseaseMonitorVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseaseNumDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 疾病监测情况
 *
 * @author hang.yuan 2022/5/25 15:28
 */
@Service
public class DistrictDiseaseMonitorService {

    /**
     * 疾病监测情况
     */
    public void getDistrictDiseaseMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictDiseaseMonitorVO districtDiseaseMonitorVO = new DistrictDiseaseMonitorVO();

        getDiseaseMonitorVariableVO(statConclusionList,districtDiseaseMonitorVO);
        getDiseaseMonitorTableList(statConclusionList,districtDiseaseMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictDiseaseMonitorVO(districtDiseaseMonitorVO);

    }

    /**
     * 疾病监测情况-说明变量
     */
    private void getDiseaseMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictDiseaseMonitorVO districtDiseaseMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO diseaseMonitorVariableVO = new DistrictDiseaseMonitorVO.DiseaseMonitorVariableVO();

        DiseaseNum diseaseNum = new DiseaseNum().build(statConclusionList).ratioNotSymbol().ratio();

        diseaseMonitorVariableVO.setHypertensionRatio(diseaseNum.hypertensionRatioStr);
        diseaseMonitorVariableVO.setAnemiaRatio(diseaseNum.anemiaRatioStr);
        diseaseMonitorVariableVO.setDiabetesRatio(diseaseNum.diabetesRatioStr);
        diseaseMonitorVariableVO.setAllergicAsthmaRatio(diseaseNum.allergicAsthmaRatioStr);
        diseaseMonitorVariableVO.setPhysicalDisabilityRatio(diseaseNum.physicalDisabilityRatioStr);

        //最高值
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.nonNull(sc.getDiseaseNum())).collect(Collectors.toList());
        Map<Integer, List<StatConclusion>> schoolAgeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        Map<Integer, DiseaseNum> diseaseNumMap = Maps.newHashMap();
        schoolAgeMap.forEach((schoolAge,list)->getDiseaseNum(schoolAge,list,diseaseNumMap));

        if (diseaseNumMap.size() >= 2){
            diseaseMonitorVariableVO.setMaxHypertensionRatio(getSchoolAgeRatio(diseaseNumMap,DiseaseNum::getHypertension,DiseaseNum::getHypertensionRatioStr));
            diseaseMonitorVariableVO.setMaxAnemiaRatio(getSchoolAgeRatio(diseaseNumMap,DiseaseNum::getAnemia,DiseaseNum::getAnemiaRatioStr));
            diseaseMonitorVariableVO.setMaxDiabetesRatio(getSchoolAgeRatio(diseaseNumMap,DiseaseNum::getDiabetes,DiseaseNum::getDiabetesRatioStr));
            diseaseMonitorVariableVO.setMaxAllergicAsthmaRatio(getSchoolAgeRatio(diseaseNumMap,DiseaseNum::getAllergicAsthma,DiseaseNum::getAllergicAsthmaRatioStr));
            diseaseMonitorVariableVO.setMaxPhysicalDisabilityRatio(getSchoolAgeRatio(diseaseNumMap,DiseaseNum::getPhysicalDisability,DiseaseNum::getPhysicalDisabilityRatioStr));
        }

        districtDiseaseMonitorVO.setDiseaseMonitorVariableVO(diseaseMonitorVariableVO);
    }


    private <K>void getDiseaseNum(K key, List<StatConclusion> statConclusionList,Map<K, DiseaseNum> diseaseNumMap){
        DiseaseNum build = new DiseaseNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        diseaseNumMap.put(key,build);
    }

    private DistrictDiseaseMonitorVO.SchoolAgeRatio getSchoolAgeRatio(Map<Integer, DiseaseNum> diseaseNumMap,Function<DiseaseNum,Integer> function ,Function<DiseaseNum,String> mapper){
        DistrictDiseaseMonitorVO.SchoolAgeRatio schoolAgeRatio = new DistrictDiseaseMonitorVO.SchoolAgeRatio();
        TwoTuple<Integer, String> tuple = getMaxMap(diseaseNumMap, function, mapper);
        schoolAgeRatio.setSchoolAge(SchoolAge.get(tuple.getFirst()).desc);
        schoolAgeRatio.setRatio(tuple.getSecond());
        return schoolAgeRatio;
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,String> getMaxMap(Map<K, T> map, Function<T,Integer> function ,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    /**
     * 疾病监测情况-不同学龄段
     */
    private void getDiseaseMonitorTableList(List<StatConclusion> statConclusionList, DistrictDiseaseMonitorVO districtDiseaseMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        List<DistrictDiseaseMonitorVO.DiseaseMonitorTable> tableList = Lists.newArrayList();

        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        DistrictDiseaseMonitorVO.DiseaseMonitorTable primaryTable = getSchoolAgeTable(conclusionMap,SchoolAge.PRIMARY.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable juniorTable = getSchoolAgeTable(conclusionMap,SchoolAge.JUNIOR.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable highTable = getSchoolAgeTable(conclusionMap,10);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable normalHighTable = getSchoolAgeTable(conclusionMap,SchoolAge.HIGH.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable vocationalHighTable = getSchoolAgeTable(conclusionMap,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictDiseaseMonitorVO.DiseaseMonitorTable universityTable = getSchoolAgeTable(conclusionMap,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(primaryTable)){
            tableList.add(primaryTable);
        }
        if (Objects.nonNull(juniorTable)){
            tableList.add(juniorTable);
        }
        //高中（普高+职高）
        if (Objects.nonNull(vocationalHighTable)){
            if (Objects.nonNull(highTable)){
                tableList.add(highTable);
            }
            if (Objects.nonNull(normalHighTable)){
                tableList.add(normalHighTable);
            }
            tableList.add(vocationalHighTable);
        }else {
            if (Objects.nonNull(highTable)){
                tableList.add(highTable);
            }
        }

        if (Objects.nonNull(universityTable)){
            tableList.add(universityTable);
        }

        DistrictDiseaseMonitorVO.DiseaseMonitorTable totalTable = getDiseaseMonitorTable(statConclusionList,"合计");
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        districtDiseaseMonitorVO.setDiseaseMonitorTableList(tableList);
    }


    private DistrictDiseaseMonitorVO.DiseaseMonitorTable getSchoolAgeTable(Map<Integer, List<StatConclusion>> conclusionMap,Integer schoolAge){
        if (CollectionUtil.isEmpty(conclusionMap)){
            return null;
        }

        if (Objects.equals(schoolAge,10)){
            List<StatConclusion> mergeList=Lists.newArrayList();
            List<StatConclusion> highList = conclusionMap.get(SchoolAge.HIGH.code);
            if(CollectionUtil.isNotEmpty(highList)){
                mergeList.addAll(highList);
            }
            List<StatConclusion> vocationalHighList = conclusionMap.get(SchoolAge.VOCATIONAL_HIGH.code);
            if(CollectionUtil.isNotEmpty(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            if (CollectionUtil.isEmpty(mergeList)){
                return null;
            }
            return getDiseaseMonitorTable(mergeList, "高中");
        }

        List<StatConclusion> statConclusionList = conclusionMap.get(schoolAge);
        if(CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,SchoolAge.HIGH.code)){
            DistrictDiseaseMonitorVO.DiseaseMonitorTable normalHighTable = getDiseaseMonitorTable(statConclusionList, "普高");
            if (Objects.isNull(normalHighTable)){
                normalHighTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
                normalHighTable.setSchoolAge("普高");
            }
            return normalHighTable;
        }
        if (Objects.equals(schoolAge,SchoolAge.VOCATIONAL_HIGH.code)){
            return getDiseaseMonitorTable(statConclusionList, "职高");
        }

        return getDiseaseMonitorTable(statConclusionList, SchoolAge.get(schoolAge).desc);
    }

    private DistrictDiseaseMonitorVO.DiseaseMonitorTable getDiseaseMonitorTable(List<StatConclusion> statConclusionList,String schoolAgeDesc) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        DiseaseNum diseaseNum = new DiseaseNum().build(statConclusionList).ratioNotSymbol().ratio();
        DistrictDiseaseMonitorVO.DiseaseMonitorTable diseaseMonitorTable = buildTable(diseaseNum);
        diseaseMonitorTable.setSchoolAge(schoolAgeDesc);
        return diseaseMonitorTable;
    }

    private DistrictDiseaseMonitorVO.DiseaseMonitorTable buildTable(DiseaseNum diseaseNum) {
        DistrictDiseaseMonitorVO.DiseaseMonitorTable diseaseMonitorTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
        diseaseMonitorTable.setValidScreeningNum(diseaseNum.validScreeningNum);
        diseaseMonitorTable.setHypertensionNum(diseaseNum.hypertension);
        diseaseMonitorTable.setHypertensionRatio(diseaseNum.hypertensionRatio);
        diseaseMonitorTable.setAnemiaNum(diseaseNum.anemia);
        diseaseMonitorTable.setAnemiaRatio(diseaseNum.anemiaRatio);
        diseaseMonitorTable.setDiabetesNum(diseaseNum.diabetes);
        diseaseMonitorTable.setDiabetesRatio(diseaseNum.diabetesRatio);
        diseaseMonitorTable.setAllergicAsthmaNum(diseaseNum.allergicAsthma);
        diseaseMonitorTable.setAllergicAsthmaRatio(diseaseNum.allergicAsthmaRatio);
        diseaseMonitorTable.setPhysicalDisabilityNum(diseaseNum.physicalDisability);
        diseaseMonitorTable.setPhysicalDisabilityRatio(diseaseNum.physicalDisabilityRatio);
        return diseaseMonitorTable;
    }

    @Data
    private static class DiseaseNum extends EntityFunction{

        private Integer validScreeningNum;
        /**
         * 贫血
         */
        private Integer anemia;
        /**
         * 高血压
         */
        private Integer hypertension;

        /**
         * 糖尿病
         */
        private Integer diabetes;
        /**
         * 过敏性哮喘
         */
        private Integer allergicAsthma;
        /**
         * 身体残疾
         */
        private Integer physicalDisability;

        // ========== 不带% ============
        /**
         * 贫血
         */
        private BigDecimal anemiaRatio;
        /**
         * 高血压
         */
        private BigDecimal hypertensionRatio;

        /**
         * 糖尿病
         */
        private BigDecimal diabetesRatio;
        /**
         * 过敏性哮喘
         */
        private BigDecimal allergicAsthmaRatio;
        /**
         * 身体残疾
         */
        private BigDecimal physicalDisabilityRatio;

        // ========== 带% ============
        /**
         * 贫血
         */
        private String anemiaRatioStr;
        /**
         * 高血压
         */
        private String hypertensionRatioStr;

        /**
         * 糖尿病
         */
        private String diabetesRatioStr;
        /**
         * 过敏性哮喘
         */
        private String allergicAsthmaRatioStr;
        /**
         * 身体残疾
         */
        private String physicalDisabilityRatioStr;

        public DiseaseNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();
            this.anemia = getSum(statConclusionList, DiseaseNumDO::getAnemia);
            this.hypertension = getSum(statConclusionList, DiseaseNumDO::getHypertension);
            this.diabetes = getSum(statConclusionList, DiseaseNumDO::getDiabetes);
            this.allergicAsthma  = getSum(statConclusionList, DiseaseNumDO::getAllergicAsthma);
            this.physicalDisability = getSum(statConclusionList, DiseaseNumDO::getPhysicalDisability);
            return this;
        }

        /**
         * 不带%
         */
        public DiseaseNum ratioNotSymbol(){
            this.anemiaRatio = getRatioNotSymbol(anemia,validScreeningNum);
            this.hypertensionRatio = getRatioNotSymbol(hypertension,validScreeningNum);
            this.diabetesRatio = getRatioNotSymbol(diabetes,validScreeningNum);
            this.allergicAsthmaRatio = getRatioNotSymbol(allergicAsthma,validScreeningNum);
            this.physicalDisabilityRatio = getRatioNotSymbol(physicalDisability,validScreeningNum);
            return this;
        }

        /**
         * 带%
         */
        public DiseaseNum ratio(){
            this.anemiaRatioStr = getRatio(anemia,validScreeningNum);
            this.hypertensionRatioStr = getRatio(hypertension,validScreeningNum);
            this.diabetesRatioStr = getRatio(diabetes,validScreeningNum);
            this.allergicAsthmaRatioStr = getRatio(allergicAsthma,validScreeningNum);
            this.physicalDisabilityRatioStr = getRatio(physicalDisability,validScreeningNum);
            return this;
        }

        private Integer getSum(List<StatConclusion> statConclusionList, Function<DiseaseNumDO,Integer> function){
            if (CollectionUtil.isNotEmpty(statConclusionList)){
               return statConclusionList.stream().map(StatConclusion::getDiseaseNum).filter(Objects::nonNull)
                       .map(function).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            }
            return ReportConst.ZERO;
        }

    }


}
