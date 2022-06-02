package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolChartVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class SchoolHeightAndWeightMonitorService {

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();

    /**
     * 体重身高监测结果
     */
    public void getSchoolHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO = new SchoolHeightAndWeightMonitorVO();

        map.put(0,statConclusionList.size());
        //说明变量
        getHeightAndWeightMonitorVariableVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同性别
        getHeightAndWeightSexVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同年级
        getHeightAndWeightGradeVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同年龄段
        getHeightAndWeightAgeVO(statConclusionList,schoolHeightAndWeightMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolHeightAndWeightMonitorVO(schoolHeightAndWeightMonitorVO);
    }

    /**
     * 体重身高监测结果-说明变量
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightNum heightAndWeight = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();

        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO();
        heightAndWeightMonitorVariableVO.setOverweightRatio(heightAndWeight.overweightRatioStr);
        heightAndWeightMonitorVariableVO.setObeseRatio(heightAndWeight.obeseRatioStr);
        heightAndWeightMonitorVariableVO.setStuntingRatio(heightAndWeight.stuntingRatioStr);
        heightAndWeightMonitorVariableVO.setMalnourishedRatio(heightAndWeight.malnourishedRatioStr);

        schoolHeightAndWeightMonitorVO.setHeightAndWeightMonitorVariableVO(heightAndWeightMonitorVariableVO);
    }

    /**
     * 体重身高监测结果-不同性别
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList,heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList,heightAndWeightSexVO);
        getHeightAndWeightSexMonitorChart(statConclusionList,heightAndWeightSexVO);
        schoolHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    private void getHeightAndWeightSexMonitorChart(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolChartVO.Chart chart = new SchoolChartVO.Chart();
        List<String> x = Lists.newArrayList(ReportConst.OVERWEIGHT,ReportConst.OBESE,ReportConst.MALNOURISHED,ReportConst.STUNTING);
        List<SchoolChartVO.ChartData> y = Lists.newArrayList(
                new SchoolChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new SchoolChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        getChartData(genderMap.get(GenderEnum.MALE.type), y,0);
        getChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);
        chart.setX(x);
        chart.setY(y);
        heightAndWeightSexVO.setHeightAndWeightSexMonitorChart(chart);
    }
    private void getChartData(List<StatConclusion> statConclusionList,List<SchoolChartVO.ChartData> y,Integer index) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();
        y.get(index).getData().add(num.overweightRatio);
        y.get(index).getData().add(num.obeseRatio);
        y.get(index).getData().add(num.malnourishedRatio);
        y.get(index).getData().add(num.stuntingRatio);
    }

    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<HeightAndWeightNum> heightAndWeightSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getHeightAndWeightNum(gender,list,heightAndWeightSexList));

        if (heightAndWeightSexList.size() >= 1){
            SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO();
            heightAndWeightSexVariableVO.setOverweightRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getOverweightRatio, HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getObeseRatio, HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getStuntingRatio, HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getMalnourishedRatio, HeightAndWeightNum::getMalnourishedRatioStr));
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


    private SchoolHeightAndWeightMonitorVO.HeightAndWeightSex getRatioCompare(List<HeightAndWeightNum> heightAndWeightNumList, Function<HeightAndWeightNum,BigDecimal> function,Function<HeightAndWeightNum,String> mapper) {
        if (CollectionUtil.isEmpty(heightAndWeightNumList)){
            return null;
        }
        CollectionUtil.sort(heightAndWeightNumList, Comparator.comparing(function).reversed());
        SchoolHeightAndWeightMonitorVO.HeightAndWeightSex sex = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSex();
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

    private void setSexCompare(HeightAndWeightNum forward,HeightAndWeightNum back, Function<HeightAndWeightNum, String> mapper,
                               SchoolHeightAndWeightMonitorVO.HeightAndWeightSex sex,
                               String backSex,String zeroRatio) {

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

    private void setSymbol(SchoolHeightAndWeightMonitorVO.HeightAndWeightSex sex, String forwar, String back) {
        if (Objects.equals(forwar, back)){
            sex.setSymbol("=");
        }else {
            sex.setSymbol(">");
        }
    }

    /**
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable maleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable femaleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable totalTable = getHeightAndWeightSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setHeightAndWeightSexMonitorTableList(tableList);

    }
    private SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable getHeightAndWeightSexTable(List<StatConclusion> statConclusionList, Integer gender) {
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

        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        if (Objects.equals(10,gender)){
            heightAndWeightMonitorTable.setItemName("合计");
        }else {
            heightAndWeightMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return heightAndWeightMonitorTable;
    }

    public SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable buildTable(HeightAndWeightNum heightAndWeightNum){
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= new SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable();
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
    private void getHeightAndWeightGradeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO();
        getHeightAndWeightGradeVariableVO(statConclusionList,heightAndWeightGradeVO);
        getHeightAndWeightGradeMonitorTableList(statConclusionList,heightAndWeightGradeVO);
        getHeightAndWeightGradeMonitorChart(statConclusionList,heightAndWeightGradeVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightGradeVO(heightAndWeightGradeVO);

    }

    private void getHeightAndWeightGradeMonitorChart(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolChartVO.Chart chart = new SchoolChartVO.Chart();
        List<String> x = Lists.newArrayList();
        List<SchoolChartVO.ChartData> y = Lists.newArrayList(
                new SchoolChartVO.ChartData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new SchoolChartVO.ChartData(ReportConst.OBESE,Lists.newArrayList()),
                new SchoolChartVO.ChartData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new SchoolChartVO.ChartData(ReportConst.STUNTING,Lists.newArrayList())
        );
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            x.add(gradeCodeEnum.getName());
            HeightAndWeightNum num = new HeightAndWeightNum().build(list).ratioNotSymbol().ratio();
            setGradeData(y,num);
        });
        chart.setX(x);
        chart.setY(y);
        heightAndWeightGradeVO.setHeightAndWeightGradeMonitorChart(chart);
    }

    private void setGradeData(List<SchoolChartVO.ChartData> y, HeightAndWeightNum num){
        y.get(0).getData().add(num.overweightRatio);
        y.get(1).getData().add(num.obeseRatio);
        y.get(2).getData().add(num.malnourishedRatio);
        y.get(3).getData().add(num.stuntingRatio);
    }

    /**
     * 体重身高监测结果-不同学龄段-说明变量
     */
    private void getHeightAndWeightGradeVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVariableVO schoolAgeVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVariableVO();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getHeightAndWeightNum(gradeCodeEnum.getName(),list,heightAndWeightNumMap);
        });

        if (heightAndWeightNumMap.size() >= 2){
            schoolAgeVariableVO.setOverweightRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            schoolAgeVariableVO.setObeseRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            schoolAgeVariableVO.setStuntingRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            schoolAgeVariableVO.setMalnourishedRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            heightAndWeightGradeVO.setHeightAndWeightGradeVariableVO(schoolAgeVariableVO);
        }
    }


    private SchoolHeightAndWeightMonitorVO.GradeRatio getGradeRatio(Map<String, HeightAndWeightNum> heightAndWeightNumMap,Function<HeightAndWeightNum,Integer> function ,Function<HeightAndWeightNum,String> mapper){
        if (CollectionUtil.isEmpty(heightAndWeightNumMap)){
            return null;
        }
        SchoolHeightAndWeightMonitorVO.GradeRatio gradeRatio = new SchoolHeightAndWeightMonitorVO.GradeRatio();
        TwoTuple<String, String> maxTuple = ReportUtil.getMaxMap(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr);
        TwoTuple<String, String> minTuple = ReportUtil.getMinMap(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinGrade(minTuple.getFirst());
        gradeRatio.setMinRatio(minTuple.getSecond());
        return gradeRatio;
    }


    /**
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightGradeMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getHeightAndWeightGrade(statConclusionList,"合计",tableList);

        heightAndWeightGradeVO.setHeightAndWeightGradeMonitorTableList(tableList);
    }


    private void getHeightAndWeightGrade(List<StatConclusion> statConclusionList,String grade,List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(grade);
        gradeList.add(heightAndWeightMonitorTable);
    }

    /**
     * 体重身高监测结果-不同年龄段
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO();
        getHeightAndWeightAgeVariableVO(statConclusionList,ageVO);
        getHeightAndWeightAgeMonitorTableList(statConclusionList,ageVO);
        getHeightAndWeightAgeMonitorChart(statConclusionList,ageVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }

    private void getHeightAndWeightAgeMonitorChart(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolChartVO.AgeChart ageChart = new SchoolChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);
        List<String> y = Lists.newArrayList();
        List<SchoolChartVO.AgeData> x = Lists.newArrayList(
                new SchoolChartVO.AgeData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new SchoolChartVO.AgeData(ReportConst.OBESE,Lists.newArrayList()),
                new SchoolChartVO.AgeData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new SchoolChartVO.AgeData(ReportConst.STUNTING,Lists.newArrayList())
        );
        dynamicAgeSegmentList.forEach(age-> {
            y.add(AgeSegmentEnum.get(age).getDesc());
            HeightAndWeightNum num = new HeightAndWeightNum().build(ageMap.get(age)).ratioNotSymbol().ratio();
            setAgeData(x,num);
        });
        ageChart.setX(x);
        ageChart.setY(y);
        ageVO.setHeightAndWeightAgeMonitorChart(ageChart);
    }

    private void setAgeData(List<SchoolChartVO.AgeData> data, HeightAndWeightNum num){
        data.get(0).getData().add(num.overweightRatio);
        data.get(1).getData().add(num.obeseRatio);
        data.get(2).getData().add(num.malnourishedRatio);
        data.get(3).getData().add(num.stuntingRatio);
    }

    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getHeightAndWeightNum(age,list,heightAndWeightNumMap));

        if (heightAndWeightNumMap.size() >= 2){
            SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO ageVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO();
            ageVariableVO.setOverweightRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            ageVariableVO.setObeseRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            ageVariableVO.setStuntingRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            ageVariableVO.setMalnourishedRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
        }

    }

    private SchoolHeightAndWeightMonitorVO.AgeRatio getAgeRatio(Map<Integer, HeightAndWeightNum> heightAndWeightNumMap, Function<HeightAndWeightNum,Integer> function,Function<HeightAndWeightNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(heightAndWeightNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(heightAndWeightNumMap, function,mapper);
        SchoolHeightAndWeightMonitorVO.AgeRatio ageRatio = new SchoolHeightAndWeightMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
        return ageRatio;
    }


    /**
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getHeightAndWeightAgeTable(age,list,tableList));
        getHeightAndWeightAgeTable(1000,statConclusionList,tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList) {
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
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable = buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }


    @EqualsAndHashCode(callSuper = true)
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
            this.overweightRatio = getRatioNotSymbol(overweightNum,getTotal());
            this.obeseRatio = getRatioNotSymbol(obeseNum,getTotal());
            this.stuntingRatio = getRatioNotSymbol(stuntingNum,getTotal());
            this.malnourishedRatio = getRatioNotSymbol(malnourishedNum,getTotal());
            return this;
        }

        /**
         * 带%
         */
        public HeightAndWeightNum ratio(){
            this.overweightRatioStr = getRatio(overweightNum,getTotal());
            this.obeseRatioStr = getRatio(obeseNum,getTotal());
            this.stuntingRatioStr = getRatio(stuntingNum,getTotal());
            this.malnourishedRatioStr = getRatio(malnourishedNum,getTotal());
            return this;
        }

        public HeightAndWeightNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }

    private static Integer getTotal(){
        return map.get(0);
    }
}
