package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.api.management.domain.vo.report.ChartVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class SchoolHeightAndWeightMonitorService {

    @Autowired
    private ReportChartService reportChartService;


    /**
     * 体重身高监测结果
     */
    public void getSchoolHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO = new SchoolHeightAndWeightMonitorVO();

        HeightAndWeightNum.MAP.put(0,statConclusionList.size());
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
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new HeightAndWeightNum()
                .build(statConclusionList).ratio().buildSchoolHeightAndWeightMonitorVariableVO();
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
        reportChartService.getSexMonitorChart(statConclusionList,heightAndWeightSexVO);
        schoolHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
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


    private SexCompare getRatioCompare(List<HeightAndWeightNum> heightAndWeightNumList, Function<HeightAndWeightNum,BigDecimal> function,Function<HeightAndWeightNum,String> mapper) {
        if (CollectionUtil.isEmpty(heightAndWeightNumList)){
            return null;
        }
        CollectionUtil.sort(heightAndWeightNumList, Comparator.comparing(function).reversed());
        SexCompare sex = new SexCompare();
        if (heightAndWeightNumList.size() == 1){
            HeightAndWeightNum num = heightAndWeightNumList.get(0);
            if (Objects.equals(GenderEnum.MALE.type,num.getGender())){
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
                               SexCompare sex,
                               String backSex,String zeroRatio) {

        String forwardRatio = mapper.apply(forward);
        sex.setForwardSex(GenderEnum.getName(forward.getGender()));
        sex.setForwardRatio(forwardRatio);

        if (Objects.nonNull(back)){
            String backRatio = mapper.apply(back);
            sex.setBackSex(GenderEnum.getName(back.getGender()));
            sex.setBackRatio(backRatio);
            ReportUtil.setSymbol(sex,forwardRatio,backRatio);
        }else {
            sex.setBackSex(backSex);
            sex.setBackRatio(zeroRatio);
            ReportUtil.setSymbol(sex,forwardRatio,zeroRatio);
        }
    }


    /**
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO sexVO) {
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
        ChartVO.Chart chart = new ChartVO.Chart();
        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList(
                new ChartVO.ChartData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.OBESE,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.STUNTING,Lists.newArrayList())
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

    private void setGradeData(List<ChartVO.ChartData> y, HeightAndWeightNum num){
        y.get(0).getData().add(num.getOverweightRatio());
        y.get(1).getData().add(num.getObeseRatio());
        y.get(2).getData().add(num.getMalnourishedRatio());
        y.get(3).getData().add(num.getStuntingRatio());
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
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getHeightAndWeightGrade(statConclusionList,"合计",tableList);

        heightAndWeightGradeVO.setHeightAndWeightGradeMonitorTableList(tableList);
    }


    private void getHeightAndWeightGrade(List<StatConclusion> statConclusionList,String grade,List<HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable= new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().buildTable();
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
        reportChartService.getAgeMonitorChart(statConclusionList,ageVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
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
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age->getHeightAndWeightAgeTable(age,ageMap.get(age),tableList));
        getHeightAndWeightAgeTable(1000,statConclusionList,tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<HeightAndWeightMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable =new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }

}
