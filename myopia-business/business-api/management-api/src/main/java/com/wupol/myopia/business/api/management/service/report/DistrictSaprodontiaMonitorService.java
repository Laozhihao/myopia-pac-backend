package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictChartVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSaprodontiaMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 龋齿监测
 *
 * @author hang.yuan 2022/5/25 15:04
 */
@Service
public class DistrictSaprodontiaMonitorService {

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();

    /**
     * 龋齿监测结果
     */
    public void getDistrictSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO = new DistrictSaprodontiaMonitorVO();
        map.put(0,statConclusionList.size());

        //说明变量
        getSaprodontiaMonitorVariableVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同性别
        getSaprodontiaSexVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同学龄段
        getSaprodontiaSchoolAgeVO(statConclusionList,districtSaprodontiaMonitorVO);
        //不同年龄段
        getSaprodontiaAgeVO(statConclusionList,districtSaprodontiaMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSaprodontiaMonitorVO(districtSaprodontiaMonitorVO);
    }

    /**
     * 龋齿监测结果-说明变量
     */
    private void getSaprodontiaMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = buildSaprodontiaMonitorVariableVO(saprodontiaNum);
        districtSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }
    private DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO buildSaprodontiaMonitorVariableVO(SaprodontiaNum saprodontiaNum){
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(saprodontiaNum.dmftRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(saprodontiaNum.saprodontiaLossAndRepairRatioStr);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.saprodontiaLossAndRepairTeethRatioStr);
        return saprodontiaMonitorVariableVO;
    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorChart(statConclusionList,saprodontiaSexVO);
        districtSaprodontiaMonitorVO.setSaprodontiaSexVO(saprodontiaSexVO);

    }

    /**
     * 龋齿监测结果-不同性别-图表
     */
    private void getSaprodontiaSexMonitorChart(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictChartVO.Chart chart = new DistrictChartVO.Chart();
        List<String> x = Lists.newArrayList(ReportConst.SAPRODONTIA,ReportConst.SAPRODONTIA_LOSS,ReportConst.SAPRODONTIA_REPAIR);
        List<DistrictChartVO.ChartData> y = Lists.newArrayList(
                new DistrictChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new DistrictChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );

        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        getChartData(genderMap.get(GenderEnum.MALE.type), y,0);
        getChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);

        chart.setX(x);
        chart.setY(y);
        saprodontiaSexVO.setSaprodontiaSexMonitorChart(chart);
    }

    private void getChartData(List<StatConclusion> statConclusionList,List<DistrictChartVO.ChartData> y,Integer index) {
        SaprodontiaNum num = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();
        y.get(index).getData().add(num.saprodontiaRatio);
        y.get(index).getData().add(num.saprodontiaLossRatio);
        y.get(index).getData().add(num.saprodontiaRepairRatio);
    }

    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<SaprodontiaNum> saprodontiaSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> {
            getSaprodontiaNum(gender,list,saprodontiaSexList);
        });

        if (saprodontiaSexList.size() >= 1){
            DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
            saprodontiaSexVariableVO.setSaprodontiaRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRatio,SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaLossRatio,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRepairRatio,SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaSexVO.setSaprodontiaSexVariableVO(saprodontiaSexVariableVO);
        }
    }

    private void getSaprodontiaNum(Integer gender,List<StatConclusion> statConclusionList,List<SaprodontiaNum> saprodontiaSexList){
        SaprodontiaNum build = new SaprodontiaNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaSexList.add(build);
    }

    private <K>void getSaprodontiaNum(K key, List<StatConclusion> statConclusionList,Map<K, SaprodontiaNum> saprodontiaNumMap){
        SaprodontiaNum build = new SaprodontiaNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        saprodontiaNumMap.put(key,build);
    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<SaprodontiaNum> saprodontiaSexList, Function<SaprodontiaNum,BigDecimal> function, Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function).reversed());
        DistrictSaprodontiaMonitorVO.SaprodontiaSex sex = new DistrictSaprodontiaMonitorVO.SaprodontiaSex();
        if (saprodontiaSexList.size() == 1){
            SaprodontiaNum num = saprodontiaSexList.get(0);
            if (Objects.equals(GenderEnum.MALE.type,num.gender)){
                setSexCompare(num,null, mapper, sex,GenderEnum.FEMALE.desc,ReportConst.ZERO_RATIO_STR);
            }else {
                setSexCompare(num,null, mapper, sex,GenderEnum.MALE.desc,ReportConst.ZERO_RATIO_STR);
            }
        }
        if (saprodontiaSexList.size() == 2){
            SaprodontiaNum forward = saprodontiaSexList.get(0);
            SaprodontiaNum back = saprodontiaSexList.get(1);
            setSexCompare(forward,back, mapper, sex,null,null);
        }
        return sex;
    }
    private void setSexCompare(SaprodontiaNum forward, SaprodontiaNum back, Function<SaprodontiaNum, String> mapper,
                               DistrictSaprodontiaMonitorVO.SaprodontiaSex sex,
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

    private void setSymbol(DistrictSaprodontiaMonitorVO.SaprodontiaSex sex, String forwar, String back) {
        if (Objects.equals(forwar, back)){
            sex.setSymbol("=");
        }else {
            sex.setSymbol(">");
        }
    }

    /**
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaSexMonitorTableList(tableList);

    }

    private DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList,Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        List<StatConclusion> conclusionlist;
        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);

        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
       return saprodontiaMonitorTable;
    }

    public DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable buildTable(SaprodontiaNum saprodontiaNum){
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable();
        saprodontiaMonitorTable.setValidScreeningNum(saprodontiaNum.validScreeningNum);
        saprodontiaMonitorTable.setDmftRatio(saprodontiaNum.dmftRatio);
        saprodontiaMonitorTable.setSaprodontiaNum(saprodontiaNum.saprodontiaNum);
        saprodontiaMonitorTable.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatio);
        saprodontiaMonitorTable.setSaprodontiaLossNum(saprodontiaNum.saprodontiaLossNum);
        saprodontiaMonitorTable.setSaprodontiaLossRatio(saprodontiaNum.saprodontiaLossRatio);
        saprodontiaMonitorTable.setSaprodontiaRepairNum(saprodontiaNum.saprodontiaRepairNum);
        saprodontiaMonitorTable.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairNum(saprodontiaNum.saprodontiaLossAndRepairNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairRatio(saprodontiaNum.saprodontiaLossAndRepairRatio);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethNum(saprodontiaNum.saprodontiaLossAndRepairTeethNum);
        saprodontiaMonitorTable.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.saprodontiaLossAndRepairTeethRatio);
        return saprodontiaMonitorTable;
    }

    /**
     * 龋齿监测结果-不同学龄段
     */
    private void getSaprodontiaSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO();
        getSaprodontiaSchoolAgeVariableVO(statConclusionList,saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorTableList(statConclusionList,saprodontiaSchoolAgeVO);
        getSaprodontiaSchoolAgeMonitorChart(statConclusionList,saprodontiaSchoolAgeVO);
        districtSaprodontiaMonitorVO.setSaprodontiaSchoolAgeVO(saprodontiaSchoolAgeVO);
    }

    /**
     * 龋齿监测结果-不同学龄段-图表
     */
    private void getSaprodontiaSchoolAgeMonitorChart(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictChartVO.Chart chart = new DistrictChartVO.Chart();

        List<String> x = Lists.newArrayList();
        List<DistrictChartVO.ChartData> y = Lists.newArrayList(
                new DistrictChartVO.ChartData(ReportConst.SAPRODONTIA,Lists.newArrayList()),
                new DistrictChartVO.ChartData(ReportConst.SAPRODONTIA_LOSS,Lists.newArrayList()),
                new DistrictChartVO.ChartData(ReportConst.SAPRODONTIA_REPAIR,Lists.newArrayList())
        );

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primary)){
            x.add(SchoolAge.PRIMARY.desc);
            setSchoolAgeData(y,primary);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.JUNIOR.code);
        if (Objects.nonNull(junior)){
            x.add(SchoolAge.JUNIOR.desc);
            setSchoolAgeData(y,junior);
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.HIGH.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(normalHigh) || Objects.nonNull(vocationalHigh)){
            x.add("高中");
            DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(statConclusionList, 10);
            setSchoolAgeData(y,high);
        }

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(university)){
            x.add(SchoolAge.UNIVERSITY.desc);
            setSchoolAgeData(y,university);
        }
        chart.setX(x);
        chart.setY(y);

        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeMonitorChart(chart);

    }

    private void setSchoolAgeData(List<DistrictChartVO.ChartData> y, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge schoolAge){
        y.get(0).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRatio()));
        y.get(1).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaLossRatio()));
        y.get(2).getData().add(ReportUtil.getRatioNotSymbol(schoolAge.getSaprodontiaRepairRatio()));
    }



    /**
     * 龋齿监测结果-不同学龄段-说明变量
     */
    private void getSaprodontiaSchoolAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVariableVO();
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge primary = getSaprodontiaSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge junior = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.JUNIOR.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge high = getSaprodontiaSchoolAge(statConclusionList,10);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge normalHigh = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.HIGH.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge vocationalHigh = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge university = getSaprodontiaSchoolAge(statConclusionList,SchoolAge.UNIVERSITY.code);

        saprodontiaSchoolAgeVariableVO.setPrimarySchool(primary);
        saprodontiaSchoolAgeVariableVO.setJuniorHighSchool(junior);
        saprodontiaSchoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)){
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
            saprodontiaSchoolAgeVariableVO.setNormalHighSchool(normalHigh);
            saprodontiaSchoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        }else {
            saprodontiaSchoolAgeVariableVO.setHighSchool(high);
        }
        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeVariableVO(saprodontiaSchoolAgeVariableVO);
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        List<StatConclusion> conclusionList;
        if (Objects.equals(schoolAge,10)){
            conclusionList = statConclusionList.stream().filter(sc-> Objects.equals(SchoolAge.HIGH.code,sc.getSchoolAge()) ||Objects.equals(SchoolAge.VOCATIONAL_HIGH.code,sc.getSchoolAge())).collect(Collectors.toList());
        }else {
            conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        }
        return getSaprodontiaSchoolAgeRecord(conclusionList);
    }


    private DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge getSaprodontiaSchoolAgeRecord(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();

        DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge saprodontiaSchoolAge = new DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAge();

        saprodontiaSchoolAge.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatioStr);
        saprodontiaSchoolAge.setSaprodontiaLossRatio(saprodontiaNum.saprodontiaLossRatioStr);
        saprodontiaSchoolAge.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatioStr);

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getSaprodontiaNum(gradeCodeEnum.getName(),list,saprodontiaNumMap);
        });

        if (saprodontiaNumMap.size() >= 2){
            saprodontiaSchoolAge.setMaxSaprodontiaRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum,SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaLossRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSchoolAge.setMaxSaprodontiaRepairRatio(getGradeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum,SaprodontiaNum::getSaprodontiaRepairRatioStr));
        }

        return saprodontiaSchoolAge;
    }
    private DistrictSaprodontiaMonitorVO.GradeRatio getGradeRatio(Map<String, SaprodontiaNum> saprodontiaNumMap,Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper){
        if (CollectionUtil.isNotEmpty(saprodontiaNumMap)){
            return null;
        }
        DistrictSaprodontiaMonitorVO.GradeRatio gradeRatio = new DistrictSaprodontiaMonitorVO.GradeRatio();
        TwoTuple<String, String> tuple = ReportUtil.getMaxMap(saprodontiaNumMap, function, mapper);
        gradeRatio.setGrade(tuple.getFirst());
        gradeRatio.setRatio(tuple.getSecond());
        return gradeRatio;
    }



    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> primaryList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> juniorList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(juniorList)){
            tableList.addAll(juniorList);
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> vocationalHighList = getSaprodontiaSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHighList)){
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> normalHighList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> highList = getSaprodontiaSchoolAgeMergeTable(statConclusionList, 10, "高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }

        }else {
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> highList = getSaprodontiaSchoolAgeMergeTable(statConclusionList, SchoolAge.HIGH.code, "高中");
            if (CollectionUtil.isNotEmpty(highList)){
                tableList.addAll(highList);
            }
        }

        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> universityList = getSaprodontiaSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(universityList)){
            tableList.addAll(universityList);
        }

        saprodontiaSchoolAgeVO.setSaprodontiaSchoolAgeMonitorTableList(tableList);
    }

    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaSchoolAgeMergeTable(List<StatConclusion> statConclusionList,Integer schoolAge,String itemName) {

        if (Objects.equals(schoolAge,10)){
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getSaprodontiaSchoolAgeTable(conclusionList,itemName,mergeList);
            return mergeList;
        }
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> saprodontiaGradeList = getSaprodontiaGrade(statConclusionList, schoolAge);
        if (CollectionUtil.isNotEmpty(saprodontiaGradeList)){
            saprodontiaGradeList.get(saprodontiaGradeList.size()-1).setItemName(itemName);
        }
        return saprodontiaGradeList;
    }

    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> mergeList=Lists.newArrayList();
            List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), SchoolAge.HIGH.code)||Objects.equals(sc.getSchoolAge(), SchoolAge.VOCATIONAL_HIGH.code)).collect(Collectors.toList());
            getSaprodontiaSchoolAgeTable(conclusionList,"高中",mergeList);
            return mergeList;
        }

        return getSaprodontiaGrade(statConclusionList,schoolAge);
    }


    private List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> getSaprodontiaGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return Lists.newArrayList();
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getSaprodontiaSchoolAgeTable(list,gradeCodeEnum.getName(),gradeList);
        });
        getSaprodontiaSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,gradeList);

        return gradeList;
    }

    private void getSaprodontiaSchoolAgeTable(List<StatConclusion> statConclusionList,String grade ,List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);
        saprodontiaMonitorTable.setItemName(grade);
        gradeList.add(saprodontiaMonitorTable);
    }

    /**
     * 龋齿监测结果-不同年龄
     */
    private void getSaprodontiaAgeVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO districtSaprodontiaMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorChart(statConclusionList,saprodontiaAgeVO);
        districtSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }

    private void getSaprodontiaAgeMonitorChart(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictChartVO.AgeChart ageChart = new DistrictChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);

        List<String> y = Lists.newArrayList();
        List<DistrictChartVO.AgeData> x = Lists.newArrayList(
                new DistrictChartVO.AgeData(ReportConst.SAPRODONTIA,Lists.newArrayList()),
                new DistrictChartVO.AgeData(ReportConst.SAPRODONTIA_LOSS,Lists.newArrayList()),
                new DistrictChartVO.AgeData(ReportConst.SAPRODONTIA_REPAIR,Lists.newArrayList())
        );

        dynamicAgeSegmentList.forEach(age-> {
            y.add(AgeSegmentEnum.get(age).getDesc());
            SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(ageMap.get(age)).ratioNotSymbol().ratio();
            setAgeData(x,saprodontiaNum);
        });
        ageChart.setY(y);
        ageChart.setX(x);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorChart(ageChart);
    }

    private void setAgeData(List<DistrictChartVO.AgeData> data, SaprodontiaNum saprodontiaNum){
        data.get(0).getData().add(saprodontiaNum.saprodontiaRatio);
        data.get(1).getData().add(saprodontiaNum.saprodontiaLossRatio);
        data.get(2).getData().add(saprodontiaNum.saprodontiaRepairRatio);
    }

    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);
        Map<Integer, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        dynamicAgeSegmentList.forEach(age-> getSaprodontiaNum(age,ageMap.get(age),saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2){
            DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new DistrictSaprodontiaMonitorVO.SaprodontiaAgeVariableVO();
            saprodontiaAgeVariableVO.setSaprodontiaRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum,SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaLossRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum,SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum,SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
        }
    }


    private DistrictSaprodontiaMonitorVO.AgeRatio getAgeRatio(Map<Integer, SaprodontiaNum> saprodontiaNumMap, Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(saprodontiaNumMap, function,mapper);
        DistrictSaprodontiaMonitorVO.AgeRatio ageRatio = new DistrictSaprodontiaMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
        return ageRatio;
    }


    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegmentList.forEach(age-> getSaprodontiaAgeTable(age,ageMap.get(age),tableList));
        getSaprodontiaAgeTable(1000,statConclusionList,tableList);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorTableList(tableList);
    }


    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList) {

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol();
        DistrictSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaAgeMonitorTable = buildTable(saprodontiaNum);
        saprodontiaAgeMonitorTable.setItemName(itemName);
        tableList.add(saprodontiaAgeMonitorTable);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class SaprodontiaNum extends EntityFunction{

        /**
         * 筛查人数
         */
        private Integer validScreeningNum;

        /**
         * 龋失补牙数
         */
        private Integer dmftNum;

        /**
         * 龋均
         */
        private String dmftRatio;

        /**
         * 有龋人数
         */
        private Integer saprodontiaNum;

        /**
         * 龋失人数
         */
        private Integer saprodontiaLossNum;

        /**
         * 龋补人数
         */
        private Integer saprodontiaRepairNum;

        /**
         * 龋患（失、补）人数
         */
        private Integer saprodontiaLossAndRepairNum;

        /**
         * 龋患（失、补）牙数
         */
        private Integer saprodontiaLossAndRepairTeethNum;

        // ========= 不带% =============

        /**
         * 龋患率
         */
        private BigDecimal saprodontiaRatio;
        /**
         * 龋失率
         */
        private BigDecimal saprodontiaLossRatio;

        /**
         * 龋补率
         */
        private BigDecimal saprodontiaRepairRatio;
        /**
         * 龋患（失、补）率
         */
        private BigDecimal saprodontiaLossAndRepairRatio;

        /**
         * 龋患（失、补）构成比
         */
        private BigDecimal saprodontiaLossAndRepairTeethRatio;

        // ========= 带% =============

        /**
         * 龋患率
         */
        private String saprodontiaRatioStr;
        /**
         * 龋失率
         */
        private String saprodontiaLossRatioStr;
        /**
         * 龋补率
         */
        private String saprodontiaRepairRatioStr;
        /**
         * 龋患（失、补）率
         */
        private String saprodontiaLossAndRepairRatioStr;

        /**
         * 龋患（失、补）构成比
         */
        private String saprodontiaLossAndRepairTeethRatioStr;

        /**
         * 性别
         */
        private Integer gender;

        public SaprodontiaNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }

        public SaprodontiaNum build(List<StatConclusion> statConclusionList){
            if (Objects.isNull(statConclusionList)){
                this.validScreeningNum=ReportConst.ZERO;
                this.dmftNum=ReportConst.ZERO;
                this.saprodontiaLossAndRepairTeethNum=ReportConst.ZERO;
                this.saprodontiaNum=ReportConst.ZERO;
                this.saprodontiaLossNum=ReportConst.ZERO;
                this.saprodontiaRepairNum=ReportConst.ZERO;
                this.saprodontiaLossAndRepairNum=ReportConst.ZERO;
                return this;
            }
            this.validScreeningNum = statConclusionList.size();

            Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(ReportConst.ZERO);
            this.dmftNum = statConclusionList.stream().filter(Objects::nonNull).filter(predicateTrue).mapToInt(totalFunction).sum();

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(ReportConst.ZERO) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(ReportConst.ZERO);
            this.saprodontiaLossAndRepairTeethNum  = statConclusionList.stream().filter(Objects::nonNull).filter(lossAndRepairPredicateTrue).mapToInt(lossAndRepairTotalFunction).sum();

            this.saprodontiaNum  = getCount(statConclusionList, StatConclusion::getIsSaprodontia);
            this.saprodontiaLossNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaLoss);
            this.saprodontiaRepairNum = getCount(statConclusionList, StatConclusion::getIsSaprodontiaRepair);
            this.saprodontiaLossAndRepairNum = (int)statConclusionList.stream()
                    .filter(lossAndRepairPredicateTrue).count();

            return this;
        }

        /**
         * 不带%
         */
        public SaprodontiaNum ratioNotSymbol(){
            this.dmftRatio = Optional.ofNullable(MathUtil.num(dmftNum,getTotal())).orElse(ReportConst.ZERO_STR);
            this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum,getTotal());
            this.saprodontiaLossRatio =getRatioNotSymbol(saprodontiaLossNum,getTotal());
            this.saprodontiaRepairRatio = getRatioNotSymbol(saprodontiaRepairNum,getTotal());
            this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum,getTotal());
            this.saprodontiaLossAndRepairTeethRatio =getRatioNotSymbol(saprodontiaLossAndRepairTeethNum,dmftNum);
            return this;
        }

        /**
         * 带%
         */
        public SaprodontiaNum ratio(){
            this.dmftRatio = Optional.ofNullable(MathUtil.num(dmftNum,getTotal())).orElse(ReportConst.ZERO_STR);
            this.saprodontiaRatioStr = getRatio(saprodontiaNum,getTotal());
            this.saprodontiaLossRatioStr =getRatio(saprodontiaLossNum,getTotal());
            this.saprodontiaRepairRatioStr = getRatio(saprodontiaRepairNum,getTotal());
            this.saprodontiaLossAndRepairRatioStr =getRatio(saprodontiaLossAndRepairNum,getTotal());
            this.saprodontiaLossAndRepairTeethRatioStr =getRatio(saprodontiaLossAndRepairTeethNum,dmftNum);
            return this;
        }
    }

    private static Integer getTotal(){
        return map.get(0);
    }
}
