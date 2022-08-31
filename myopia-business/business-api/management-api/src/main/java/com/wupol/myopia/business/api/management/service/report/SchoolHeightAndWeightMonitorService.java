package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
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

        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO = new SchoolHeightAndWeightMonitorVO();

        HeightAndWeightNum.MAP.put(0, statConclusionList.size());
        //说明变量
        getHeightAndWeightMonitorVariableVO(statConclusionList, schoolHeightAndWeightMonitorVO);
        //不同性别
        getHeightAndWeightSexVO(statConclusionList, schoolHeightAndWeightMonitorVO);
        //不同年级
        getHeightAndWeightGradeVO(statConclusionList, schoolHeightAndWeightMonitorVO);
        //不同年龄段
        getHeightAndWeightAgeVO(statConclusionList, schoolHeightAndWeightMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolHeightAndWeightMonitorVO(schoolHeightAndWeightMonitorVO);
    }

    /**
     * 体重身高监测结果-说明变量
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratio()
                .buildHeightAndWeightMonitorVariableVO();
        schoolHeightAndWeightMonitorVO.setHeightAndWeightMonitorVariableVO(heightAndWeightMonitorVariableVO);
    }

    /**
     * 体重身高监测结果-不同性别
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightSexVO heightAndWeightSexVO = new HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList, heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList, heightAndWeightSexVO);
        reportChartService.getSexMonitorChart(statConclusionList, heightAndWeightSexVO);
        schoolHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }


    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<HeightAndWeightNum> heightAndWeightSexList = Lists.newArrayList();
        genderMap.forEach((gender, list) -> getHeightAndWeightNum(gender, list, heightAndWeightSexList));

        if (!heightAndWeightSexList.isEmpty()) {
            HeightAndWeightSexVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new HeightAndWeightSexVO.HeightAndWeightSexVariableVO();
            heightAndWeightSexVariableVO.setOverweightRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getOverweightRatio, HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getObeseRatio, HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getStuntingRatio, HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(ReportUtil.getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getMalnourishedRatio, HeightAndWeightNum::getMalnourishedRatioStr));
            heightAndWeightSexVO.setHeightAndWeightSexVariableVO(heightAndWeightSexVariableVO);
        }

    }

    private void getHeightAndWeightNum(Integer gender, List<StatConclusion> statConclusionList, List<HeightAndWeightNum> heightAndWeightSexList) {
        HeightAndWeightNum build = new HeightAndWeightNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightSexList.add(build);
    }


    /**
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightSexVO sexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        HeightAndWeightMonitorTable maleTable = getHeightAndWeightSexTable(statConclusionList, GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)) {
            tableList.add(maleTable);
        }
        HeightAndWeightMonitorTable femaleTable = getHeightAndWeightSexTable(statConclusionList, GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)) {
            tableList.add(femaleTable);
        }
        HeightAndWeightMonitorTable totalTable = getHeightAndWeightSexTable(statConclusionList, 10);
        if (Objects.nonNull(totalTable)) {
            tableList.add(totalTable);
        }
        sexVO.setHeightAndWeightSexMonitorTableList(tableList);

    }

    private HeightAndWeightMonitorTable getHeightAndWeightSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10, gender)) {
            conclusionlist = statConclusionList;
        } else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol().buildTable();
        if (Objects.equals(10, gender)) {
            heightAndWeightMonitorTable.setItemName(ReportConst.TOTAL);
        } else {
            heightAndWeightMonitorTable.setItemName(GenderEnum.getName(gender)+ReportConst.SEX);
        }
        return heightAndWeightMonitorTable;
    }

    /**
     * 体重身高监测结果-不同学龄段
     */
    private void getHeightAndWeightGradeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightGradeVO heightAndWeightGradeVO = new HeightAndWeightGradeVO();
        getHeightAndWeightGradeVariableVO(statConclusionList, heightAndWeightGradeVO);
        getHeightAndWeightGradeMonitorTableList(statConclusionList, heightAndWeightGradeVO);
        reportChartService.getGradeMonitorChart(statConclusionList, heightAndWeightGradeVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightGradeVO(heightAndWeightGradeVO);

    }


    /**
     * 体重身高监测结果-不同学龄段-说明变量
     */
    private void getHeightAndWeightGradeVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        HeightAndWeightGradeVO.HeightAndWeightGradeVariableVO schoolAgeVariableVO = new HeightAndWeightGradeVO.HeightAndWeightGradeVariableVO();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> ReportUtil.getHeightAndWeightNum(ReportUtil.getItemName(gradeCode,SchoolAge.VOCATIONAL_HIGH.code), list, heightAndWeightNumMap));

        if (heightAndWeightNumMap.size() >= 2) {
            schoolAgeVariableVO.setOverweightRatio(ReportUtil.getSchoolGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            schoolAgeVariableVO.setObeseRatio(ReportUtil.getSchoolGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            schoolAgeVariableVO.setStuntingRatio(ReportUtil.getSchoolGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            schoolAgeVariableVO.setMalnourishedRatio(ReportUtil.getSchoolGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            heightAndWeightGradeVO.setHeightAndWeightGradeVariableVO(schoolAgeVariableVO);
        }
    }


    /**
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightGradeMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();

        List<HeightAndWeightMonitorTable> primaryList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code);
        if (CollUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<HeightAndWeightMonitorTable> juniorList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code);
        if (CollUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }
        List<HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.HIGH.code);
        if (CollUtil.isNotEmpty(normalHighList)) {
            tableList.addAll(normalHighList);
        }
        List<HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollUtil.isNotEmpty(vocationalHighList)) {
            tableList.addAll(vocationalHighList);
        }
        List<HeightAndWeightMonitorTable> universityList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (CollUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }

        getHeightAndWeightGrade(statConclusionList,ReportConst.TOTAL,tableList);
        heightAndWeightGradeVO.setHeightAndWeightGradeMonitorTableList(tableList);
    }

    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }

        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollUtil.sort(gradeCodeMap, String::compareTo);
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> getHeightAndWeightGrade(list, ReportUtil.getItemName(grade,schoolAge), tableList));
        getHeightAndWeightGrade(conclusionList, ReportUtil.getItemNameTotal(schoolAge), tableList);

        return tableList;
    }


    private void getHeightAndWeightGrade(List<StatConclusion> statConclusionList, String grade, List<HeightAndWeightMonitorTable> gradeList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(grade);
        gradeList.add(heightAndWeightMonitorTable);
    }

    /**
     * 体重身高监测结果-不同年龄段
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightAgeVO ageVO = new HeightAndWeightAgeVO();
        getHeightAndWeightAgeVariableVO(statConclusionList, ageVO);
        getHeightAndWeightAgeMonitorTableList(statConclusionList, ageVO);
        reportChartService.getAgeMonitorChart(statConclusionList, ageVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }


    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightAgeVO ageVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        ageMap.forEach((age, list) -> ReportUtil.getHeightAndWeightNum(age, list, heightAndWeightNumMap));

        if (heightAndWeightNumMap.size() >= 2) {
            HeightAndWeightAgeVO.HeightAndWeightAgeVariableVO ageVariableVO = new HeightAndWeightAgeVO.HeightAndWeightAgeVariableVO();
            ageVariableVO.setOverweightRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            ageVariableVO.setObeseRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            ageVariableVO.setStuntingRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            ageVariableVO.setMalnourishedRatio(ReportUtil.getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
        }

    }


    /**
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightAgeVO ageVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()), TreeMap::new, Collectors.toList()));
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list) -> getHeightAndWeightAgeTable(age, list, tableList));
        getHeightAndWeightAgeTable(1000, statConclusionList, tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<HeightAndWeightMonitorTable> tableList) {
        if (CollUtil.isEmpty(conclusionlist)) {
            return;
        }

        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }

}
