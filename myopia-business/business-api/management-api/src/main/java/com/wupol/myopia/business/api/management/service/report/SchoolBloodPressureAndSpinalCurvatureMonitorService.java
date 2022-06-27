package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 血压与脊柱弯曲监测
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class SchoolBloodPressureAndSpinalCurvatureMonitorService {

    @Autowired
    private ReportChartService reportChartService;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    public void getSchoolBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO();

        BloodPressureAndSpinalCurvatureNum.MAP.put(0, statConclusionList.size());
        //说明变量
        getBloodPressureAndSpinalCurvatureMonitorVariableVO(statConclusionList, schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同性别
        getBloodPressureAndSpinalCurvatureSexVO(statConclusionList, schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年级
        getBloodPressureAndSpinalCurvatureGradeVO(statConclusionList, schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年龄段
        getBloodPressureAndSpinalCurvatureAgeVO(statConclusionList, schoolBloodPressureAndSpinalCurvatureMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolBloodPressureAndSpinalCurvatureMonitorVO(schoolBloodPressureAndSpinalCurvatureMonitorVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureMonitorVariableVO variableVO = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList)
                .ratio()
                .buildBloodPressureAndSpinalCurvatureMonitorVariableVO();
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureMonitorVariableVO(variableVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        BloodPressureAndSpinalCurvatureSexVO sexVO = new BloodPressureAndSpinalCurvatureSexVO();
        getBloodPressureAndSpinalCurvatureSexVariableVO(statConclusionList, sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorTableList(statConclusionList, sexVO);
        reportChartService.getSexMonitorChart(statConclusionList, sexVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSexVO(sexVO);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSexVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureSexList = Lists.newArrayList();
        genderMap.forEach((gender, list) -> getBloodPressureAndSpinalCurvatureNum(gender, list, bloodPressureAndSpinalCurvatureSexList));

        if (!bloodPressureAndSpinalCurvatureSexList.isEmpty()) {
            BloodPressureAndSpinalCurvatureSexVO.BloodPressureAndSpinalCurvatureSexVariableVO sexVariableVO = new BloodPressureAndSpinalCurvatureSexVO.BloodPressureAndSpinalCurvatureSexVariableVO();
            sexVariableVO.setAbnormalSpineCurvatureRatioCompare(ReportUtil.getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatio, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            sexVariableVO.setHighBloodPressureRatioCompare(ReportUtil.getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatio, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));

            sexVO.setBloodPressureAndSpinalCurvatureSexVariableVO(sexVariableVO);
        }
    }

    private void getBloodPressureAndSpinalCurvatureNum(Integer gender, List<StatConclusion> statConclusionList, List<BloodPressureAndSpinalCurvatureNum> sexList) {
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        sexList.add(build);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSexMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        BloodPressureAndSpinalCurvatureMonitorTable maleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList, GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)) {
            tableList.add(maleTable);
        }
        BloodPressureAndSpinalCurvatureMonitorTable femaleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList, GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)) {
            tableList.add(femaleTable);
        }
        BloodPressureAndSpinalCurvatureMonitorTable totalTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList, 10);
        if (Objects.nonNull(totalTable)) {
            tableList.add(totalTable);
        }
        sexVO.setBloodPressureAndSpinalCurvatureSexMonitorTableList(tableList);
    }

    private BloodPressureAndSpinalCurvatureMonitorTable getBloodPressureAndSpinalCurvatureSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10, gender)) {
            conclusionlist = statConclusionList;
        } else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol().buildTable();
        if (Objects.equals(10, gender)) {
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName(ReportConst.TOTAL);
        } else {
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName(GenderEnum.getName(gender)+ReportConst.SEX);
        }
        return bloodPressureAndSpinalCurvatureMonitorTable;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段
     */
    private void getBloodPressureAndSpinalCurvatureGradeVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureGradeVO gradeVO = new BloodPressureAndSpinalCurvatureGradeVO();
        getBloodPressureAndSpinalCurvatureGradeVariableVO(statConclusionList, gradeVO);
        getBloodPressureAndSpinalCurvatureGradeMonitorTableList(statConclusionList, gradeVO);
        reportChartService.getGradeMonitorChart(statConclusionList, gradeVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureGradeVO(gradeVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureGradeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureGradeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        BloodPressureAndSpinalCurvatureGradeVO.BloodPressureAndSpinalCurvatureGradeVariableVO schoolAgeVariableVO = new BloodPressureAndSpinalCurvatureGradeVO.BloodPressureAndSpinalCurvatureGradeVariableVO();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            ReportUtil.getBloodPressureAndSpinalCurvatureNum(gradeCodeEnum.getName(), list, bloodPressureAndSpinalCurvatureNumMap);
        });

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2) {
            schoolAgeVariableVO.setAbnormalSpineCurvatureRatio(ReportUtil.getSchoolGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            schoolAgeVariableVO.setHighBloodPressureRatio(ReportUtil.getSchoolGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
            schoolAgeVO.setBloodPressureAndSpinalCurvatureGradeVariableVO(schoolAgeVariableVO);
        }
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureGradeMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureGradeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        List<BloodPressureAndSpinalCurvatureMonitorTable> primaryList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> juniorList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.HIGH.code);
        if (CollectionUtil.isNotEmpty(normalHighList)) {
            tableList.addAll(normalHighList);
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> vocationalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)) {
            tableList.addAll(vocationalHighList);
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> universityList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }

        getBloodPressureAndSpinalCurvatureGrade(statConclusionList,ReportConst.TOTAL,tableList);
        schoolAgeVO.setBloodPressureAndSpinalCurvatureGradeMonitorTableList(tableList);
    }


    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> getBloodPressureAndSpinalCurvatureGrade(list, ReportUtil.getItemName(grade,schoolAge), tableList));
        getBloodPressureAndSpinalCurvatureGrade(conclusionList, ReportUtil.getItemNameTotal(schoolAge), tableList);
        return tableList;
    }


    private void getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList, String grade, List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(grade);
        gradeList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段
     */
    private void getBloodPressureAndSpinalCurvatureAgeVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureAgeVO ageVO = new BloodPressureAndSpinalCurvatureAgeVO();
        getBloodPressureAndSpinalCurvatureAgeVariableVO(statConclusionList, ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorTableList(statConclusionList, ageVO);
        reportChartService.getAgeMonitorChart(statConclusionList, ageVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureAgeVO(ageVO);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureAgeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        ageMap.forEach((age, list) -> ReportUtil.getBloodPressureAndSpinalCurvatureNum(age, list, bloodPressureAndSpinalCurvatureNumMap));

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2) {
            BloodPressureAndSpinalCurvatureAgeVO.BloodPressureAndSpinalCurvatureAgeVariableVO ageVariableVO = new BloodPressureAndSpinalCurvatureAgeVO.BloodPressureAndSpinalCurvatureAgeVariableVO();
            ageVariableVO.setAbnormalSpineCurvatureRatio(ReportUtil.getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            ageVariableVO.setHighBloodPressureRatio(ReportUtil.getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
            ageVO.setBloodPressureAndSpinalCurvatureAgeVariableVO(ageVariableVO);
        }
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureAgeMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()), TreeMap::new, Collectors.toList()));
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list) -> getBloodPressureAndSpinalCurvatureAgeTable(age, list, tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000, statConclusionList, tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> conclusionlist, List<BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)) {
            return;
        }

        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(itemName);
        tableList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

}
