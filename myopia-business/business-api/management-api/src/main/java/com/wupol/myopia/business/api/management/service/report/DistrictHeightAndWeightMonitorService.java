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

import java.util.*;
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

        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO = new DistrictHeightAndWeightMonitorVO();
        HeightAndWeightNum.MAP.put(0, statConclusionList.size());
        //说明变量
        getHeightAndWeightMonitorVariableVO(statConclusionList, districtHeightAndWeightMonitorVO);
        //不同性别
        getHeightAndWeightSexVO(statConclusionList, districtHeightAndWeightMonitorVO);
        //不同学龄段
        getHeightAndWeightSchoolAgeVO(statConclusionList, districtHeightAndWeightMonitorVO);
        //不同年龄段
        getHeightAndWeightAgeVO(statConclusionList, districtHeightAndWeightMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictHeightAndWeightMonitorVO(districtHeightAndWeightMonitorVO);
    }

    /**
     * 体重身高监测结果-说明变量
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightSexVO heightAndWeightSexVO = new HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList, heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList, heightAndWeightSexVO);
        reportChartService.getSexMonitorChart(statConclusionList, heightAndWeightSexVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
    private void getHeightAndWeightSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO = new HeightAndWeightSchoolAgeVO();
        getHeightAndWeightSchoolAgeVariableVO(statConclusionList, heightAndWeightSchoolAgeVO);
        getHeightAndWeightSchoolAgeMonitorTableList(statConclusionList, heightAndWeightSchoolAgeVO);
        getHeightAndWeightSchoolAgeMonitorChart(statConclusionList, heightAndWeightSchoolAgeVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSchoolAgeVO(heightAndWeightSchoolAgeVO);

    }

    private void getHeightAndWeightSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = getData(statConclusionList);
        if (CollectionUtil.isNotEmpty(tupleList)) {
            reportChartService.getSchoolAgeMonitorChart(tupleList, heightAndWeightSchoolAgeVO);
        }

    }


    private List<TwoTuple<String, SchoolAgeRatioVO>> getData(List<StatConclusion> statConclusionList) {
        if (ReportUtil.getSchoolGrade(statConclusionList)) {
            return Collections.emptyList();
        }

        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = Lists.newArrayList();

        HeightAndWeightSchoolAge primary = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primary)) {
            tupleList.add(TwoTuple.of(SchoolAge.PRIMARY.desc, primary));
        }
        HeightAndWeightSchoolAge junior = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        if (Objects.nonNull(junior)) {
            tupleList.add(TwoTuple.of(SchoolAge.JUNIOR.desc, junior));
        }

        HeightAndWeightSchoolAge normalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        HeightAndWeightSchoolAge vocationalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(normalHigh) || Objects.nonNull(vocationalHigh)) {
            HeightAndWeightSchoolAge high = getHeightAndWeightSchoolSchoolAge(statConclusionList, 10);
            tupleList.add(TwoTuple.of(ReportConst.HIGH, high));
        }
        HeightAndWeightSchoolAge university = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(university)) {
            tupleList.add(TwoTuple.of(SchoolAge.UNIVERSITY.desc, university));
        }
        return tupleList;
    }

    /**
     * 体重身高监测结果-不同学龄段-说明变量
     */
    private void getHeightAndWeightSchoolAgeVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        HeightAndWeightSchoolAgeVO.HeightAndWeightSchoolAgeVariableVO schoolAgeVariableVO = new HeightAndWeightSchoolAgeVO.HeightAndWeightSchoolAgeVariableVO();

        HeightAndWeightSchoolAge primary = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        HeightAndWeightSchoolAge junior = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        HeightAndWeightSchoolAge high = getHeightAndWeightSchoolSchoolAge(statConclusionList, 10);
        HeightAndWeightSchoolAge normalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        HeightAndWeightSchoolAge vocationalHigh = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        HeightAndWeightSchoolAge university = getHeightAndWeightSchoolSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);

        schoolAgeVariableVO.setPrimarySchool(primary);
        schoolAgeVariableVO.setJuniorHighSchool(junior);
        schoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)) {
            schoolAgeVariableVO.setHighSchool(high);
            schoolAgeVariableVO.setNormalHighSchool(normalHigh);
            schoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        } else {
            schoolAgeVariableVO.setHighSchool(high);
        }
        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    private HeightAndWeightSchoolAge getHeightAndWeightSchoolSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return null;
        }

        List<StatConclusion> conclusionList;
        if (Objects.equals(schoolAge, 10)) {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(SchoolAge.HIGH.code, sc.getSchoolAge()) || Objects.equals(SchoolAge.VOCATIONAL_HIGH.code, sc.getSchoolAge())).collect(Collectors.toList());
        }else {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        }
        return getHeightAndWeightSchoolAge(conclusionList);

    }

    private HeightAndWeightSchoolAge getHeightAndWeightSchoolAge(List<StatConclusion> statConclusionList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return null;
        }

        HeightAndWeightSchoolAge heightAndWeightSchoolAge = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratio()
                .buildHeightAndWeightSchoolAge();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            ReportUtil.getHeightAndWeightNum(gradeCodeEnum.getName(), list, heightAndWeightNumMap);
        });

        if (heightAndWeightNumMap.size() >= 2) {
            heightAndWeightSchoolAge.setMaxOverweightRatio(ReportUtil.getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSchoolAge.setMaxObeseRatio(ReportUtil.getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSchoolAge.setMaxStuntingRatio(ReportUtil.getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSchoolAge.setMaxMalnourishedRatio(ReportUtil.getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
        }

        return heightAndWeightSchoolAge;
    }


    /**
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        List<HeightAndWeightMonitorTable> primaryList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<HeightAndWeightMonitorTable> juniorList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }

        List<HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)) {
            List<HeightAndWeightMonitorTable> normalHighList = changeHeightAndWeightSchoolAgeNameTable(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)) {
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
            List<HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList, 10, ReportConst.HIGH);
            if (CollectionUtil.isNotEmpty(highList)) {
                tableList.addAll(highList);
            }
        } else {
            List<HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList, SchoolAge.HIGH.code, ReportConst.HIGH);
            if (CollectionUtil.isNotEmpty(highList)) {
                tableList.addAll(highList);
            }
        }

        List<HeightAndWeightMonitorTable> universityList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }
        getHeightAndWeightSchoolAgeTable(statConclusionList,ReportConst.TOTAL,tableList);

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }

    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName) {
        if (Objects.equals(schoolAge, 10)) {
            List<HeightAndWeightMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList, itemName, mergeList);
            return mergeList;
        }
        List<HeightAndWeightMonitorTable> heightAndWeightGradeList = getHeightAndWeightGrade(statConclusionList, schoolAge);
        if (CollectionUtil.isNotEmpty(heightAndWeightGradeList)) {
            heightAndWeightGradeList.get(heightAndWeightGradeList.size() - 1).setItemName(itemName);
        }
        return heightAndWeightGradeList;
    }


    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge, 10)) {
            List<HeightAndWeightMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList, ReportConst.HIGH, mergeList);
            return mergeList;
        }

        return getHeightAndWeightGrade(statConclusionList, schoolAge);
    }

    private List<HeightAndWeightMonitorTable> changeHeightAndWeightSchoolAgeNameTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<HeightAndWeightMonitorTable> heightAndWeightGrade = getHeightAndWeightGrade(statConclusionList, schoolAge);
        for (HeightAndWeightMonitorTable table : heightAndWeightGrade) {
            if (table.getItemName().startsWith("高")){
                table.setItemName("普"+table.getItemName());
            }
        }

        return heightAndWeightGrade;
    }

    private List<HeightAndWeightMonitorTable> getHeightAndWeightGrade(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<HeightAndWeightMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightSchoolAgeTable(list, gradeCodeEnum.getName(), gradeList);
        });
        getHeightAndWeightSchoolAgeTable(conclusionList, SchoolAge.get(schoolAge).desc, gradeList);

        return gradeList;
    }

    private void getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, String grade, List<HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(grade);
        gradeList.add(heightAndWeightMonitorTable);
    }

    /**
     * 体重身高监测结果-不同年龄段
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
        if (CollectionUtil.isEmpty(statConclusionList)) {
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
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()), TreeMap::new, Collectors.toList()));
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age -> getHeightAndWeightAgeTable(age, ageMap.get(age), tableList));
        getHeightAndWeightAgeTable(1000, statConclusionList, tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<HeightAndWeightMonitorTable> tableList) {
        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum()
                .build(conclusionlist)
                .ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }
}
