package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 龋齿监测
 *
 * @author hang.yuan 2022/5/25 15:04
 */
@Service
public class SchoolSaprodontiaMonitorService {

    @Autowired
    private ReportChartService reportChartService;

    /**
     * 龋齿监测结果
     */
    public void getSchoolSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO = new SchoolSaprodontiaMonitorVO();

        SaprodontiaNum.MAP.put(0,statConclusionList.size());
        //说明变量
        getSaprodontiaMonitorVariableVO(statConclusionList,schoolSaprodontiaMonitorVO);
        //不同性别
        getSaprodontiaSexVO(statConclusionList,schoolSaprodontiaMonitorVO);
        //不同年级
        getSaprodontiaGradeVO(statConclusionList,schoolSaprodontiaMonitorVO);
        //不同年龄段
        getSaprodontiaAgeVO(statConclusionList,schoolSaprodontiaMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolSaprodontiaMonitorVO(schoolSaprodontiaMonitorVO);
    }

    /**
     * 龋齿监测结果-说明变量
     */
    private void getSaprodontiaMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().ratio();
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = buildSaprodontiaMonitorVariableVO(saprodontiaNum);
        schoolSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }
    private SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO buildSaprodontiaMonitorVariableVO(SaprodontiaNum saprodontiaNum){
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(saprodontiaNum.getDmftRatio());
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(saprodontiaNum.getSaprodontiaRatioStr());
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(saprodontiaNum.getSaprodontiaRepairRatioStr());
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(saprodontiaNum.getSaprodontiaLossAndRepairRatioStr());
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.getSaprodontiaLossAndRepairTeethRatioStr());
        return saprodontiaMonitorVariableVO;
    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaSexVO saprodontiaSexVO = new SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList,saprodontiaSexVO);
        reportChartService.getSexMonitorChart(statConclusionList,saprodontiaSexVO);
        schoolSaprodontiaMonitorVO.setSaprodontiaSexVO(saprodontiaSexVO);

    }


    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<SaprodontiaNum> saprodontiaSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getSaprodontiaNum(gender,list,saprodontiaSexList));

        if (saprodontiaSexList.size() >= 1){
            SaprodontiaSexVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new SaprodontiaSexVO.SaprodontiaSexVariableVO();
            saprodontiaSexVariableVO.setSaprodontiaRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRatio, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaLossRatio, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRepairRatio, SaprodontiaNum::getSaprodontiaRepairRatioStr));
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

    private SexCompare getRatioCompare(List<SaprodontiaNum> saprodontiaSexList, Function<SaprodontiaNum,BigDecimal> function, Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function).reversed());
        SexCompare sex = new SexCompare();
        if (saprodontiaSexList.size() == 1){
            SaprodontiaNum num = saprodontiaSexList.get(0);
            if (Objects.equals(GenderEnum.MALE.type,num.getGender())){
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
                               SexCompare sex, String backSex, String zeroRatio) {

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
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaSexMonitorTableList(tableList);

    }

    private SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList,Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        SaprodontiaMonitorTable saprodontiaMonitorTable = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol().buildTable();
        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
       return saprodontiaMonitorTable;
    }



    /**
     * 龋齿监测结果-不同年级
     */
    private void getSaprodontiaGradeVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO = new SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO();
        getSaprodontiaGradeVariableVO(statConclusionList,saprodontiaGradeVO);
        getSaprodontiaGradeMonitorTableList(statConclusionList,saprodontiaGradeVO);
        getSaprodontiaGradeMonitorChart(statConclusionList,saprodontiaGradeVO);
        schoolSaprodontiaMonitorVO.setSaprodontiaGradeVO(saprodontiaGradeVO);

    }

    private void getSaprodontiaGradeMonitorChart(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        ChartVO.Chart chart = new ChartVO.Chart();
        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList(
                new ChartVO.ChartData(ReportConst.SAPRODONTIA,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.SAPRODONTIA_LOSS,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.SAPRODONTIA_REPAIR,Lists.newArrayList())
        );

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            x.add(gradeCodeEnum.getName());
            SaprodontiaNum num = new SaprodontiaNum().build(list).ratioNotSymbol().ratio();
            setGradeData(y,num);
        });
        chart.setX(x);
        chart.setY(y);
        saprodontiaGradeVO.setSaprodontiaGradeMonitorChart(chart);

    }

    private void setGradeData(List<ChartVO.ChartData> y, SaprodontiaNum num){
        y.get(0).getData().add(num.getSaprodontiaRatio());
        y.get(1).getData().add(num.getSaprodontiaLossRatio());
        y.get(2).getData().add(num.getSaprodontiaRepairRatio());
    }

    /**
     * 龋齿监测结果-不同学龄段-说明变量
     */
    private void getSaprodontiaGradeVariableVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaGradeVariableVO saprodontiaGradeVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaGradeVariableVO();
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getSaprodontiaNum(gradeCodeEnum.getName(),list,saprodontiaNumMap);
        });

        if (saprodontiaNumMap.size() >= 2){
            saprodontiaGradeVariableVO.setSaprodontiaRatio(getGradeRatio(saprodontiaNumMap,SaprodontiaNum::getSaprodontiaNum, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaGradeVariableVO.setSaprodontiaLossRatio(getGradeRatio(saprodontiaNumMap,SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaGradeVariableVO.setSaprodontiaRepairRatio(getGradeRatio(saprodontiaNumMap,SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaGradeVO.setSaprodontiaGradeVariableVO(saprodontiaGradeVariableVO);
        }

    }


    private SchoolSaprodontiaMonitorVO.GradeRatio getGradeRatio(Map<String, SaprodontiaNum> saprodontiaNumMap,Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper){
        if (CollectionUtil.isEmpty(saprodontiaNumMap)){
            return null;
        }
        SchoolSaprodontiaMonitorVO.GradeRatio gradeRatio = new SchoolSaprodontiaMonitorVO.GradeRatio();
        TwoTuple<String, String> maxTuple = ReportUtil.getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<String, String> minTuple = ReportUtil.getMinMap(saprodontiaNumMap, function,mapper);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinGrade(minTuple.getFirst());
        gradeRatio.setMinRatio(minTuple.getSecond());
        return gradeRatio;
    }


    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaGradeMonitorTableList(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().sorted(Comparator.comparing(StatConclusion::getSchoolGradeCode)).collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        List<SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getSaprodontiaGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getSaprodontiaGrade(statConclusionList,"合计",tableList);
        saprodontiaGradeVO.setSaprodontiaGradeMonitorTableList(tableList);
    }

    private void getSaprodontiaGrade(List<StatConclusion> statConclusionList,String grade,List<SaprodontiaMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaMonitorTable saprodontiaMonitorTable = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol().buildTable();
        saprodontiaMonitorTable.setItemName(grade);
        gradeList.add(saprodontiaMonitorTable);
    }

    /**
     * 龋齿监测结果-不同年龄
     */
    private void getSaprodontiaAgeVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaAgeVO saprodontiaAgeVO = new SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList,saprodontiaAgeVO);
        reportChartService.getAgeMonitorChart(statConclusionList,saprodontiaAgeVO);
        schoolSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }


    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getSaprodontiaNum(age,list,saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2){
            SaprodontiaAgeVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new SaprodontiaAgeVO.SaprodontiaAgeVariableVO();
            saprodontiaAgeVariableVO.setSaprodontiaRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaLossRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
        }
    }

    private SaprodontiaAgeVO.AgeRatio getAgeRatio(Map<Integer, SaprodontiaNum> saprodontiaNumMap, Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaNumMap)){
            return null;
        }
        SaprodontiaAgeVO.AgeRatio ageRatio = new SaprodontiaAgeVO.AgeRatio();
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(saprodontiaNumMap, function,mapper);
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
        return ageRatio;
    }

    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age->getSaprodontiaAgeTable(age,ageMap.get(age),tableList));
        getSaprodontiaAgeTable(1000,statConclusionList,tableList);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorTableList(tableList);

    }


    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SaprodontiaMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }
        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        SaprodontiaMonitorTable saprodontiaAgeMonitorTable = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol().buildTable();
        saprodontiaAgeMonitorTable.setItemName(itemName);
        tableList.add(saprodontiaAgeMonitorTable);
    }

}
