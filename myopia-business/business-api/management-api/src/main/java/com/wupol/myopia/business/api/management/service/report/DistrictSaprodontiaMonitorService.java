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
 * 龋齿监测
 *
 * @author hang.yuan 2022/5/25 15:04
 */
@Service
public class DistrictSaprodontiaMonitorService {

    @Autowired
    private ReportChartService reportChartService;

    /**
     * 龋齿监测结果
     */
    public void getDistrictSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO = new DistrictSaprodontiaMonitorVO();

        SaprodontiaNum.MAP.put(0, statConclusionList.size());
        SaprodontiaNum.MAP.put(1, SaprodontiaNum.dmftNum(statConclusionList));

        //说明变量
        getSaprodontiaMonitorVariableVO(statConclusionList, districtSaprodontiaMonitorVO);
        //不同性别
        getSaprodontiaSexVO(statConclusionList, districtSaprodontiaMonitorVO);
        //不同学龄段
        getSaprodontiaSchoolAgeVO(statConclusionList, districtSaprodontiaMonitorVO);
        //不同年龄段
        getSaprodontiaAgeVO(statConclusionList, districtSaprodontiaMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSaprodontiaMonitorVO(districtSaprodontiaMonitorVO);
    }

    /**
     * 龋齿监测结果-说明变量
     */
    private void getSaprodontiaMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new SaprodontiaNum()
                .build(statConclusionList)
                .ratio()
                .buildSaprodontiaMonitorVariableVO();
        districtSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaSexVO saprodontiaSexVO = new SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList, saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList, saprodontiaSexVO);
        reportChartService.getSexMonitorChart(statConclusionList, saprodontiaSexVO);
        districtSaprodontiaMonitorVO.setSaprodontiaSexVO(saprodontiaSexVO);

    }


    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, SaprodontiaSexVO saprodontiaSexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<SaprodontiaNum> saprodontiaSexList = Lists.newArrayList();
        genderMap.forEach((gender, list) -> getSaprodontiaNum(gender, list, saprodontiaSexList));

        if (!saprodontiaSexList.isEmpty()) {
            SaprodontiaSexVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new SaprodontiaSexVO.SaprodontiaSexVariableVO();
            saprodontiaSexVariableVO.setSaprodontiaRatioCompare(ReportUtil.getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRatio, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(ReportUtil.getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaLossRatio, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(ReportUtil.getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRepairRatio, SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaSexVO.setSaprodontiaSexVariableVO(saprodontiaSexVariableVO);
        }
    }

    private void getSaprodontiaNum(Integer gender, List<StatConclusion> statConclusionList, List<SaprodontiaNum> saprodontiaSexList) {
        SaprodontiaNum build = new SaprodontiaNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaSexList.add(build);
    }



    /**
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, SaprodontiaSexVO saprodontiaSexVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }

        List<SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList, GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)) {
            tableList.add(maleTable);
        }
        SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList, GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)) {
            tableList.add(femaleTable);
        }
        SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList, 10);
        if (Objects.nonNull(totalTable)) {
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaSexMonitorTableList(tableList);

    }

    private SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }

        List<StatConclusion> conclusionlist;
        if (Objects.equals(10, gender)) {
            conclusionlist = statConclusionList;
        } else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        SaprodontiaMonitorTable saprodontiaMonitorTable = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol().buildTable();

        if (Objects.equals(10, gender)) {
            saprodontiaMonitorTable.setItemName(ReportConst.TOTAL);
        } else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender)+ ReportConst.SEX);
        }
        return saprodontiaMonitorTable;
    }


    /**
     * 龋齿监测结果-不同学龄段
     */
    private void getSaprodontiaSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO = new SaprodontiaSchoolAgeVO();
        getSaprodontiaSchoolAgeVariableVO(statConclusionList, saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorTableList(statConclusionList, saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorChart(statConclusionList, saprodontiaSchoolAgeVO);
        districtSaprodontiaMonitorVO.setSaprodontiaSchoolAgeVO(saprodontiaSchoolAgeVO);
    }

    /**
     * 龋齿监测结果-不同学龄段-图表
     */
    private void getSaprodontiaSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = getData(statConclusionList);
        if (CollUtil.isNotEmpty(tupleList)) {
            reportChartService.getSchoolAgeMonitorChart(tupleList, saprodontiaSchoolAgeVO);
        }
    }

    private List<TwoTuple<String, SchoolAgeRatioVO>> getData(List<StatConclusion> statConclusionList) {

        if (ReportUtil.getSchoolGrade(statConclusionList)) {
            return Collections.emptyList();
        }

        List<TwoTuple<String, SchoolAgeRatioVO>> tupleList = Lists.newArrayList();
        SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primary)) {
            tupleList.add(TwoTuple.of(SchoolAge.PRIMARY.desc, primary));
        }
        SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        if (Objects.nonNull(junior)) {
            tupleList.add(TwoTuple.of(SchoolAge.JUNIOR.desc, junior));
        }
        SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(normalHigh) || Objects.nonNull(vocationalHigh)) {
            SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(statConclusionList, 10);
            tupleList.add(TwoTuple.of(ReportConst.HIGH, high));
        }

        SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(university)) {
            tupleList.add(TwoTuple.of(SchoolAge.UNIVERSITY.desc, university));
        }
        return tupleList;
    }


    /**
     * 龋齿监测结果-不同学龄段-说明变量
     */
    private void getSaprodontiaSchoolAgeVariableVO(List<StatConclusion> statConclusionList, SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaSchoolAgeVO.SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO = new SaprodontiaSchoolAgeVO.SaprodontiaSchoolAgeVariableVO();
        SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.JUNIOR.code);
        SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(statConclusionList, 10);
        SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.HIGH.code);
        SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.UNIVERSITY.code);

        saprodontiaSchoolAgeVariableVO.setPrimarySchool(primary);
        saprodontiaSchoolAgeVariableVO.setJuniorHighSchool(junior);
        saprodontiaSchoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh) && Objects.nonNull(normalHigh) ) {
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
            saprodontiaSchoolAgeVariableVO.setNormalHighSchool(normalHigh);
            saprodontiaSchoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        } else {
            changeGradeName(high);
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
        }
        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeVariableVO(saprodontiaSchoolAgeVariableVO);
    }

    private void changeGradeName(SaprodontiaSchoolAge high){
        Optional.ofNullable(high)
                .map(SaprodontiaSchoolAge::getMaxSaprodontiaRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxSaprodontiaRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(SaprodontiaSchoolAge::getMaxSaprodontiaLossRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxSaprodontiaLossRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));

        Optional.ofNullable(high)
                .map(SaprodontiaSchoolAge::getMaxSaprodontiaRepairRatio)
                .map(GradeRatio::getGrade)
                .ifPresent(grade-> high.getMaxSaprodontiaRepairRatio().setGrade(grade.replace(ReportConst.VOCATIONAL_HIGH, StrUtil.EMPTY)));
    }

    private SaprodontiaSchoolAge getSaprodontiaSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }

        List<StatConclusion> conclusionList;
        if (Objects.equals(schoolAge, 10)) {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(SchoolAge.HIGH.code, sc.getSchoolAge()) || Objects.equals(SchoolAge.VOCATIONAL_HIGH.code, sc.getSchoolAge())).collect(Collectors.toList());
        } else {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        }
        return getSaprodontiaSchoolAgeRecord(conclusionList);
    }


    private SaprodontiaSchoolAge getSaprodontiaSchoolAgeRecord(List<StatConclusion> statConclusionList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return null;
        }

        SaprodontiaSchoolAge saprodontiaSchoolAge = new SaprodontiaNum()
                .build(statConclusionList)
                .ratio()
                .buildSaprodontiaSchoolAge();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode, list) -> ReportUtil.getSaprodontiaNum(ReportUtil.getGradeName(gradeCode), list, saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2) {
            saprodontiaSchoolAge.setMaxSaprodontiaRatio(ReportUtil.getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaCount, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaLossRatio(ReportUtil.getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaRepairRatio(ReportUtil.getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRepairRatioStr));
        }

        return saprodontiaSchoolAge;
    }


    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Boolean singleSchoolAge = ReportUtil.singleSchoolAge(statConclusionList);
        List<SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        List<SaprodontiaMonitorTable> primaryList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.PRIMARY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(primaryList)) {
            tableList.addAll(primaryList);
        }
        List<SaprodontiaMonitorTable> juniorList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.JUNIOR.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(juniorList)) {
            tableList.addAll(juniorList);
        }
        List<SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(vocationalHighList)) {
            List<SaprodontiaMonitorTable> normalHighList = changeSaprodontiaSchoolAgeNameTable(statConclusionList, SchoolAge.HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                tableList.addAll(normalHighList);
                tableList.addAll(vocationalHighList);
                List<SaprodontiaMonitorTable> highList = getSaprodontiaSchoolAgeMergeTable(statConclusionList, 10, ReportConst.HIGH,singleSchoolAge);
                if (CollUtil.isNotEmpty(highList)) {
                    tableList.addAll(highList);
                }
            }else {
                ReportUtil.changeName(vocationalHighList,tableList);
            }

        } else {
            List<SaprodontiaMonitorTable> normalHighList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.HIGH.code,singleSchoolAge);
            if (CollUtil.isNotEmpty(normalHighList)) {
                normalHighList.get(normalHighList.size()-1).setItemName(ReportConst.HIGH);
                tableList.addAll(normalHighList);
            }
        }

        List<SaprodontiaMonitorTable> universityList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.UNIVERSITY.code,singleSchoolAge);
        if (CollUtil.isNotEmpty(universityList)) {
            tableList.addAll(universityList);
        }

        getSaprodontiaSchoolAgeTable(statConclusionList,ReportConst.TOTAL,tableList);

        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeMonitorTableList(tableList);
    }

    private List<SaprodontiaMonitorTable> getSaprodontiaSchoolAgeMergeTable(List<StatConclusion> statConclusionList, Integer schoolAge, String itemName,Boolean singleSchoolAge) {

        if (Objects.equals(schoolAge, 10)) {
            List<SaprodontiaMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getSaprodontiaSchoolAgeTable(conclusionList, itemName, mergeList);
            return mergeList;
        }
        List<SaprodontiaMonitorTable> saprodontiaGradeList = getSaprodontiaGrade(statConclusionList, schoolAge,singleSchoolAge);
        if (CollUtil.isNotEmpty(saprodontiaGradeList)) {
            saprodontiaGradeList.get(saprodontiaGradeList.size() - 1).setItemName(itemName);
        }
        return saprodontiaGradeList;
    }

    private List<SaprodontiaMonitorTable> getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge, 10)) {
            List<SaprodontiaMonitorTable> mergeList = Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code) || Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getSaprodontiaSchoolAgeTable(conclusionList, ReportConst.HIGH, mergeList);
            return mergeList;
        }

        return getSaprodontiaGrade(statConclusionList, schoolAge,singleSchoolAge);
    }

    private List<SaprodontiaMonitorTable> changeSaprodontiaSchoolAgeNameTable(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<SaprodontiaMonitorTable> saprodontiaGrade = getSaprodontiaGrade(statConclusionList, schoolAge,singleSchoolAge);
        for (SaprodontiaMonitorTable table : saprodontiaGrade) {
            if (table.getItemName().startsWith("高")){
                table.setItemName("普"+table.getItemName());
            }
        }
        return saprodontiaGrade;
    }


    private List<SaprodontiaMonitorTable> getSaprodontiaGrade(List<StatConclusion> statConclusionList, Integer schoolAge,Boolean singleSchoolAge) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = MapUtil.sort(gradeCodeMap);
        List<SaprodontiaMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade, list) -> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getSaprodontiaSchoolAgeTable(list, gradeCodeEnum.getName(), gradeList);
        });
        if (Objects.equals(singleSchoolAge,Boolean.FALSE)){
            getSaprodontiaSchoolAgeTable(conclusionList, SchoolAge.get(schoolAge).desc, gradeList);
        }

        return gradeList;
    }

    private void getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList, String grade, List<SaprodontiaMonitorTable> gradeList) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaMonitorTable saprodontiaMonitorTable = new SaprodontiaNum()
                .build(statConclusionList)
                .ratioNotSymbol()
                .buildTable();
        saprodontiaMonitorTable.setItemName(grade);
        gradeList.add(saprodontiaMonitorTable);
    }

    /**
     * 龋齿监测结果-不同年龄
     */
    private void getSaprodontiaAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        SaprodontiaAgeVO saprodontiaAgeVO = new SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList, saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList, saprodontiaAgeVO);
        reportChartService.getAgeMonitorChart(statConclusionList, saprodontiaAgeVO);
        districtSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }


    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)-> ReportUtil.getSaprodontiaNum(age, list, saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2) {
            SaprodontiaAgeVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new SaprodontiaAgeVO.SaprodontiaAgeVariableVO();
            saprodontiaAgeVariableVO.setSaprodontiaRatio(ReportUtil.getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaCount, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaLossRatio(ReportUtil.getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(ReportUtil.getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
        }
    }

    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollUtil.isEmpty(statConclusionList)) {
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()), TreeMap::new, Collectors.toList()));
        List<SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list) -> getSaprodontiaAgeTable(age, list, tableList));
        getSaprodontiaAgeTable(1000, statConclusionList, tableList);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorTableList(tableList);
    }


    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SaprodontiaMonitorTable> tableList) {

        String itemName;
        if (age == 1000) {
            itemName = ReportConst.TOTAL;
        } else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        SaprodontiaMonitorTable saprodontiaAgeMonitorTable = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol().buildTable();
        saprodontiaAgeMonitorTable.setItemName(itemName);
        tableList.add(saprodontiaAgeMonitorTable);
    }

}
