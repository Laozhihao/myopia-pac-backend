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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param districtCommonDiseasesAnalysisVO 按区域常见病分析对象
     */
    public void getDistrictBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param districtBloodPressureAndSpinalCurvatureMonitorVO 血压与脊柱弯曲监测实体
     */
    private void getBloodPressureAndSpinalCurvatureMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param districtBloodPressureAndSpinalCurvatureMonitorVO 血压与脊柱弯曲监测实体
     */
    private void getBloodPressureAndSpinalCurvatureSexVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param sexVO 血压与脊柱弯曲异常监测-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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

    /**
     * 获取血压与脊柱弯曲异常监测统计实体
     *
     * @param gender 性别
     * @param statConclusionList 筛查结论数据集合
     * @param sexList 血压与脊柱弯曲异常监测统计实体集合
     */
    private void getBloodPressureAndSpinalCurvatureNum(Integer gender, List<StatConclusion> statConclusionList, List<BloodPressureAndSpinalCurvatureNum> sexList) {
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        sexList.add(build);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-表格数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param sexVO 血压与脊柱弯曲异常监测-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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

    /**
     * 获取血压与脊柱弯曲异常监测-表格数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param gender 性别
     */
    private BloodPressureAndSpinalCurvatureMonitorTable getBloodPressureAndSpinalCurvatureSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param districtBloodPressureAndSpinalCurvatureMonitorVO 血压与脊柱弯曲监测实体
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO = new BloodPressureAndSpinalCurvatureSchoolAgeVO();
        getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(statConclusionList, schoolAgeVO);
        getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(statConclusionList, schoolAgeVO);
        getBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(statConclusionList, schoolAgeVO);
        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSchoolAgeVO(schoolAgeVO);
    }

    /**
     * 获取血压与脊柱弯曲异常监测结果不同学龄图表数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAgeVO 血压与脊柱弯曲异常-不同学龄
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = getData(statConclusionList);
        if (CollUtil.isNotEmpty(tupleList)) {
            reportChartService.getSchoolAgeMonitorChart(tupleList, schoolAgeVO);
        }
    }

    /**
     * 获取构造图表数据的原始数据集合
     *
     * @param statConclusionList 筛查结论数据集合
     */
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAgeVO 血压与脊柱弯曲异常-不同学龄
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
        if (Objects.nonNull(vocationalHigh) && Objects.nonNull(normalHigh)) {
            schoolAgeVariableVO.setHighSchool(high);
            schoolAgeVariableVO.setNormalHighSchool(normalHigh);
            schoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        } else {
            changeGradeName(high);
            schoolAgeVariableVO.setHighSchool(high);
        }
        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    /**
     * 修改年级名称
     *
     * @param high 血压与脊柱弯曲异常-不同学龄对象
     */
    private void changeGradeName(BloodPressureAndSpinalCurvatureSchoolAge high){
        Optional.ofNullable(high)
                .map(BloodPressureAndSpinalCurvatureSchoolAge::getMaxHighBloodPressureRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxHighBloodPressureRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(BloodPressureAndSpinalCurvatureSchoolAge::getMaxAbnormalSpineCurvatureRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxAbnormalSpineCurvatureRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

    }

    /**
     * 获取血压与脊柱弯曲异常-不同学龄对象
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAge 学龄段
     */
    private BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
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

    /**
     * 获取血压与脊柱弯曲异常-不同学龄对象
     *
     * @param statConclusionList 筛查结论数据集合
     */
    private BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }

        BloodPressureAndSpinalCurvatureSchoolAge bloodPressureAndSpinalCurvatureSchoolAge = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList)
                .ratio()
                .buildBloodPressureAndSpinalCurvatureSchoolAge();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> ReportUtil.getBloodPressureAndSpinalCurvatureNum(ReportUtil.getGradeName(gradeCode), list, bloodPressureAndSpinalCurvatureNumMap));

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2) {
            bloodPressureAndSpinalCurvatureSchoolAge.setMaxAbnormalSpineCurvatureRatio(ReportUtil.getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            bloodPressureAndSpinalCurvatureSchoolAge.setMaxHighBloodPressureRatio(ReportUtil.getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
        }
        return bloodPressureAndSpinalCurvatureSchoolAge;
    }


    /**
     * 获取血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAgeVO 血压与脊柱弯曲异常-不同学龄对象
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Boolean singleSchoolAge = ReportUtil.singleSchoolAge(statConclusionList);
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        List<BloodPressureAndSpinalCurvatureMonitorTable> primaryList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> juniorList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }

        List<BloodPressureAndSpinalCurvatureMonitorTable> vocationalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(vocationalHighList)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = changeBloodPressureAndSpinalCurvatureSchoolAgeNameTable(statConclusionList, SchoolAge.HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                tableList.addAll(normalHighList);
                tableList.addAll(vocationalHighList);
                List<BloodPressureAndSpinalCurvatureMonitorTable> highList = getBloodPressureAndSpinalCurvatureSchoolAgeMergeTable(statConclusionList, 10, ReportConst.HIGH,singleSchoolAge);
                if (CollUtil.isNotEmpty(highList)) {
                    tableList.addAll(highList);
                }
            }else {
                ReportUtil.changeName(vocationalHighList,tableList);
            }

        } else {
            List<BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                normalHighList.get(normalHighList.size()-1).setItemName(ReportConst.HIGH);
                tableList.addAll(normalHighList);
            }
        }

        List<BloodPressureAndSpinalCurvatureMonitorTable> universityList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }

        getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,ReportConst.TOTAL,tableList);

        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(tableList);
    }

    /**
     * 获取血压与脊柱弯曲异常监测结果-不同学龄段-表格数据的合并数据集合
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAge 学龄段
     * @param itemName 项目名
     * @param singleSchoolAge 是否是单学龄段
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName,Boolean singleSchoolAge) {
        if (Objects.equals(schoolAge, 10)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, itemName, mergeList);
            return mergeList;
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGradeList = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge,singleSchoolAge);
        if (CollUtil.isNotEmpty(bloodPressureAndSpinalCurvatureGradeList)) {
            bloodPressureAndSpinalCurvatureGradeList.get(bloodPressureAndSpinalCurvatureGradeList.size() - 1).setItemName(itemName);
        }
        return bloodPressureAndSpinalCurvatureGradeList;
    }

    /**
     * 获取血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAge 学龄段
     * @param singleSchoolAge 是否是单学龄段
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge, 10)) {
            List<BloodPressureAndSpinalCurvatureMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, ReportConst.HIGH, mergeList);
            return mergeList;
        }

        return getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge,singleSchoolAge);
    }

    /**
     * 修改血压与脊柱弯曲异常监测结果-不同学龄段-表格数据的项目名称
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAge 学龄段
     * @param singleSchoolAge 是否是单学龄段
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> changeBloodPressureAndSpinalCurvatureSchoolAgeNameTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGrade = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, schoolAge,singleSchoolAge);
        for (BloodPressureAndSpinalCurvatureMonitorTable table : bloodPressureAndSpinalCurvatureGrade) {
            if (table.getItemName().startsWith("高")){
                table.setItemName("普"+table.getItemName());
            }
        }

        return bloodPressureAndSpinalCurvatureGrade;
    }

    /**
     * 获取血压与脊柱弯曲异常监测结果-不同学龄段-表格数据中的年级数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param schoolAge 学龄段
     * @param singleSchoolAge 是否是单学龄段
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = MapUtil.sort(gradeCodeMap);
        List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(list, gradeCodeEnum.getName(), gradeList);
        });
        if (Objects.equals(singleSchoolAge,Boolean.FALSE)){
            getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList, SchoolAge.get(schoolAge).desc, gradeList);
        }

        return gradeList;
    }

    /**
     * 获取血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     *
     * @param statConclusionList 筛查结论数据集合
     * @param grade 年级
     * @param gradeList 表格数据集合
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList, String grade, List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(grade);
        gradeList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段
     *
     * @param statConclusionList 筛查结论数据集合
     * @param districtBloodPressureAndSpinalCurvatureMonitorVO 血压与脊柱弯曲监测实体
     */
    private void getBloodPressureAndSpinalCurvatureAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param ageVO 血压与脊柱弯曲异常监测-不同年龄段
     */
    private void getBloodPressureAndSpinalCurvatureAgeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
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
     *
     * @param statConclusionList 筛查结论数据集合
     * @param ageVO 血压与脊柱弯曲异常监测-不同年龄段
     */
    private void getBloodPressureAndSpinalCurvatureAgeMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()), TreeMap::new, Collectors.toList()));
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list) -> getBloodPressureAndSpinalCurvatureAgeTable(age, list, tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000, statConclusionList, tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-表格数据
     *
     * @param age 年龄段
     * @param statConclusionList 筛查结论数据集合
     * @param tableList 表格数据集合
     */
    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> statConclusionList, List<BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(itemName);
        tableList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

}
