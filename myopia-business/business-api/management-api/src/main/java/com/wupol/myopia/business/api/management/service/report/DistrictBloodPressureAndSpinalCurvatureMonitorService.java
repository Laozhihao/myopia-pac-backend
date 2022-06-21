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
 * 血压与脊柱弯曲监测
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class DistrictBloodPressureAndSpinalCurvatureMonitorService {

    @Autowired
    private ReportChartService reportChartService;

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    public void getDistrictBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO();

        BloodPressureAndSpinalCurvatureNum.MAP.put(0, statConclusionList.size());
        //说明变量
        getBloodPressureAndSpinalCurvatureMonitorVariableVO(statConclusionList, districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同性别
        getBloodPressureAndSpinalCurvatureSexVO(statConclusionList, districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同学龄段
        getBloodPressureAndSpinalCurvatureSchoolAgeVO(statConclusionList, districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年龄段
        getBloodPressureAndSpinalCurvatureAgeVO(statConclusionList, districtBloodPressureAndSpinalCurvatureMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictBloodPressureAndSpinalCurvatureMonitorVO(districtBloodPressureAndSpinalCurvatureMonitorVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureMonitorVariableVO variableVO = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList)
                .ratio()
                .buildBloodPressureAndSpinalCurvatureMonitorVariableVO();
        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureMonitorVariableVO(variableVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureSexVO sexVO = new BloodPressureAndSpinalCurvatureSexVO();
        getBloodPressureAndSpinalCurvatureSexVariableVO(statConclusionList, sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorTableList(statConclusionList, sexVO);
        reportChartService.getSexMonitorChart(statConclusionList, sexVO);
        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSexVO(sexVO);
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
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO = new BloodPressureAndSpinalCurvatureSchoolAgeVO();
        getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(statConclusionList, schoolAgeVO);
        getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(statConclusionList, schoolAgeVO);
        getBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(statConclusionList, schoolAgeVO);
        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSchoolAgeVO(schoolAgeVO);
    }

    private void getBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = getData(statConclusionList);
        if (CollectionUtil.isNotEmpty(tupleList)) {
            reportChartService.getSchoolAgeMonitorChart(tupleList, schoolAgeVO);
        }
    }

    private List<TwoTuple<String, SchoolAgeRatioVO>> getData(List<StatConclusion> statConclusionList) {
        if (ReportUtil.getSchoolGrade(statConclusionList)) {
            return Collections.emptyList();
        }

        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = Lists.newArrayList();

        BloodPressureAndSpinalCurvatureSchoolAge primary = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primary)) {
            tupleList.add(TwoTuple.of(SchoolAge.PRIMARY.desc, primary));
        }
        BloodPressureAndSpinalCurvatureSchoolAge junior = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        if (Objects.nonNull(junior)) {
            tupleList.add(TwoTuple.of(SchoolAge.JUNIOR.desc, junior));
        }

        BloodPressureAndSpinalCurvatureSchoolAge normalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        BloodPressureAndSpinalCurvatureSchoolAge vocationalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(normalHigh) || Objects.nonNull(vocationalHigh)) {
            BloodPressureAndSpinalCurvatureSchoolAge high = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, 10);
            tupleList.add(TwoTuple.of(ReportConst.HIGH, high));
        }
        BloodPressureAndSpinalCurvatureSchoolAge university = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(university)) {
            tupleList.add(TwoTuple.of(SchoolAge.UNIVERSITY.desc, university));
        }
        return tupleList;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }

        BloodPressureAndSpinalCurvatureSchoolAgeVO.BloodPressureAndSpinalCurvatureSchoolAgeVariableVO schoolAgeVariableVO = new BloodPressureAndSpinalCurvatureSchoolAgeVO.BloodPressureAndSpinalCurvatureSchoolAgeVariableVO();

        BloodPressureAndSpinalCurvatureSchoolAge primary = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        BloodPressureAndSpinalCurvatureSchoolAge junior = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        BloodPressureAndSpinalCurvatureSchoolAge high = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, 10);
        BloodPressureAndSpinalCurvatureSchoolAge normalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        BloodPressureAndSpinalCurvatureSchoolAge vocationalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        BloodPressureAndSpinalCurvatureSchoolAge university = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);

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
        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    private BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return null;
        }

        List<StatConclusion> conclusionList;
        if (Objects.equals(schoolAge, 10)) {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(SchoolAge.HIGH.code, sc.getSchoolAge()) || Objects.equals(SchoolAge.VOCATIONAL_HIGH.code, sc.getSchoolAge())).collect(Collectors.toList());
        }else {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        }

        return getBloodPressureAndSpinalCurvatureSchoolAge(conclusionList);

    }

    private BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return null;
        }

        BloodPressureAndSpinalCurvatureSchoolAge bloodPressureAndSpinalCurvatureSchoolAge = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList)
                .ratio()
                .buildBloodPressureAndSpinalCurvatureSchoolAge();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            ReportUtil.getBloodPressureAndSpinalCurvatureNum(gradeCodeEnum.getName(), list, bloodPressureAndSpinalCurvatureNumMap);
        });

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2) {
            bloodPressureAndSpinalCurvatureSchoolAge.setMaxAbnormalSpineCurvatureRatio(ReportUtil.getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            bloodPressureAndSpinalCurvatureSchoolAge.setMaxHighBloodPressureRatio(ReportUtil.getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
        }
        return bloodPressureAndSpinalCurvatureSchoolAge;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
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

        List<BloodPressureAndSpinalCurvatureMonitorTable> vocationalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = changeBloodPressureAndSpinalCurvatureSchoolAgeNameTable(statConclusionList, SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)) {
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
            List<BloodPressureAndSpinalCurvatureMonitorTable> highList = getBloodPressureAndSpinalCurvatureSchoolAgeMergeTable(statConclusionList, 10, "高中");
            if (CollectionUtil.isNotEmpty(highList)) {
                tableList.addAll(highList);
            }
        } else {
            List<BloodPressureAndSpinalCurvatureMonitorTable> highList = getBloodPressureAndSpinalCurvatureSchoolAgeMergeTable(statConclusionList, SchoolAge.HIGH.code, "高中");
            if (CollectionUtil.isNotEmpty(highList)) {
                tableList.addAll(highList);
            }
        }

        List<BloodPressureAndSpinalCurvatureMonitorTable> universityList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }

        getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,ReportConst.TOTAL,tableList);

        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(tableList);
    }

    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName) {
        if (Objects.equals(schoolAge, 10)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, itemName, mergeList);
            return mergeList;
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGradeList = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge);
        if (CollectionUtil.isNotEmpty(bloodPressureAndSpinalCurvatureGradeList)) {
            bloodPressureAndSpinalCurvatureGradeList.get(bloodPressureAndSpinalCurvatureGradeList.size() - 1).setItemName(itemName);
        }
        return bloodPressureAndSpinalCurvatureGradeList;
    }

    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge, 10)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, ReportConst.HIGH, mergeList);
            return mergeList;
        }

        return getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge);
    }

    private List<BloodPressureAndSpinalCurvatureMonitorTable> changeBloodPressureAndSpinalCurvatureSchoolAgeNameTable(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGrade = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge);
        for (BloodPressureAndSpinalCurvatureMonitorTable table : bloodPressureAndSpinalCurvatureGrade) {
            if (table.getItemName().startsWith("高")){
                table.setItemName("普"+table.getItemName());
            }
        }

        return bloodPressureAndSpinalCurvatureGrade;
    }

    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(list, gradeCodeEnum.getName(), gradeList);
        });
        getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, SchoolAge.get(schoolAge).desc, gradeList);

        return gradeList;
    }

    private void getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList, String grade, List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
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
    private void getBloodPressureAndSpinalCurvatureAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureAgeVO ageVO = new BloodPressureAndSpinalCurvatureAgeVO();
        getBloodPressureAndSpinalCurvatureAgeVariableVO(statConclusionList, ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorTableList(statConclusionList, ageVO);
        reportChartService.getAgeMonitorChart(statConclusionList, ageVO);
        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureAgeVO(ageVO);
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
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age -> getBloodPressureAndSpinalCurvatureAgeTable(age, ageMap.get(age), tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000, statConclusionList, tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> conclusionlist, List<BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
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
