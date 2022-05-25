package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSaprodontiaMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 龋齿监测
 *
 * @author hang.yuan 2022/5/25 15:04
 */
@Service
public class SaprodontiaMonitorService {

    /**
     * 龋齿监测结果
     */
    public void getDistrictSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO = new DistrictSaprodontiaMonitorVO();

        //说明变量
        getSaprodontiaMonitorVariableVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同性别
        getSaprodontiaSexVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同学龄段
        getSaprodontiaSchoolAgeVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同年龄段
        getSaprodontiaAgeVO(statConclusionList,districtSaprodontiaMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSaprodontiaMonitorVO(districtSaprodontiaMonitorVO);
    }

    /**
     * 龋齿监测结果-说明变量
     */
    private void getSaprodontiaMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int validScreeningNum = statConclusionList.size();

        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

        int lossAndRepairTeethNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

        int dmftNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(MathUtil.ratio(dmftNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));

        districtSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList,saprodontiaSexVO);

        districtSaprodontiaMonitorVO.setSaprodontiaSexVO(saprodontiaSexVO);

    }

    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        int validScreeningNum = statConclusionList.size();
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum> saprodontiaSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> saprodontiaSexList.add(getSaprodontiaSexNum(gender,list)));

        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontia);
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaLossRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontiaLoss);
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaRepairRatioCompare = getRatioCompare(saprodontiaSexList,validScreeningNum, DistrictSaprodontiaMonitorVO.SaprodontiaSexNum::getSaprodontiaRepair);

        DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
        saprodontiaSexVariableVO.setSaprodontiaRatioCompare(saprodontiaRatioCompare);
        saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(saprodontiaLossRatioCompare);
        saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(saprodontiaRepairRatioCompare);

        saprodontiaSexVO.setSaprodontiaSexVariableVO(saprodontiaSexVariableVO);
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSexNum getSaprodontiaSexNum(Integer gender,List<StatConclusion> statConclusionList){
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        DistrictSaprodontiaMonitorVO.SaprodontiaSexNum saprodontiaSexNum = new DistrictSaprodontiaMonitorVO.SaprodontiaSexNum();
        saprodontiaSexNum.setGender(gender);
        saprodontiaSexNum.setSaprodontia(saprodontiaNum);
        saprodontiaSexNum.setSaprodontiaLoss(saprodontiaLossNum);
        saprodontiaSexNum.setSaprodontiaRepair(saprodontiaRepairNum);
        return saprodontiaSexNum;
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum> saprodontiaSexList, Integer validScreeningNum, Function<DistrictSaprodontiaMonitorVO.SaprodontiaSexNum,Integer> function) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function));
        DistrictSaprodontiaMonitorVO.SaprodontiaSex saprodontiaSex = new DistrictSaprodontiaMonitorVO.SaprodontiaSex();
        for (int i = 0; i < saprodontiaSexList.size(); i++) {
            DistrictSaprodontiaMonitorVO.SaprodontiaSexNum saprodontiaSexNum = saprodontiaSexList.get(i);
            if (i==0){
                saprodontiaSex.setForwardSex(GenderEnum.getName(saprodontiaSexNum.getGender()));
                saprodontiaSex.setForwardRatio(MathUtil.ratio(function,saprodontiaSexNum,validScreeningNum));
            }
            if (i==1){
                saprodontiaSex.setBackSex(GenderEnum.getName(saprodontiaSexNum.getGender()));
                saprodontiaSex.setBackRatio(MathUtil.ratio(function,saprodontiaSexNum,validScreeningNum));
            }
        }
        return saprodontiaSex;
    }


    /**
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaSexMonitorTableList(tableList);

    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList,Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        int validScreeningNum = conclusionlist.size();

        int saprodontiaNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)conclusionlist.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        saprodontiaMonitorTable.setValidScreeningNum(validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        return saprodontiaMonitorTable;
    }

    /**
     * 龋齿监测结果-不同学龄段
     */
    private void getSaprodontiaSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO();
        getSaprodontiaSchoolAgeVariableVO(statConclusionList,saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorTableList(statConclusionList,saprodontiaSchoolAgeVO);

        districtSaprodontiaMonitorVO.setSaprodontiaSchoolAgeVO(saprodontiaSchoolAgeVO);

    }

    /**
     * 龋齿监测结果-不同学龄段-说明变量
     */
    private void getSaprodontiaSchoolAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO();

        int validScreeningNum = statConclusionList.size();
        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(conclusionMap, SchoolAge.PRIMARY.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.JUNIOR.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(conclusionMap,10,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.HIGH.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.VOCATIONAL_HIGH.code,validScreeningNum);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.UNIVERSITY.code,validScreeningNum);

        saprodontiaSchoolAgeVariableVO.setPrimarySchool(primary);
        saprodontiaSchoolAgeVariableVO.setJuniorHighSchool(junior);
        saprodontiaSchoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)){
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
            saprodontiaSchoolAgeVariableVO.setNormalHighSchool(normalHigh);
            saprodontiaSchoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        }else {
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
        }
        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeVariableVO(saprodontiaSchoolAgeVariableVO);
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(Map<Integer, List<StatConclusion>> conclusionMap, Integer schoolAge,Integer validScreeningNum) {
        if (CollectionUtil.isEmpty(conclusionMap)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<StatConclusion> mergeList=Lists.newArrayList();
            List<StatConclusion> normalHigh = conclusionMap.get(SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHigh)){
                mergeList.addAll(normalHigh);
            }
            List<StatConclusion> vocationalHigh = conclusionMap.get(SchoolAge.VOCATIONAL_HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHigh)){
                mergeList.addAll(vocationalHigh);
            }
            return getSaprodontiaSchoolAge(mergeList,validScreeningNum);
        }
        List<StatConclusion> statConclusionList = conclusionMap.get(schoolAge);

        return getSaprodontiaSchoolAge(statConclusionList,validScreeningNum);

    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(List<StatConclusion> statConclusionList,Integer validScreeningNum){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge saprodontiaSchoolAge = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge();
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        saprodontiaSchoolAge.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaSchoolAge.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaSchoolAge.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->saprodontiaNumMap.put(gradeCode,getSaprodontiaNum(list)));

        TwoTuple<String, Integer> saprodontia = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontia);
        TwoTuple<String, Integer> saprodontiaLoss = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaLoss);
        TwoTuple<String, Integer> saprodontiaRepair = getMaxMap(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaRepair);

        saprodontiaSchoolAge.setMaxSaprodontiaRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontia.getFirst(),MathUtil.ratio(saprodontia.getSecond(),validScreeningNum)));
        saprodontiaSchoolAge.setMaxSaprodontiaLossRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontiaLoss.getFirst(),MathUtil.ratio(saprodontiaLoss.getSecond(),validScreeningNum)));
        saprodontiaSchoolAge.setMaxSaprodontiaRepairRatio(new DistrictSaprodontiaMonitorVO.GradeRatio(saprodontiaRepair.getFirst(),MathUtil.ratio(saprodontiaRepair.getSecond(),validScreeningNum)));

        return saprodontiaSchoolAge;
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaNum getSaprodontiaNum(List<StatConclusion> statConclusionList) {
        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

        return new DistrictSaprodontiaMonitorVO.SaprodontiaNum()
                .setSaprodontia(saprodontiaNum)
                .setSaprodontiaLoss(saprodontiaLossNum)
                .setSaprodontiaRepair(saprodontiaRepairNum);
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,Integer> getMaxMap(Map<K, T> map, Function<T,Integer> function){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
    }


    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> primaryList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> juniorList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> highList = getSaprodontiaSchoolAgeTable(statConclusionList,10);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> normalHighList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            tableList.addAll(highList);
            tableList.addAll(normalHighList);
            tableList.addAll(vocationalHighList);
        }else {
            tableList.addAll(highList);
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> universityList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeMonitorTableList(tableList);
    }


    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> mergeList=Lists.newArrayList();
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> normalHighList = getSaprodontiaGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                mergeList.addAll(normalHighList);
            }
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            return mergeList;
        }

        return getSaprodontiaGrade(statConclusionList,schoolAge);
    }


    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        int validScreeningNum = statConclusionList.size();
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> getSaprodontiaSchoolAgeTable(list,grade,validScreeningNum,gradeList));
        getSaprodontiaSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,validScreeningNum,gradeList);

        return gradeList;
    }

    private void getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,String grade,int validScreeningNum,List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int saprodontiaNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)statConclusionList.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = statConclusionList.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaMonitorTable.setItemName(grade);
        saprodontiaMonitorTable.setValidScreeningNum(validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));

        gradeList.add(saprodontiaMonitorTable);
    }

    /**
     * 龋齿监测结果-不同年龄
     */
    private void getSaprodontiaAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList,saprodontiaAgeVO);

        districtSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }

    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        int validScreeningNum = statConclusionList.size();

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->saprodontiaNumMap.put(age,getSaprodontiaNum(list)));

        DistrictSaprodontiaMonitorVO.AgeRatio saprodontia = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontia,validScreeningNum);
        DistrictSaprodontiaMonitorVO.AgeRatio saprodontiaLoss = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaLoss,validScreeningNum);
        DistrictSaprodontiaMonitorVO.AgeRatio saprodontiaRepair = getAgeRatio(saprodontiaNumMap, DistrictSaprodontiaMonitorVO.SaprodontiaNum::getSaprodontiaRepair,validScreeningNum);

        DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO();
        saprodontiaAgeVariableVO.setSaprodontiaRatio(saprodontia);
        saprodontiaAgeVariableVO.setSaprodontiaLossRatio(saprodontiaLoss);
        saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(saprodontiaRepair);

        saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
    }


    private DistrictSaprodontiaMonitorVO.AgeRatio getAgeRatio(Map<Integer, DistrictSaprodontiaMonitorVO.SaprodontiaNum> saprodontiaNumMap, Function<DistrictSaprodontiaMonitorVO.SaprodontiaNum,Integer> function,int validScreeningNum) {
        TwoTuple<Integer, Integer> maxTuple = getMaxMap(saprodontiaNumMap, function);
        TwoTuple<Integer, Integer> minTuple = getMinMap(saprodontiaNumMap, function);
        DistrictSaprodontiaMonitorVO.AgeRatio ageRatio = new DistrictSaprodontiaMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(MathUtil.ratio(maxTuple.getSecond(),validScreeningNum));
        ageRatio.setMinRatio(MathUtil.ratio(minTuple.getSecond(),validScreeningNum));
        return ageRatio;
    }

    private Integer getLessAge(Integer age){
        if (age < 6){
            return 6;
        }else if (age < 8){
            return 8;
        }else if (age < 10){
            return 10;
        }else if (age < 12){
            return 12;
        }else if (age < 14){
            return 14;
        }else if (age < 16){
            return 16;
        }else if (age < 18){
            return 18;
        }else {
            return 19;
        }
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    private <T,K>TwoTuple<K,Integer> getMinMap(Map<K, T> map, Function<T,Integer> function){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0));
    }

    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getSaprodontiaAgeTable(age,list,tableList));
        getSaprodontiaAgeTable(1000,statConclusionList,tableList);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorTableList(tableList);

    }


    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        int validScreeningNum = conclusionlist.size();

        int saprodontiaNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontia)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaRepairNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaRepair)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossNum = (int)conclusionlist.stream()
                .map(StatConclusion::getIsSaprodontiaLoss)
                .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
        int saprodontiaLossAndRepairNum = (int)conclusionlist.stream()
                .filter(sc ->Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE,sc.getIsSaprodontiaRepair())).count();

        Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
        ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
        int lossAndRepairTeethNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(lossAndRepairPredicateTrue)
                .mapToInt(lossAndRepairTotalFunction).sum();

        Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
        ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
        int dmftNum = conclusionlist.stream().filter(Objects::nonNull)
                .filter(predicateTrue)
                .mapToInt(totalFunction).sum();


        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaAgeMonitorTable = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaAgeMonitorTable.setItemName(itemName);
        saprodontiaAgeMonitorTable.setSaprodontiaNum(saprodontiaNum);
        saprodontiaAgeMonitorTable.setSaprodontiaRatio(MathUtil.ratio(saprodontiaNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossNum(saprodontiaLossNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossRatio(MathUtil.ratio(saprodontiaLossNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaRepairNum(saprodontiaRepairNum);
        saprodontiaAgeMonitorTable.setSaprodontiaRepairRatio(MathUtil.ratio(saprodontiaRepairNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaLossAndRepairNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairRatio(MathUtil.ratio(saprodontiaLossAndRepairNum,validScreeningNum));
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairTeethNum(lossAndRepairTeethNum);
        saprodontiaAgeMonitorTable.setSaprodontiaLossAndRepairTeethRatio(MathUtil.ratio(lossAndRepairTeethNum,dmftNum));
        saprodontiaAgeMonitorTable.setDmftRatio(MathUtil.num(dmftNum,validScreeningNum));

        tableList.add(saprodontiaAgeMonitorTable);
    }
}
