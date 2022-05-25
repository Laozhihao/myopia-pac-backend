package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictDiseaseMonitorVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseaseNumDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

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
public class DiseaseMonitorService {

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
        int validScreeningNum = statConclusionList.size();
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.nonNull(sc.getDiseaseNum())).collect(Collectors.toList());

        int hypertensionNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getHypertension()).orElse(0)).sum();
        int anemiaNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAnemia()).orElse(0)).sum();
        int diabetesNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getDiabetes()).orElse(0)).sum();
        int allergicAsthmaNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAllergicAsthma()).orElse(0)).sum();
        int physicalDisabilityNum = conclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getPhysicalDisability()).orElse(0)).sum();

        diseaseMonitorVariableVO.setHypertensionRatio(MathUtil.ratio(hypertensionNum,validScreeningNum));
        diseaseMonitorVariableVO.setAnemiaRatio(MathUtil.ratio(anemiaNum,validScreeningNum));
        diseaseMonitorVariableVO.setDiabetesRatio(MathUtil.ratio(diabetesNum,validScreeningNum));
        diseaseMonitorVariableVO.setAllergicAsthmaRatio(MathUtil.ratio(allergicAsthmaNum,validScreeningNum));
        diseaseMonitorVariableVO.setPhysicalDisabilityRatio(MathUtil.ratio(physicalDisabilityNum,validScreeningNum));

        //最高值
        Map<Integer, List<StatConclusion>> schoolAgeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        Map<Integer, DiseaseNumDO> diseaseNumDOMap = Maps.newHashMap();
        schoolAgeMap.forEach((schoolAge,list)-> diseaseNumDOMap.put(schoolAge,getDiseaseNumDO(list)));

        TwoTuple<Integer, Integer> hypertensionTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getHypertension);
        TwoTuple<Integer, Integer> anemiaTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getAnemia);
        TwoTuple<Integer, Integer> diabetesTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getDiabetes);
        TwoTuple<Integer, Integer> allergicAsthmaTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getAllergicAsthma);
        TwoTuple<Integer, Integer> physicalDisabilityTuple = getMaxKeyAndValue(diseaseNumDOMap, DiseaseNumDO::getPhysicalDisability);

        diseaseMonitorVariableVO.setMaxHypertensionRatio(getSchoolAgeRatio(hypertensionTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxAnemiaRatio(getSchoolAgeRatio(anemiaTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxDiabetesRatio(getSchoolAgeRatio(diabetesTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxAllergicAsthmaRatio(getSchoolAgeRatio(allergicAsthmaTuple,validScreeningNum));
        diseaseMonitorVariableVO.setMaxPhysicalDisabilityRatio(getSchoolAgeRatio(physicalDisabilityTuple,validScreeningNum));

        districtDiseaseMonitorVO.setDiseaseMonitorVariableVO(diseaseMonitorVariableVO);
    }


    /**
     * 获取疾病数统计
     */
    private DiseaseNumDO getDiseaseNumDO(List<StatConclusion> statConclusionList){
        int hypertensionNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getHypertension()).orElse(0)).sum();
        int anemiaNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAnemia()).orElse(0)).sum();
        int diabetesNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getDiabetes()).orElse(0)).sum();
        int allergicAsthmaNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getAllergicAsthma()).orElse(0)).sum();
        int physicalDisabilityNum = statConclusionList.stream().mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum().getPhysicalDisability()).orElse(0)).sum();
        return new DiseaseNumDO().setHypertension(hypertensionNum)
                .setAnemia(anemiaNum).setDiabetes(diabetesNum)
                .setAllergicAsthma(allergicAsthmaNum).setPhysicalDisability(physicalDisabilityNum);

    }

    private DistrictDiseaseMonitorVO.SchoolAgeRatio getSchoolAgeRatio(TwoTuple<Integer, Integer> tuple,Integer validScreeningNum){
        DistrictDiseaseMonitorVO.SchoolAgeRatio schoolAgeRatio = new DistrictDiseaseMonitorVO.SchoolAgeRatio();
        if (Objects.nonNull(tuple)){
            schoolAgeRatio.setSchoolAge(SchoolAge.get(tuple.getFirst()).desc);
            schoolAgeRatio.setRatio(MathUtil.ratio(tuple.getSecond(),validScreeningNum));
        }
        return schoolAgeRatio;
    }

    /**
     * 疾病监测情况-说明变量-获取map中Value最大值及对应的Key
     */
    private TwoTuple<Integer,Integer> getMaxKeyAndValue(Map<Integer, DiseaseNumDO> map, Function<DiseaseNumDO,Integer> function){
        List<Map.Entry<Integer, DiseaseNumDO>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<Integer, DiseaseNumDO> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
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
            tableList.add(highTable);
            tableList.add(normalHighTable);
            tableList.add(vocationalHighTable);
        }else {
            tableList.add(highTable);
        }

        if (Objects.nonNull(universityTable)){
            tableList.add(universityTable);
        }

        DistrictDiseaseMonitorVO.DiseaseMonitorTable totalTable = getRecord(statConclusionList,"合计");
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
            return getRecord(mergeList, "高中");
        }

        List<StatConclusion> statConclusionList = conclusionMap.get(schoolAge);
        if(CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,SchoolAge.HIGH.code)){
            DistrictDiseaseMonitorVO.DiseaseMonitorTable normalHighTable = getRecord(statConclusionList, "普高");
            if (Objects.isNull(normalHighTable)){
                normalHighTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
                normalHighTable.setSchoolAge("普高");
            }
            return normalHighTable;
        }
        if (Objects.equals(schoolAge,SchoolAge.VOCATIONAL_HIGH.code)){
            return getRecord(statConclusionList, "职高");
        }

        return getRecord(statConclusionList, SchoolAge.get(schoolAge).desc);
    }

    private DistrictDiseaseMonitorVO.DiseaseMonitorTable getRecord(List<StatConclusion> statConclusionList,String schoolAgeDesc) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        DistrictDiseaseMonitorVO.DiseaseMonitorTable diseaseMonitorTable = new DistrictDiseaseMonitorVO.DiseaseMonitorTable();
        int validScreeningNum = statConclusionList.size();

        DistrictDiseaseMonitorVO.Ratio hypertensionRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getHypertension);
        DistrictDiseaseMonitorVO.Ratio anemiaRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getAnemia);
        DistrictDiseaseMonitorVO.Ratio diabetesRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getDiabetes);
        DistrictDiseaseMonitorVO.Ratio allergicAsthmaRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getAllergicAsthma);
        DistrictDiseaseMonitorVO.Ratio physicalDisabilityRatio = getRatio(statConclusionList,validScreeningNum,DiseaseNumDO::getPhysicalDisability);

        diseaseMonitorTable.setSchoolAge(schoolAgeDesc);
        diseaseMonitorTable.setValidScreeningNum(validScreeningNum);
        diseaseMonitorTable.setHypertension(hypertensionRatio);
        diseaseMonitorTable.setAnemia(anemiaRatio);
        diseaseMonitorTable.setDiabetes(diabetesRatio);
        diseaseMonitorTable.setAllergicAsthma(allergicAsthmaRatio);
        diseaseMonitorTable.setPhysicalDisability(physicalDisabilityRatio);

        return diseaseMonitorTable;
    }

    private DistrictDiseaseMonitorVO.Ratio getRatio(List<StatConclusion> primaryAndAboveStatConclusionList, int validScreeningNum,Function<DiseaseNumDO,Integer> function) {
        if (CollectionUtil.isEmpty(primaryAndAboveStatConclusionList)){
            return null;
        }
        DistrictDiseaseMonitorVO.Ratio ratio = new DistrictDiseaseMonitorVO.Ratio();
        int num = primaryAndAboveStatConclusionList.stream()
                .filter(sc -> Objects.nonNull(sc.getDiseaseNum()))
                .mapToInt(sc -> Optional.ofNullable(sc.getDiseaseNum()).map(function).orElse(0)).sum();
        ratio.setNum(num);
        ratio.setRatio(MathUtil.ratio(num,validScreeningNum));
        return null;
    }



}
