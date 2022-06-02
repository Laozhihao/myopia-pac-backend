package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictHeightAndWeightMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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
public class DistrictHeightAndWeightMonitorService {

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();


    /**
     * 体重身高监测结果
     */
    public void getDistrictHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO = new DistrictHeightAndWeightMonitorVO();
        map.put(0,statConclusionList.size());
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
        getHeightAndWeightSexMonitorChart(statConclusionList,heightAndWeightSexVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    private void getHeightAndWeightSexMonitorChart(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictChartVO.Chart chart = new DistrictChartVO.Chart();
        List<String> x = Lists.newArrayList(ReportConst.OVERWEIGHT,ReportConst.OBESE,ReportConst.MALNOURISHED,ReportConst.STUNTING);
        List<DistrictChartVO.ChartData> y = Lists.newArrayList(
                new DistrictChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new DistrictChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        getChartData(genderMap.get(GenderEnum.MALE.type), y,0);
        getChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);
        chart.setX(x);
        chart.setY(y);
        heightAndWeightSexVO.setHeightAndWeightSexMonitorChart(chart);
    }

    private void getChartData(List<StatConclusion> statConclusionList,List<DistrictChartVO.ChartData> y,Integer index) {
        HeightAndWeightNum num = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().ratio();
        y.get(index).getData().add(num.overweightRatio);
        y.get(index).getData().add(num.obeseRatio);
        y.get(index).getData().add(num.malnourishedRatio);
        y.get(index).getData().add(num.stuntingRatio);
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
            heightAndWeightSexVariableVO.setOverweightRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getOverweightRatio,HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getObeseRatio,HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getStuntingRatio,HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(getRatioCompare(heightAndWeightSexList,HeightAndWeightNum::getMalnourishedRatio,HeightAndWeightNum::getMalnourishedRatioStr));

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


    private DistrictHeightAndWeightMonitorVO.HeightAndWeightSex getRatioCompare(List<HeightAndWeightNum> heightAndWeightNumList, Function<HeightAndWeightNum,BigDecimal> function,Function<HeightAndWeightNum,String> mapper) {
        if (CollectionUtil.isEmpty(heightAndWeightNumList)){
            return null;
        }
        CollectionUtil.sort(heightAndWeightNumList, Comparator.comparing(function).reversed());
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
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> primaryList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> juniorList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList,10,"高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }else {
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList,SchoolAge.HIGH.code,"高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }

        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> universityList = getHeightAndWeightSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }
    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName) {
        if (Objects.equals(schoolAge,10)){
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList,itemName,mergeList);
            return mergeList;
        }
        List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> heightAndWeightGradeList = getHeightAndWeightGrade(statConclusionList, schoolAge);
        if (CollectionUtil.isNotEmpty(heightAndWeightGradeList)){
            heightAndWeightGradeList.get(heightAndWeightGradeList.size()-1).setItemName(itemName);
        }
        return heightAndWeightGradeList;
    }


    private List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList,"高中",mergeList);
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
        getHeightAndWeightAgeMonitorChart(statConclusionList,ageVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }

    private void getHeightAndWeightAgeMonitorChart(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictChartVO.AgeChart ageChart = new DistrictChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);
        List<String> y = Lists.newArrayList();
        List<DistrictChartVO.AgeData> x = Lists.newArrayList(
                new DistrictChartVO.AgeData(ReportConst.OVERWEIGHT,Lists.newArrayList()),
                new DistrictChartVO.AgeData(ReportConst.OBESE,Lists.newArrayList()),
                new DistrictChartVO.AgeData(ReportConst.MALNOURISHED,Lists.newArrayList()),
                new DistrictChartVO.AgeData(ReportConst.STUNTING,Lists.newArrayList())
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
    private void setAgeData(List<DistrictChartVO.AgeData> data, HeightAndWeightNum num){
        data.get(0).getData().add(num.overweightRatio);
        data.get(1).getData().add(num.obeseRatio);
        data.get(2).getData().add(num.malnourishedRatio);
        data.get(3).getData().add(num.stuntingRatio);
    }

    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
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
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(heightAndWeightNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(heightAndWeightNumMap, function,mapper);
        DistrictHeightAndWeightMonitorVO.AgeRatio ageRatio = new DistrictHeightAndWeightMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
        return ageRatio;
    }

    /**
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
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
