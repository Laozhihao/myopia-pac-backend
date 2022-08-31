package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtCommonDiseasesAnalysisVO 按区域常见病分析
     */
    public void getDistrictHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtHeightAndWeightMonitorVO 身高体重监测实体
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtHeightAndWeightMonitorVO 身高体重监测实体
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param heightAndWeightSexVO 身高体重监测-不同性别
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

    /**
     * 获取身高体重监测统计对象
     *
     * @param gender 性别
     * @param statConclusionList 筛查数据结论集合
     * @param heightAndWeightSexList 身高体重监测统计对象集合
     */
    private void getHeightAndWeightNum(Integer gender, List<StatConclusion> statConclusionList, List<HeightAndWeightNum> heightAndWeightSexList) {
        HeightAndWeightNum build = new HeightAndWeightNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightSexList.add(build);
    }


    /**
     * 体重身高监测结果-不同性别-表格数据
     *
     * @param statConclusionList 筛查数据结论集合
     * @param sexVO 身高体重监测-不同性别对象
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

    /**
     * 获取身高体重性别表格数据
     *
     * @param statConclusionList 筛查数据结论集合
     * @param gender 性别
     */
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtHeightAndWeightMonitorVO 身高体重监测实体
     */
    private void getHeightAndWeightSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO = new HeightAndWeightSchoolAgeVO();
        getHeightAndWeightSchoolAgeVariableVO(statConclusionList, heightAndWeightSchoolAgeVO);
        getHeightAndWeightSchoolAgeMonitorTableList(statConclusionList, heightAndWeightSchoolAgeVO);
        getHeightAndWeightSchoolAgeMonitorChart(statConclusionList, heightAndWeightSchoolAgeVO);
        districtHeightAndWeightMonitorVO.setHeightAndWeightSchoolAgeVO(heightAndWeightSchoolAgeVO);

    }

    /**
     * 获取身高体重学龄图表数据
     *
     * @param statConclusionList 筛查数据结论集合
     * @param heightAndWeightSchoolAgeVO 身高体重监测-不同学龄
     */
    private void getHeightAndWeightSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = getData(statConclusionList);
        if (CollUtil.isNotEmpty(tupleList)) {
            reportChartService.getSchoolAgeMonitorChart(tupleList, heightAndWeightSchoolAgeVO);
        }

    }

    /**
     * 获取构建图表数据的原数据集合
     *
     * @param statConclusionList 筛查数据结论集合
     */
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param heightAndWeightSchoolAgeVO 身高体重监测-不同学龄
     */
    private void getHeightAndWeightSchoolAgeVariableVO(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
        if (Objects.nonNull(vocationalHigh) && Objects.nonNull(normalHigh)) {
            schoolAgeVariableVO.setHighSchool(high);
            schoolAgeVariableVO.setNormalHighSchool(normalHigh);
            schoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        } else {
            changeGradeName(high);
            schoolAgeVariableVO.setHighSchool(high);
        }
        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    /**
     * 修改年级名称
     *
     * @param high 身高体重监测-不同学龄对象
     */
    private void changeGradeName(HeightAndWeightSchoolAge high){
        Optional.ofNullable(high)
                .map(HeightAndWeightSchoolAge::getMaxOverweightRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxOverweightRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(HeightAndWeightSchoolAge::getMaxObeseRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxObeseRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(HeightAndWeightSchoolAge::getMaxStuntingRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxStuntingRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(HeightAndWeightSchoolAge::getMaxMalnourishedRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxMalnourishedRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));
    }

    /**
     * 获取身高体重监测-不同学龄对象
     *
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄段
     */
    private HeightAndWeightSchoolAge getHeightAndWeightSchoolSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
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

    /**
     * 获取身高体重监测-不同学龄对象
     *
     * @param statConclusionList 筛查数据结论集合
     */
    private HeightAndWeightSchoolAge getHeightAndWeightSchoolAge(List<StatConclusion> statConclusionList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }

        HeightAndWeightSchoolAge heightAndWeightSchoolAge = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratio()
                .buildHeightAndWeightSchoolAge();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> ReportUtil.getHeightAndWeightNum(ReportUtil.getGradeName(gradeCode), list, heightAndWeightNumMap));

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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param heightAndWeightSchoolAgeVO 身高体重监测-不同学龄
     */
    private void getHeightAndWeightSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Boolean singleSchoolAge = ReportUtil.singleSchoolAge(statConclusionList);
        List<HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        List<HeightAndWeightMonitorTable> primaryList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<HeightAndWeightMonitorTable> juniorList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }

        List<HeightAndWeightMonitorTable> vocationalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(vocationalHighList)) {
            List<HeightAndWeightMonitorTable> normalHighList = changeHeightAndWeightSchoolAgeNameTable(statConclusionList, SchoolAge.HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                tableList.addAll(normalHighList);
                tableList.addAll(vocationalHighList);
                List<HeightAndWeightMonitorTable> highList = getHeightAndWeightSchoolAgeMergeTable(statConclusionList, 10, ReportConst.HIGH,singleSchoolAge);
                if (CollUtil.isNotEmpty(highList)) {
                    tableList.addAll(highList);
                }
            }else {
                ReportUtil.changeName(vocationalHighList,tableList);
            }

        } else {
            List<HeightAndWeightMonitorTable> normalHighList = getHeightAndWeightSchoolAgeTable(statConclusionList,  SchoolAge.HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                normalHighList.get(normalHighList.size()-1).setItemName(ReportConst.HIGH);
                tableList.addAll(normalHighList);
            }
        }

        List<HeightAndWeightMonitorTable> universityList = getHeightAndWeightSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }
        getHeightAndWeightSchoolAgeTable(statConclusionList,ReportConst.TOTAL,tableList);

        heightAndWeightSchoolAgeVO.setHeightAndWeightSchoolAgeMonitorTableList(tableList);
    }

    /**
     * 获取身高体重不同学龄表数据合并数据集合
     *
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄
     * @param itemName 项目名称
     * @param singleSchoolAge 是否是单个学龄
     */
    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName,Boolean singleSchoolAge) {
        if (Objects.equals(schoolAge, 10)) {
            List<HeightAndWeightMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList, itemName, mergeList);
            return mergeList;
        }
        List<HeightAndWeightMonitorTable> heightAndWeightGradeList = getHeightAndWeightGrade(statConclusionList, schoolAge,singleSchoolAge);
        if (CollUtil.isNotEmpty(heightAndWeightGradeList)) {
            heightAndWeightGradeList.get(heightAndWeightGradeList.size() - 1).setItemName(itemName);
        }
        return heightAndWeightGradeList;
    }

    /**
     * 获取身高体重学龄表格数据集合
     *
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄
     * @param singleSchoolAge 是否是单个学龄
     */
    private List<HeightAndWeightMonitorTable> getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge, 10)) {
            List<HeightAndWeightMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getHeightAndWeightSchoolAgeTable(conclusionList, ReportConst.HIGH, mergeList);
            return mergeList;
        }

        return getHeightAndWeightGrade(statConclusionList, schoolAge,singleSchoolAge);
    }

    /**
     * 修改身高体重不同学龄表格数据的项目名称
     *
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄
     * @param singleSchoolAge 是否是单个学龄
     */
    private List<HeightAndWeightMonitorTable> changeHeightAndWeightSchoolAgeNameTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<HeightAndWeightMonitorTable> heightAndWeightGrade = getHeightAndWeightGrade(statConclusionList, schoolAge,singleSchoolAge);
        for (HeightAndWeightMonitorTable table : heightAndWeightGrade) {
            if (table.getItemName().startsWith("高")){
                table.setItemName("普"+table.getItemName());
            }
        }

        return heightAndWeightGrade;
    }

    /**
     * 获取身高体重不同学龄表格数据中年级数据
     *
     * @param statConclusionList 筛查数据结论集合
     * @param schoolAge 学龄
     * @param singleSchoolAge 是否是单个学龄
     */
    private List<HeightAndWeightMonitorTable> getHeightAndWeightGrade(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = MapUtil.sort(gradeCodeMap);
        List<HeightAndWeightMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getHeightAndWeightSchoolAgeTable(list, gradeCodeEnum.getName(), gradeList);
        });
        if (Objects.equals(singleSchoolAge,Boolean.FALSE)){
            getHeightAndWeightSchoolAgeTable(conclusionList, SchoolAge.get(schoolAge).desc, gradeList);
        }

        return gradeList;
    }

    /**
     * 获取身高体重不同学龄表格数据
     *
     * @param statConclusionList 筛查数据结论集合
     * @param grade 年级
     * @param gradeList 身高体重不同学龄表格数据集合
     */
    private void getHeightAndWeightSchoolAgeTable(List<StatConclusion> statConclusionList, String grade, List<HeightAndWeightMonitorTable> gradeList) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param districtHeightAndWeightMonitorVO 身高体重监测实体
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, DistrictHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param ageVO 身高体重监测-不同年龄段
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
     *
     * @param statConclusionList 筛查数据结论集合
     * @param ageVO 身高体重监测-不同年龄段
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

    /**
     * 获取体重身高监测结果-不同年龄段-表格数据
     *
     * @param age 年龄
     * @param statConclusionList 筛查数据结论集合
     * @param tableList 表格数据集合
     */
    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> statConclusionList, List<HeightAndWeightMonitorTable> tableList) {
        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightMonitorTable heightAndWeightMonitorTable = new HeightAndWeightNum()
                .build(statConclusionList)
                .ratioNotSymbol().buildTable();
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }
}
