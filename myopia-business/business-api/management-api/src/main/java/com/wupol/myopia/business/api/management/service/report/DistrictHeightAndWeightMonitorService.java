package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ReportChartService reportChartService;

    /**
     * 体重身高监测结果
     */
    public void getDistrictHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO = new DistrictHeightAndWeightMonitorVO();
        HeightAndWeightNum.MAP.put(0,statConclusionList.size());
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
        HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratio()
                .buildHeightAndWeightMonitorVariableVO();
        districtHeightAndWeightMonitorVO.setHeightAndWeightMonitorVariableVO(heightAndWeightMonitorVariableVO);
    }

    /**
     * 体重身高监测结果-不同性别
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightSexVO heightAndWeightSexVO = new HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList,heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList,heightAndWeightSexVO);
        reportChartService.getSexMonitorChart(statConclusionList,heightAndWeightSexVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<HeightAndWeightNum> heightAndWeightSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getHeightAndWeightNum(gender,list,heightAndWeightSexList));

        if (heightAndWeightSexList.size() >= 1){
            HeightAndWeightSexVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new HeightAndWeightSexVO.HeightAndWeightSexVariableVO();
            heightAndWeightSexVariableVO.setOverweightRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getOverweightRatio,HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getObeseRatio,HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getStuntingRatio,HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getMalnourishedRatio,HeightAndWeightNum::getMalnourishedRatioStr));

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


    /**
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        HeightAndWeightMonitorTable maleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        HeightAndWeightMonitorTable femaleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        HeightAndWeightMonitorTable totalTable = getHeightAndWeightSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setHeightAndWeightSexMonitorTableList(tableList);

    }
    private HeightAndWeightMonitorTable getHeightAndWeightSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        HeightAndWeightMonitorTable heightAndWeightMonitorTable= new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol().buildTable();
        if (Objects.equals(10,gender)){
            heightAndWeightMonitorTable.setItemName("合计");
        }else {
            heightAndWeightMonitorTable.setItemName(GenderEnum.getName(gender));
        }
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
        getHeightAndWeightSchoolAgeMonitorChart(statConclusionList,heightAndWeightSchoolAgeVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSchoolAgeVO(heightAndWeightSchoolAgeVO);

    }

    private void getHeightAndWeightSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictChartVO.Chart chart = new DistrictChartVO.Chart();
        List<String> x = Lists.newArrayList();
        List<DistrictChartVO.ChartData> y = Lists.newArrayList(
                new DistrictChartVO.ChartData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new DistrictChartVO.ChartData(ReportConst.OBESE,Lists.newArrayList()),
                new DistrictChartVO.ChartData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new DistrictChartVO.ChartData(ReportConst.STUNTING,Lists.newArrayList())
        );

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge primary = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primary)){
            x.add(SchoolAge.PRIMARY.desc);
            setSchoolAgeData(y,primary);
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge junior = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.JUNIOR.code);
        if (Objects.nonNull(junior)){
            x.add(SchoolAge.JUNIOR.desc);
            setSchoolAgeData(y,junior);
        }

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge normalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.HIGH.code);
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge vocationalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(normalHigh) || Objects.nonNull(vocationalHigh)){
            x.add("高中");
            DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge high = getHeightAndWeightSchoolSchoolAge(statConclusionList,10);
            setSchoolAgeData(y,high);
        }
        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge university = getHeightAndWeightSchoolSchoolAge(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(university)){
            x.add(SchoolAge.UNIVERSITY.desc);
            setSchoolAgeData(y,university);
        }
        chart.setX(x);
        chart.setY(y);

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorChart(chart);

    }


    private void setSchoolAgeData(List<DistrictChartVO.ChartData> y, DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge schoolAge) {
        y.get(0).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getOverweightRatio()));
        y.get(1).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getObeseRatio()));
        y.get(2).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getMalnourishedRatio()));
        y.get(3).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getStuntingRatio()));
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

        DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAge heightAndWeightSchoolAge = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratio()
                .buildHeightAndWeightSchoolAge();

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
        TwoTuple<String, String> tuple = ReportUtil.getMaxMap(heightAndWeightNumMap, function,mapper);
        gradeRatio.setGrade(tuple.getFirst());
        gradeRatio.setRatio(tuple.getSecond());
        return gradeRatio;
    }


    /**
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        List<HeightAndWeightMonitorTable> primaryList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<HeightAndWeightMonitorTable> juniorList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }

        List<HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            List<HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
            List<HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList,10,"高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }else {
            List<HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList,SchoolAge.HIGH.code,"高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }

        List<HeightAndWeightMonitorTable> universityList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }
    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName) {
        if (Objects.equals(schoolAge,10)){
            List<HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList,itemName,mergeList);
            return mergeList;
        }
        List<HeightAndWeightMonitorTable> heightAndWeightGradeList = getHeightAndWeightGrade(statConclusionList, schoolAge);
        if (CollectionUtil.isNotEmpty(heightAndWeightGradeList)){
            heightAndWeightGradeList.get(heightAndWeightGradeList.size()-1).setItemName(itemName);
        }
        return heightAndWeightGradeList;
    }


    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge,10)){
            List<HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList,"高中",mergeList);
            return mergeList;
        }

        return getHeightAndWeightGrade(statConclusionList,schoolAge);
    }

    private List<HeightAndWeightMonitorTable> getHeightAndWeightGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<HeightAndWeightMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightSchoolAgeTable(list,gradeCodeEnum.getName(),gradeList);
        });
        getHeightAndWeightSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,gradeList);

        return gradeList;
    }

    private void getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList,String grade,List<HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable= new HeightAndWeightNum()
                .build(statConclusionList)
                .ratioNotSymbol().buildTable();
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
        HeightAndWeightAgeVO ageVO = new HeightAndWeightAgeVO();
        getHeightAndWeightAgeVariableVO(statConclusionList,ageVO);
        getHeightAndWeightAgeMonitorTableList(statConclusionList,ageVO);
        reportChartService.getAgeMonitorChart(statConclusionList,ageVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }


    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getHeightAndWeightNum(age,list,heightAndWeightNumMap));

        if (heightAndWeightNumMap.size() >= 2){
            HeightAndWeightAgeVO.HeightAndWeightAgeVariableVO ageVariableVO = new HeightAndWeightAgeVO.HeightAndWeightAgeVariableVO();
            ageVariableVO.setOverweightRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum,HeightAndWeightNum::getOverweightRatioStr));
            ageVariableVO.setObeseRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum,HeightAndWeightNum::getObeseRatioStr));
            ageVariableVO.setStuntingRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum,HeightAndWeightNum::getStuntingRatioStr));
            ageVariableVO.setMalnourishedRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum,HeightAndWeightNum::getMalnourishedRatioStr));

            ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
        }

    }

    /**
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age->getHeightAndWeightAgeTable(age,ageMap.get(age),tableList));
        getHeightAndWeightAgeTable(1000,statConclusionList,tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<HeightAndWeightMonitorTable> tableList) {
        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum()
                .build(conclusionlist)
                .ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }
}
