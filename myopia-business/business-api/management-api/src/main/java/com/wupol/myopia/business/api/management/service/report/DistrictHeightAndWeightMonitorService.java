package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictHeightAndWeightMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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
public class DistrictHeightAndWeightMonitorService {

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
        HeightAndWeightNum heightAndWeight = new HeightAndWeightNum().build(statConclusionList).ratio().ratioNotSymbol();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO();
        heightAndWeightMonitorVariableVO.setOverweightRatio(heightAndWeight.overweightRatioStr);
        heightAndWeightMonitorVariableVO.setObeseRatio(heightAndWeight.obeseRatioStr);
        heightAndWeightMonitorVariableVO.setStuntingRatio(heightAndWeight.stuntingRatioStr);
        heightAndWeightMonitorVariableVO.setMalnourishedRatio(heightAndWeight.malnourishedRatioStr);

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

        if (heightAndWeightSexList.size() >= 1){
            DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO();
            heightAndWeightSexVariableVO.setOverweightRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr));

            heightAndWeightSexVO.setHeightAndWeightSexVariableVO(heightAndWeightSexVariableVO);
        }
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
        if (heightAndWeightNumList.size() == 1){
            HeightAndWeightNum num = heightAndWeightNumList.get(0);
            if (Objects.equals(GenderEnum.MALE.type,num.gender)){
                setSexCompare(num,null, mapper, sex,GenderEnum.FEMALE.desc,ReportConst.ZERO_RATIO_STR);
            }else {
                setSexCompare(num,null, mapper, sex,GenderEnum.MALE.desc,ReportConst.ZERO_RATIO_STR);
            }
        }
        if (heightAndWeightNumList.size() == 2){
            HeightAndWeightNum forward = heightAndWeightNumList.get(0);
            HeightAndWeightNum back = heightAndWeightNumList.get(1);
            setSexCompare(forward,back, mapper, sex,null,null);
        }
        return sex;
    }

    private void setSexCompare(HeightAndWeightNum forward, HeightAndWeightNum back, Function<HeightAndWeightNum, String> mapper,
                               DistrictHeightAndWeightMonitorVO.HeightAndWeightSex sex,
                               String backSex, String zeroRatio) {

        String forwardRatio = mapper.apply(forward);
        sex.setForwardSex(GenderEnum.getName(forward.gender));
        sex.setForwardRatio(forwardRatio);

        if (Objects.nonNull(back)){
            String backRatio = mapper.apply(back);
            sex.setBackSex(GenderEnum.getName(back.gender));
            sex.setBackRatio(backRatio);
            setSymbol(sex,forwardRatio,backRatio);
        }else {
            sex.setBackSex(backSex);
            sex.setBackRatio(zeroRatio);
            setSymbol(sex,forwardRatio,zeroRatio);
        }
    }

    private void setSymbol(DistrictHeightAndWeightMonitorVO.HeightAndWeightSex sex, String forwar, String back) {
        if (Objects.equals(forwar, back)){
            sex.setSymbol("=");
        }else {
            sex.setSymbol(">");
        }
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

        return getHeightAndWeightSchoolAge(conclusionMap.get(schoolAge));

    }

    private DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge getHeightAndWeightSchoolAge(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge heightAndWeightSchoolAge = new DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge();
        heightAndWeightSchoolAge.setOverweightRatio(heightAndWeightNum.overweightRatioStr);
        heightAndWeightSchoolAge.setObeseRatio(heightAndWeightNum.obeseRatioStr);
        heightAndWeightSchoolAge.setStuntingRatio(heightAndWeightNum.stuntingRatioStr);
        heightAndWeightSchoolAge.setMalnourishedRatio(heightAndWeightNum.malnourishedRatioStr);

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getHeightAndWeightNum(gradeCodeEnum.getName(),list,heightAndWeightNumMap);
        });

        if (heightAndWeightNumMap.size() >= 2){
            heightAndWeightSchoolAge.setMaxOverweightRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSchoolAge.setMaxObeseRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSchoolAge.setMaxStuntingRatio(getGradeRatio(heightAndWeightNumMap,HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSchoolAge.setMaxMalnourishedRatio(getGradeRatio(heightAndWeightNumMap,HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr));
        }

        return heightAndWeightSchoolAge;
    }

    private DistrictHeightAndWeightMonitorVO.GradeRatio getGradeRatio(Map<String, HeightAndWeightNum> heightAndWeightNumMap,Function<HeightAndWeightNum,Integer> function ,Function<HeightAndWeightNum,String> mapper){
        if (CollectionUtil.isEmpty(heightAndWeightNumMap)){
            return null;
        }
        DistrictHeightAndWeightMonitorVO.GradeRatio gradeRatio = new DistrictHeightAndWeightMonitorVO.GradeRatio();
        TwoTuple<String, String> tuple = getMaxMap(heightAndWeightNumMap, function,mapper);
        gradeRatio.setGrade(tuple.getFirst());
        gradeRatio.setRatio(tuple.getSecond());
        return gradeRatio;
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
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> juniorList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> highList = getHeightAndWeighSchoolAgeTable(statConclusionList,10);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> normalHighList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeighSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
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

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> universityList = getHeightAndWeighSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }

    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeighSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                mergeList.addAll(normalHighList);
            }
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightGrade(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            return mergeList;
        }

        return getHeightAndWeightGrade(statConclusionList,schoolAge);
    }

    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeightGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightSchoolAgeTable(list,gradeCodeEnum.getName(),gradeList);
        });
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

        if (heightAndWeightNumMap.size() >= 2){
            DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO ageVariableVO = new DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO();
            ageVariableVO.setOverweightRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr));
            ageVariableVO.setObeseRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr));
            ageVariableVO.setStuntingRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr));
            ageVariableVO.setMalnourishedRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr));

            ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
        }

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
    private static class HeightAndWeightNum extends EntityFunction{

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
            this.overweightNum =getCount(statConclusionList,StatConclusion::getIsOverweight);
            this.obeseNum = getCount(statConclusionList,StatConclusion::getIsObesity);
            this.malnourishedNum = getCount(statConclusionList,StatConclusion::getIsMalnutrition);
            this.stuntingNum = getCount(statConclusionList,StatConclusion::getIsStunting);
            return this;
        }

        /**
         * 不带%
         */
        public HeightAndWeightNum ratioNotSymbol(){
            this.overweightRatio = getRatioNotSymbol(overweightNum,validScreeningNum);
            this.obeseRatio = getRatioNotSymbol(obeseNum,validScreeningNum);
            this.stuntingRatio = getRatioNotSymbol(stuntingNum,validScreeningNum);
            this.malnourishedRatio = getRatioNotSymbol(malnourishedNum,validScreeningNum);
            return this;
        }

        /**
         * 带%
         */
        public HeightAndWeightNum ratio(){
            this.overweightRatioStr = getRatio(overweightNum,validScreeningNum);
            this.obeseRatioStr = getRatio(obeseNum,validScreeningNum);
            this.stuntingRatioStr = getRatio(stuntingNum,validScreeningNum);
            this.malnourishedRatioStr = getRatio(malnourishedNum,validScreeningNum);
            return this;
        }



        public HeightAndWeightNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }
}
