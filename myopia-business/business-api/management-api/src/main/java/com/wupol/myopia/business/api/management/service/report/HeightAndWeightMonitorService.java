package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictHeightAndWeightMonitorVO;
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
import java.util.stream.Collectors;

/**
 * 身高体重监测
 *
 * @author hang.yuan 2022/5/25 15:05
 */
@Service
public class HeightAndWeightMonitorService {

    /**
     * 体重身高监测结果
     */
    public void getDistrictHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO = new DistrictHeightAndWeightMonitorVO();

        //说明变量
        getHeightAndWeightMonitorVariableVO(statConclusionList,districtHeightAndWeightMonitorVO);
        //不同性别
        getHeightAndWeightSexVO(statConclusionList,districtHeightAndWeightMonitorVO);
        //不同学龄段
        getHeightAndWeightSchoolAgeVO(statConclusionList,districtHeightAndWeightMonitorVO);
        //不同年龄段
        getHeightAndWeightAgeVO(statConclusionList,districtHeightAndWeightMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictHeightAndWeightMonitorVO(districtHeightAndWeightMonitorVO);
    }

    /**
     * 体重身高监测结果-说明变量
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightNum heightAndWeight = new HeightAndWeightNum().build(statConclusionList).ratio();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO();
        heightAndWeightMonitorVariableVO.setOverweightRatio(heightAndWeight.overweightRatio);
        heightAndWeightMonitorVariableVO.setObeseRatio(heightAndWeight.obeseRatio);
        heightAndWeightMonitorVariableVO.setStuntingRatio(heightAndWeight.stuntingRatio);
        heightAndWeightMonitorVariableVO.setMalnourishedRatio(heightAndWeight.malnourishedRatio);

        districtHeightAndWeightMonitorVO.setHeightAndWeightMonitorVariableVO(heightAndWeightMonitorVariableVO);
    }

    /**
     * 体重身高监测结果-不同性别
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList,heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList,heightAndWeightSexVO);

        districtHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<HeightAndWeightNum> heightAndWeightSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getHeightAndWeightNum(gender,list,heightAndWeightSexList));

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSex overweight = getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSex obese = getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSex stunting = getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSex malnourished = getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr);

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO();
        heightAndWeightSexVariableVO.setOverweightRatioCompare(overweight);
        heightAndWeightSexVariableVO.setObeseRatioCompare(obese);
        heightAndWeightSexVariableVO.setStuntingRatioCompare(stunting);
        heightAndWeightSexVariableVO.setMalnourishedRatioCompare(malnourished);

        heightAndWeightSexVO.setHeightAndWeightSexVariableVO(heightAndWeightSexVariableVO);

    }

    private void getHeightAndWeightNum(Integer gender, List<StatConclusion> statConclusionList,List<HeightAndWeightNum> heightAndWeightSexList){
        HeightAndWeightNum build = new HeightAndWeightNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightSexList.add(build);
    }
    private <K>void getHeightAndWeightNum(K key, List<StatConclusion> statConclusionList,Map<K, HeightAndWeightNum> heightAndWeightNumMap){
        HeightAndWeightNum build = new HeightAndWeightNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightNumMap.put(key,build);
    }


    private DistrictHeightAndWeightMonitorVO.HeightAndWeightSex getRatioCompare(List<HeightAndWeightNum> heightAndWeightNumList, Function<HeightAndWeightNum,Integer> function,Function<HeightAndWeightNum,String> mapper) {
        if (CollectionUtil.isEmpty(heightAndWeightNumList)){
            return null;
        }
        CollectionUtil.sort(heightAndWeightNumList, Comparator.comparing(function));
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSex sex = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSex();
        for (int i = 0; i < heightAndWeightNumList.size(); i++) {
            HeightAndWeightNum num = heightAndWeightNumList.get(i);
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
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable maleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable femaleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable totalTable = getHeightAndWeightSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setHeightAndWeightSexMonitorTableList(tableList);

    }
    private DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable getHeightAndWeightSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        if (Objects.equals(10,gender)){
            heightAndWeightMonitorTable.setItemName("合计");
        }else {
            heightAndWeightMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return heightAndWeightMonitorTable;
    }

    public DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable buildTable(HeightAndWeightNum heightAndWeightNum){
        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= new DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable();
        heightAndWeightMonitorTable.setValidScreeningNum(heightAndWeightNum.validScreeningNum);
        heightAndWeightMonitorTable.setOverweightNum(heightAndWeightNum.overweightNum);
        heightAndWeightMonitorTable.setOverweightRatio(heightAndWeightNum.overweightRatio);
        heightAndWeightMonitorTable.setObeseNum(heightAndWeightNum.obeseNum);
        heightAndWeightMonitorTable.setObeseRatio(heightAndWeightNum.obeseRatio);
        heightAndWeightMonitorTable.setStuntingNum(heightAndWeightNum.stuntingNum);
        heightAndWeightMonitorTable.setStuntingRatio(heightAndWeightNum.stuntingRatio);
        heightAndWeightMonitorTable.setMalnourishedNum(heightAndWeightNum.malnourishedNum);
        heightAndWeightMonitorTable.setMalnourishedRatio(heightAndWeightNum.malnourishedRatio);
        return heightAndWeightMonitorTable;
    }

    /**
     * 体重身高监测结果-不同学龄段
     */
    private void getHeightAndWeightSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO();
        getHeightAndWeightSchoolAgeVariableVO(statConclusionList,heightAndWeightSchoolAgeVO);
        getHeightAndWeightSchoolAgeMonitorTableList(statConclusionList,heightAndWeightSchoolAgeVO);

        districtHeightAndWeightMonitorVO.setHeightAndWeightSchoolAgeVO(heightAndWeightSchoolAgeVO);

    }

    /**
     * 体重身高监测结果-不同学龄段-说明变量
     */
    private void getHeightAndWeightSchoolAgeVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVariableVO schoolAgeVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVariableVO();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge primary = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge junior = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.JUNIOR.code);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge high = getHeightAndWeightSchoolSchoolAge(statConclusionList,10);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge normalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.HIGH.code);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge vocationalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge university = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.UNIVERSITY.code);

        schoolAgeVariableVO.setPrimarySchool(primary);
        schoolAgeVariableVO.setJuniorHighSchool(junior);
        schoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)){
            schoolAgeVariableVO.setHighSchool(high);
            schoolAgeVariableVO.setNormalHighSchool(normalHigh);
            schoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        }else {
            schoolAgeVariableVO.setHighSchool(high);
        }
        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    private DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge getHeightAndWeightSchoolSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

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
            return getHeightAndWeightSchoolAge(mergeList);
        }

        List<StatConclusion> conclusions = conclusionMap.get(schoolAge);

        return getHeightAndWeightSchoolAge(conclusions);

    }

    private DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge getHeightAndWeightSchoolAge(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge heightAndWeightSchoolAge = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge();

        heightAndWeightSchoolAge.setOverweightRatio(heightAndWeightNum.overweightRatio);
        heightAndWeightSchoolAge.setObeseRatio(heightAndWeightNum.obeseRatio);
        heightAndWeightSchoolAge.setStuntingRatio(heightAndWeightNum.stuntingRatio);
        heightAndWeightSchoolAge.setMalnourishedRatio(heightAndWeightNum.malnourishedRatio);

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getHeightAndWeightNum(gradeCodeEnum.getName(),list,heightAndWeightNumMap);
        });

        TwoTuple<String, String> overweight = getMaxMap(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr);
        TwoTuple<String, String> obese = getMaxMap(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr);
        TwoTuple<String, String> stunting = getMaxMap(heightAndWeightNumMap,HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr);
        TwoTuple<String, String> malnourished = getMaxMap(heightAndWeightNumMap,HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr);

        heightAndWeightSchoolAge.setMaxOverweightRatio(new DistrictHeightAndWeightMonitorVO.GradeRatio(overweight.getFirst(),overweight.getSecond()));
        heightAndWeightSchoolAge.setMaxObeseRatio(new DistrictHeightAndWeightMonitorVO.GradeRatio(obese.getFirst(),obese.getSecond()));
        heightAndWeightSchoolAge.setMaxStuntingRatio(new DistrictHeightAndWeightMonitorVO.GradeRatio(stunting.getFirst(),stunting.getSecond()));
        heightAndWeightSchoolAge.setMaxMalnourishedRatio(new DistrictHeightAndWeightMonitorVO.GradeRatio(malnourished.getFirst(),malnourished.getSecond()));

        return heightAndWeightSchoolAge;
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
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> primaryList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> juniorList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (Objects.nonNull(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> highList = getHeightAndWeighSchoolAgeTable(statConclusionList,10);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> normalHighList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeighSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(vocationalHighList)){
            if (Objects.nonNull(highList)){
                tableList.addAll(highList);
            }
            if (Objects.nonNull(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
        }else {
            if (Objects.nonNull(highList)){
                tableList.addAll(highList);
            }
        }

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> universityList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(universityList)){
            tableList.addAll(universityList);
        }

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }

    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeighSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightGrade(statConclusionList, SchoolAge.HIGH.code);
            if (Objects.nonNull(normalHighList)){
                mergeList.addAll(normalHighList);
            }
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightGrade(statConclusionList, SchoolAge.HIGH.code);
            if (Objects.nonNull(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            return mergeList;
        }

        return getHeightAndWeightGrade(statConclusionList,schoolAge);
    }

    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeightGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> getHeightAndWeightSchoolAgeTable(list,grade,gradeList));
        getHeightAndWeightSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,gradeList);

        return gradeList;
    }

    private void getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList,String grade,List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(grade);

        gradeList.add(heightAndWeightMonitorTable);
    }

    /**
     * 体重身高监测结果-不同年龄段
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO();
        getHeightAndWeightAgeVariableVO(statConclusionList,ageVO);
        getHeightAndWeightAgeMonitorTableList(statConclusionList,ageVO);

        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }

    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getHeightAndWeightNum(age,list,heightAndWeightNumMap));

        DistrictHeightAndWeightMonitorVO.AgeRatio overweight = getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr);
        DistrictHeightAndWeightMonitorVO.AgeRatio obese = getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr);
        DistrictHeightAndWeightMonitorVO.AgeRatio stunting = getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr);
        DistrictHeightAndWeightMonitorVO.AgeRatio malnourished = getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr);

        DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO ageVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO();
        ageVariableVO.setOverweightRatio(overweight);
        ageVariableVO.setObeseRatio(obese);
        ageVariableVO.setStuntingRatio(stunting);
        ageVariableVO.setMalnourishedRatio(malnourished);

        ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
    }

    private DistrictHeightAndWeightMonitorVO.AgeRatio getAgeRatio(Map<Integer, HeightAndWeightNum> heightAndWeightNumMap, Function<HeightAndWeightNum,Integer> function,Function<HeightAndWeightNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = getMaxMap(heightAndWeightNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = getMinMap(heightAndWeightNumMap, function,mapper);
        DistrictHeightAndWeightMonitorVO.AgeRatio ageRatio = new DistrictHeightAndWeightMonitorVO.AgeRatio();
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
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getHeightAndWeightAgeTable(age,list,tableList));
        getHeightAndWeightAgeTable(1000,statConclusionList,tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable = buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }


    @Data
    private static class HeightAndWeightNum{

        /**
         * 筛查人数
         */
        private Integer validScreeningNum;
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


        //============= 不带% ============
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


        //============= 带% ============
        /**
         * 超重率
         */
        private String overweightRatioStr;
        /**
         * 肥胖率
         */
        private String obeseRatioStr;

        /**
         * 营养不良率
         */
        private String malnourishedRatioStr;

        /**
         * 生长迟缓率
         */
        private String stuntingRatioStr;

        /**
         * 性别
         */
        private Integer gender;


        public HeightAndWeightNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();

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

            return this;
        }

        /**
         * 不带%
         */
        public HeightAndWeightNum ratioNotSymbol(){
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

            return this;
        }

        /**
         * 带%
         */
        public HeightAndWeightNum ratio(){
            if (Objects.nonNull(overweightNum)){
                this.overweightRatioStr = MathUtil.ratio(overweightNum,validScreeningNum);
            }
            if (Objects.nonNull(obeseNum)){
                this.obeseRatioStr = MathUtil.ratio(obeseNum,validScreeningNum);
            }
            if (Objects.nonNull(stuntingNum)){
                this.stuntingRatioStr = MathUtil.ratio(stuntingNum,validScreeningNum);
            }
            if (Objects.nonNull(malnourishedNum)){
                this.malnourishedRatioStr = MathUtil.ratio(malnourishedNum,validScreeningNum);
            }
            return this;
        }

        public HeightAndWeightNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }
}
