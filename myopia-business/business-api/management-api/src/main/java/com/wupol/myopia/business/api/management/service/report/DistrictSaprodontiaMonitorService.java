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
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
public class DistrictSaprodontiaMonitorService {

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
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = buildSaprodontiaMonitorVariableVO(saprodontiaNum);
        districtSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }
    private DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO buildSaprodontiaMonitorVariableVO(SaprodontiaNum saprodontiaNum){
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(saprodontiaNum.dmftRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(saprodontiaNum.saprodontiaLossAndRepairRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.saprodontiaLossAndRepairTeethRatio);
        return saprodontiaMonitorVariableVO;
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
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<SaprodontiaNum> saprodontiaSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getSaprodontiaNum(gender,list,saprodontiaSexList));

        if (saprodontiaSexList.size() >= 2){
            DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
            saprodontiaSexVariableVO.setSaprodontiaRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaLossNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRepairNum,SaprodontiaNum::getSaprodontiaRatioStr));

            saprodontiaSexVO.setSaprodontiaSexVariableVO(saprodontiaSexVariableVO);
        }
    }

    private void getSaprodontiaNum(Integer gender,List<StatConclusion> statConclusionList,List<SaprodontiaNum> saprodontiaSexList){
        SaprodontiaNum build = new SaprodontiaNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaSexList.add(build);
    }

    private <K>void getSaprodontiaNum(K key, List<StatConclusion> statConclusionList,Map<K, SaprodontiaNum> saprodontiaNumMap){
        SaprodontiaNum build = new SaprodontiaNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaNumMap.put(key,build);
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<SaprodontiaNum> saprodontiaSexList, Function<SaprodontiaNum,Integer> function, Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function));
        DistrictSaprodontiaMonitorVO.SaprodontiaSex sex = new DistrictSaprodontiaMonitorVO.SaprodontiaSex();
        for (int i = 0; i < saprodontiaSexList.size(); i++) {
            SaprodontiaNum num = saprodontiaSexList.get(i);
            if (i==0){
                sex.setForwardSex(GenderEnum.getName(num.gender));
                sex.setForwardRatio(mapper.apply(num));
            }
            if (i==1){
                sex.setBackSex(GenderEnum.getName(num.gender));
                sex.setBackRatio(mapper.apply(num));
            }
        }
        return sex;
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

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol();

        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);

        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
       return saprodontiaMonitorTable;
    }

    public DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable buildTable(SaprodontiaNum saprodontiaNum){
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaMonitorTable.setValidScreeningNum(saprodontiaNum.validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(saprodontiaNum.dmftRatio);
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum.saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatio);
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaNum.saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(saprodontiaNum.saprodontiaLossRatio);
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaNum.saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaNum.saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(saprodontiaNum.saprodontiaLossAndRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(saprodontiaNum.saprodontiaLossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.saprodontiaLossAndRepairTeethRatio);
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

        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(conclusionMap, SchoolAge.PRIMARY.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.JUNIOR.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(conclusionMap,10);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.HIGH.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(conclusionMap,SchoolAge.UNIVERSITY.code);

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


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(Map<Integer, List<StatConclusion>> conclusionMap, Integer schoolAge) {
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
            return getSaprodontiaSchoolAge(mergeList);
        }

        return getSaprodontiaSchoolAge(conclusionMap.get(schoolAge));

    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList);

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge saprodontiaSchoolAge = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge();

        saprodontiaSchoolAge.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatio);
        saprodontiaSchoolAge.setSaprodontiaLossRatio(saprodontiaNum.saprodontiaLossRatio);
        saprodontiaSchoolAge.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatio);

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getSaprodontiaNum(gradeCodeEnum.getName(),list,saprodontiaNumMap);
        });

        if (saprodontiaNumMap.size() >= 2){
            saprodontiaSchoolAge.setMaxSaprodontiaRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum,SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaLossRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaRepairRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum,SaprodontiaNum::getSaprodontiaRepairRatioStr));
        }

        return saprodontiaSchoolAge;
    }
    private DistrictSaprodontiaMonitorVO.GradeRatio getGradeRatio(Map<String, SaprodontiaNum> saprodontiaNumMap,Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper){
        if (CollectionUtil.isNotEmpty(saprodontiaNumMap)){
            return null;
        }
        DistrictSaprodontiaMonitorVO.GradeRatio gradeRatio = new DistrictSaprodontiaMonitorVO.GradeRatio();
        TwoTuple<String, String> tuple = getMaxMap(saprodontiaNumMap, function, mapper);
        gradeRatio.setGrade(tuple.getFirst());
        gradeRatio.setRatio(tuple.getSecond());
        return gradeRatio;
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,String> getMaxMap(Map<K, T> map, Function<T,Integer> function,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
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
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
            if (CollectionUtil.isNotEmpty(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
        }else {
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> universityList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeMonitorTableList(tableList);
    }


    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
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
            return Lists.newArrayList();
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
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);
        saprodontiaMonitorTable.setItemName(grade);
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

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getSaprodontiaNum(age,list,saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2){
            DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO();
            saprodontiaAgeVariableVO.setSaprodontiaRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum,SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaLossRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum,SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
        }
    }


    private DistrictSaprodontiaMonitorVO.AgeRatio getAgeRatio(Map<Integer, SaprodontiaNum> saprodontiaNumMap, Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = getMinMap(saprodontiaNumMap, function,mapper);
        DistrictSaprodontiaMonitorVO.AgeRatio ageRatio = new DistrictSaprodontiaMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
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
    private <T,K>TwoTuple<K,String> getMinMap(Map<K, T> map, Function<T,Integer> function,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
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

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaAgeMonitorTable = buildTable(saprodontiaNum);
        saprodontiaAgeMonitorTable.setItemName(itemName);
        tableList.add(saprodontiaAgeMonitorTable);
    }

    @Data
    private static class SaprodontiaNum{

        /**
         * 筛查人数
         */
        private Integer validScreeningNum;

        /**
         * 龋失补牙数
         */
        private Integer dmftNum;

        /**
         * 龋均
         */
        private String dmftRatio;

        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋失人数
         */
        private Integer saprodontiaLossNum;

        /**
         * 龋补人数
         */
        private Integer saprodontiaRepairNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 龋患（失、补）牙数
         */
        private Integer saprodontiaLossAndRepairTeethNum;

        // ========= 不带% =============

        /**
         * 龋患率
         */
        private BigDecimal saprodontiaRatio;
        /**
         * 龋失率
         */
        private BigDecimal saprodontiaLossRatio;

        /**
         * 龋补率
         */
        private BigDecimal saprodontiaRepairRatio;
        /**
         * 龋患（失、补）率
         */
        private BigDecimal saprodontiaLossAndRepairRatio;

        /**
         * 龋患（失、补）构成比
         */
        private BigDecimal saprodontiaLossAndRepairTeethRatio;

        // ========= 带% =============

        /**
         * 龋患率
         */
        private String saprodontiaRatioStr;
        /**
         * 龋失率
         */
        private String saprodontiaLossRatioStr;
        /**
         * 龋补率
         */
        private String saprodontiaRepairRatioStr;


        /**
         * 性别
         */
        private Integer gender;

        public SaprodontiaNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }

        public SaprodontiaNum build(List<StatConclusion> statConclusionList){

            if (CollectionUtil.isNotEmpty(statConclusionList)){
                this.validScreeningNum = statConclusionList.size();
            }

            Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);

            List<StatConclusion> dmftList = statConclusionList.stream().filter(Objects::nonNull).filter(predicateTrue).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(dmftList)){
                this.dmftNum = dmftList.stream().mapToInt(totalFunction).sum();
            }

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);

            List<StatConclusion> lossAndRepairTeethList = statConclusionList.stream().filter(Objects::nonNull).filter(lossAndRepairPredicateTrue).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(lossAndRepairTeethList)){
                this.saprodontiaLossAndRepairTeethNum = lossAndRepairTeethList.stream().mapToInt(lossAndRepairTotalFunction).sum();
            }

            List<Boolean> saprodontiaList = getList(statConclusionList, StatConclusion::getIsSaprodontia);
            if (CollectionUtil.isNotEmpty(saprodontiaList)){
                this.saprodontiaNum = (int) saprodontiaList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> saprodontiaLossList = getList(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
            if (CollectionUtil.isNotEmpty(saprodontiaLossList)){
                this.saprodontiaLossNum = (int) saprodontiaLossList.stream().filter(Boolean::booleanValue).count();
            }

            List<Boolean> saprodontiaRepairList = getList(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
            if (CollectionUtil.isNotEmpty(saprodontiaRepairList)){
                this.saprodontiaRepairNum = (int) saprodontiaRepairList.stream().filter(Boolean::booleanValue).count();
            }
            List<StatConclusion> lossAndRepairList = statConclusionList.stream()
                    .filter(sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(lossAndRepairList)){
                this.saprodontiaLossAndRepairNum = lossAndRepairList.size();
            }

            return this;
        }

        private List<Boolean> getList(List<StatConclusion> statConclusionList, Function<StatConclusion,Boolean> function){
            if (CollectionUtil.isEmpty(statConclusionList)){
                return Lists.newArrayList();
            }
            return statConclusionList.stream().map(function).filter(Objects::nonNull).collect(Collectors.toList());
        }

        /**
         * 不带%
         */
        public SaprodontiaNum ratioNotSymbol(){

            if (Objects.nonNull(dmftNum)){
                this.dmftRatio = MathUtil.num(dmftNum,validScreeningNum);
            }

            if (Objects.nonNull(saprodontiaNum)){
                this.saprodontiaRatio = MathUtil.ratioNotSymbol(saprodontiaNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossNum)){
                this.saprodontiaLossRatio =MathUtil.ratioNotSymbol(saprodontiaLossNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaRepairNum)){
                this.saprodontiaRepairRatio = MathUtil.ratioNotSymbol(saprodontiaRepairNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossAndRepairNum)){
                this.saprodontiaLossAndRepairRatio = MathUtil.ratioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossAndRepairTeethNum) && Objects.nonNull(dmftNum)){
                this.saprodontiaLossAndRepairTeethRatio =MathUtil.ratioNotSymbol(saprodontiaLossAndRepairTeethNum,dmftNum);
            }

            return this;
        }

        /**
         * 带%
         */
        public SaprodontiaNum ratio(){
            if (Objects.nonNull(dmftNum)){
                this.dmftRatio = MathUtil.num(dmftNum,validScreeningNum);
            }

            if (Objects.nonNull(saprodontiaNum)){
                this.saprodontiaRatioStr = MathUtil.ratio(saprodontiaNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaLossNum)){
                this.saprodontiaLossRatioStr =MathUtil.ratio(saprodontiaLossNum,validScreeningNum);
            }
            if (Objects.nonNull(saprodontiaRepairNum)){
                this.saprodontiaRepairRatioStr = MathUtil.ratio(saprodontiaRepairNum,validScreeningNum);
            }

            return this;
        }
    }
}
