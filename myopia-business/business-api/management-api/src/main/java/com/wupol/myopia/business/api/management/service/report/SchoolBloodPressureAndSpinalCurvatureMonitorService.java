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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
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

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO();

        BloodPressureAndSpinalCurvatureNum.MAP.put(0,statConclusionList.size());
        //说明变量
        getBloodPressureAndSpinalCurvatureMonitorVariableVO(statConclusionList,schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同性别
        getBloodPressureAndSpinalCurvatureSexVO(statConclusionList,schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年级
        getBloodPressureAndSpinalCurvatureGradeVO(statConclusionList,schoolBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年龄段
        getBloodPressureAndSpinalCurvatureAgeVO(statConclusionList,schoolBloodPressureAndSpinalCurvatureMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolBloodPressureAndSpinalCurvatureMonitorVO(schoolBloodPressureAndSpinalCurvatureMonitorVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
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
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        BloodPressureAndSpinalCurvatureSexVO sexVO = new BloodPressureAndSpinalCurvatureSexVO();
        getBloodPressureAndSpinalCurvatureSexVariableVO(statConclusionList,sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorTableList(statConclusionList,sexVO);
        reportChartService.getSexMonitorChart(statConclusionList,sexVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSexVO(sexVO);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSexVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getBloodPressureAndSpinalCurvatureNum(gender,list,bloodPressureAndSpinalCurvatureSexList));

        if (bloodPressureAndSpinalCurvatureSexList.size() >= 1){
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

    private <K>void getBloodPressureAndSpinalCurvatureNum(K key, List<StatConclusion> statConclusionList,Map<K,BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap){
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        bloodPressureAndSpinalCurvatureNumMap.put(key,build);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSexMonitorTableList(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList =Lists.newArrayList();
        BloodPressureAndSpinalCurvatureMonitorTable maleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        BloodPressureAndSpinalCurvatureMonitorTable femaleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        BloodPressureAndSpinalCurvatureMonitorTable totalTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setBloodPressureAndSpinalCurvatureSexMonitorTableList(tableList);
    }

    private BloodPressureAndSpinalCurvatureMonitorTable getBloodPressureAndSpinalCurvatureSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol().buildTable();
        if (Objects.equals(10,gender)){
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName("合计");
        }else {
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return bloodPressureAndSpinalCurvatureMonitorTable;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段
     */
    private void getBloodPressureAndSpinalCurvatureGradeVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVO gradeVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVO();
        getBloodPressureAndSpinalCurvatureGradeVariableVO(statConclusionList,gradeVO);
        getBloodPressureAndSpinalCurvatureGradeMonitorTableList(statConclusionList,gradeVO);
        getBloodPressureAndSpinalCurvatureGradeMonitorChart(statConclusionList,gradeVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureGradeVO(gradeVO);
    }

    private void getBloodPressureAndSpinalCurvatureGradeMonitorChart(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVO gradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        ChartVO.Chart chart = new ChartVO.Chart();

        List<String> x = Lists.newArrayList();
        List<ChartVO.ChartData> y = Lists.newArrayList(
                new ChartVO.ChartData(ReportConst.HIGH_BLOOD_PRESSURE,Lists.newArrayList()),
                new ChartVO.ChartData(ReportConst.ABNORMAL_SPINE_CURVATURE,Lists.newArrayList())
        );
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            x.add(gradeCodeEnum.getName());
            BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(list).ratioNotSymbol().ratio();
            setGradeData(y,num);
        });
        chart.setX(x);
        chart.setY(y);
        gradeVO.setBloodPressureAndSpinalCurvatureGradeMonitorChart(chart);
    }
    private void setGradeData(List<ChartVO.ChartData> y, BloodPressureAndSpinalCurvatureNum num){
        y.get(0).getData().add(num.getHighBloodPressureRatio());
        y.get(1).getData().add(num.getAbnormalSpineCurvatureRatio());
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureGradeVariableVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVO schoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVariableVO schoolAgeVariableVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVariableVO();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getBloodPressureAndSpinalCurvatureNum(gradeCodeEnum.getName(),list,bloodPressureAndSpinalCurvatureNumMap);
        });

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2){
            schoolAgeVariableVO.setAbnormalSpineCurvatureRatio(getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            schoolAgeVariableVO.setHighBloodPressureRatio(getGradeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
            schoolAgeVO.setBloodPressureAndSpinalCurvatureGradeVariableVO(schoolAgeVariableVO);
        }
    }


    private SchoolBloodPressureAndSpinalCurvatureMonitorVO.GradeRatio getGradeRatio(Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap,
                                                                                    Function<BloodPressureAndSpinalCurvatureNum,Integer> function ,Function<BloodPressureAndSpinalCurvatureNum,String> mapper){
        if (CollectionUtil.isEmpty(bloodPressureAndSpinalCurvatureNumMap)){
            return null;
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.GradeRatio gradeRatio = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.GradeRatio();
        TwoTuple<String, String> maxTuple = ReportUtil.getMaxMap(bloodPressureAndSpinalCurvatureNumMap, function, mapper);
        TwoTuple<String, String> minTuple = ReportUtil.getMinMap(bloodPressureAndSpinalCurvatureNumMap, function, mapper);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinGrade(minTuple.getFirst());
        gradeRatio.setMinRatio(minTuple.getSecond());
        return gradeRatio;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureGradeMonitorTableList(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureGradeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        gradeCodeMap = CollectionUtil.sort(gradeCodeMap, String::compareTo);
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getBloodPressureAndSpinalCurvatureGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getBloodPressureAndSpinalCurvatureGrade(statConclusionList,"合计",tableList);

        schoolAgeVO.setBloodPressureAndSpinalCurvatureGradeMonitorTableList(tableList);
    }


    private void getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList,String grade,List<BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(grade);
        gradeList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段
     */
    private void getBloodPressureAndSpinalCurvatureAgeVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        BloodPressureAndSpinalCurvatureAgeVO ageVO = new BloodPressureAndSpinalCurvatureAgeVO();
        getBloodPressureAndSpinalCurvatureAgeVariableVO(statConclusionList,ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorTableList(statConclusionList,ageVO);
        reportChartService.getAgeMonitorChart(statConclusionList,ageVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureAgeVO(ageVO);
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureAgeVariableVO(List<StatConclusion> statConclusionList, BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getBloodPressureAndSpinalCurvatureNum(age,list,bloodPressureAndSpinalCurvatureNumMap));

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2){
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
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        List<Integer> dynamicAgeSegment = ReportUtil.dynamicAgeSegment(statConclusionList);
        dynamicAgeSegment.forEach(age->getBloodPressureAndSpinalCurvatureAgeTable(age,ageMap.get(age),tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000,statConclusionList,tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> conclusionlist, List<BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol().buildTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(itemName);
        tableList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

}
