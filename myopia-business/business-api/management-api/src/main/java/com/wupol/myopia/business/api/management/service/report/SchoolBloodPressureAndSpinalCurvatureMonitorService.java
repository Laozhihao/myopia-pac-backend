package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.*;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolChartVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 血压与脊柱弯曲监测
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class SchoolBloodPressureAndSpinalCurvatureMonitorService {

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    public void getSchoolBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO();

        map.put(0,statConclusionList.size());
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
        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvature = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().ratio();

        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorVariableVO variableVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorVariableVO();
        variableVO.setAbnormalSpineCurvatureRatio(bloodPressureAndSpinalCurvature.abnormalSpineCurvatureRatioStr);
        variableVO.setHighBloodPressureRatio(bloodPressureAndSpinalCurvature.highBloodPressureRatioStr);

        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureMonitorVariableVO(variableVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO schoolBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO();
        getBloodPressureAndSpinalCurvatureSexVariableVO(statConclusionList,sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorTableList(statConclusionList,sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorChart(statConclusionList,sexVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSexVO(sexVO);
    }

    private void getBloodPressureAndSpinalCurvatureSexMonitorChart(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolChartVO.Chart chart = new SchoolChartVO.Chart();
        List<String> x = Lists.newArrayList(ReportConst.HIGH_BLOOD_PRESSURE,ReportConst.ABNORMAL_SPINE_CURVATURE);
        List<SchoolChartVO.ChartData> y = Lists.newArrayList(
                new SchoolChartVO.ChartData(GenderEnum.MALE.desc,Lists.newArrayList()),
                new SchoolChartVO.ChartData(GenderEnum.FEMALE.desc,Lists.newArrayList())
        );

        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        getChartData(genderMap.get(GenderEnum.MALE.type), y,0);
        getChartData(genderMap.get(GenderEnum.FEMALE.type), y,1);

        chart.setX(x);
        chart.setY(y);
        sexVO.setBloodPressureAndSpinalCurvatureSexMonitorChart(chart);
    }
    private void getChartData(List<StatConclusion> statConclusionList,List<SchoolChartVO.ChartData> y,Integer index) {
        BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol().ratio();
        y.get(index).getData().add(num.highBloodPressureRatio);
        y.get(index).getData().add(num.abnormalSpineCurvatureRatio);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSexVariableVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getBloodPressureAndSpinalCurvatureNum(gender,list,bloodPressureAndSpinalCurvatureSexList));

        if (bloodPressureAndSpinalCurvatureSexList.size() >= 1){
            SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVariableVO sexVariableVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVariableVO();
            sexVariableVO.setAbnormalSpineCurvatureRatioCompare(getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatio, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            sexVariableVO.setHighBloodPressureRatioCompare(getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatio, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));

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

    private SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex getRatioCompare(List<BloodPressureAndSpinalCurvatureNum> sexList, Function<BloodPressureAndSpinalCurvatureNum,BigDecimal> function, Function<BloodPressureAndSpinalCurvatureNum,String> mapper) {
        if (CollectionUtil.isEmpty(sexList)){
            return null;
        }
        CollectionUtil.sort(sexList, Comparator.comparing(function).reversed());
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex sex = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex();
        if (sexList.size() == 1){
            BloodPressureAndSpinalCurvatureNum num = sexList.get(0);
            if (Objects.equals(GenderEnum.MALE.type,num.gender)){
                setSexCompare(num,null, mapper, sex,GenderEnum.FEMALE.desc,ReportConst.ZERO_RATIO_STR);
            }else {
                setSexCompare(num,null, mapper, sex,GenderEnum.MALE.desc,ReportConst.ZERO_RATIO_STR);
            }
        }
        if (sexList.size() == 2){
            BloodPressureAndSpinalCurvatureNum forward = sexList.get(0);
            BloodPressureAndSpinalCurvatureNum back = sexList.get(1);
            setSexCompare(forward,back, mapper, sex,null,null);
        }
        return sex;
    }

    private void setSexCompare(BloodPressureAndSpinalCurvatureNum forward, BloodPressureAndSpinalCurvatureNum back, Function<BloodPressureAndSpinalCurvatureNum, String> mapper,
                               SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex sex,
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

    private void setSymbol(SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex sex, String forwar, String back) {
        if (Objects.equals(forwar, back)){
            sex.setSymbol("=");
        }else {
            sex.setSymbol(">");
        }
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSexMonitorTableList(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList =Lists.newArrayList();
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable maleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable femaleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable totalTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setBloodPressureAndSpinalCurvatureSexMonitorTableList(tableList);
    }

    private SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable getBloodPressureAndSpinalCurvatureSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum = new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol();

        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= buildTable(bloodPressureAndSpinalCurvatureNum);
        if (Objects.equals(10,gender)){
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName("合计");
        }else {
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return bloodPressureAndSpinalCurvatureMonitorTable;
    }

    public SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable buildTable(BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum){
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable();
        bloodPressureAndSpinalCurvatureMonitorTable.setValidScreeningNum(bloodPressureAndSpinalCurvatureNum.validScreeningNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setAbnormalSpineCurvatureNum(bloodPressureAndSpinalCurvatureNum.abnormalSpineCurvatureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setAbnormalSpineCurvatureRatio(bloodPressureAndSpinalCurvatureNum.abnormalSpineCurvatureRatio);
        bloodPressureAndSpinalCurvatureMonitorTable.setHighBloodPressureNum(bloodPressureAndSpinalCurvatureNum.highBloodPressureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setHighBloodPressureRatio(bloodPressureAndSpinalCurvatureNum.highBloodPressureRatio);
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
        SchoolChartVO.Chart chart = new SchoolChartVO.Chart();

        List<String> x = Lists.newArrayList();
        List<SchoolChartVO.ChartData> y = Lists.newArrayList(
                new SchoolChartVO.ChartData(ReportConst.HIGH_BLOOD_PRESSURE,Lists.newArrayList()),
                new SchoolChartVO.ChartData(ReportConst.ABNORMAL_SPINE_CURVATURE,Lists.newArrayList())
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
    private void setGradeData(List<SchoolChartVO.ChartData> y, BloodPressureAndSpinalCurvatureNum num){
        y.get(0).getData().add(num.highBloodPressureRatio);
        y.get(1).getData().add(num.abnormalSpineCurvatureRatio);
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
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,String> getMaxMap(Map<K, T> map, Function<T,Integer> function ,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
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
        List<SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getBloodPressureAndSpinalCurvatureGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getBloodPressureAndSpinalCurvatureGrade(statConclusionList,"合计",tableList);

        schoolAgeVO.setBloodPressureAndSpinalCurvatureGradeMonitorTableList(tableList);
    }


    private void getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList,String grade,List<SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= buildTable(bloodPressureAndSpinalCurvatureNum);
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
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO();
        getBloodPressureAndSpinalCurvatureAgeVariableVO(statConclusionList,ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorTableList(statConclusionList,ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorChart(statConclusionList,ageVO);
        schoolBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureAgeVO(ageVO);
    }

    private void getBloodPressureAndSpinalCurvatureAgeMonitorChart(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolChartVO.AgeChart ageChart = new SchoolChartVO.AgeChart();
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        List<Integer> dynamicAgeSegmentList = ReportUtil.dynamicAgeSegment(statConclusionList);
        List<String> y = Lists.newArrayList();
        List<SchoolChartVO.AgeData> x = Lists.newArrayList(
                new SchoolChartVO.AgeData(ReportConst.HIGH_BLOOD_PRESSURE,Lists.newArrayList()),
                new SchoolChartVO.AgeData(ReportConst.ABNORMAL_SPINE_CURVATURE,Lists.newArrayList())
        );
        dynamicAgeSegmentList.forEach(age-> {
            y.add(AgeSegmentEnum.get(age).getDesc());
            BloodPressureAndSpinalCurvatureNum num = new BloodPressureAndSpinalCurvatureNum().build(ageMap.get(age)).ratioNotSymbol().ratio();
            setAgeData(x,num);
        });
        ageChart.setY(y);
        ageChart.setX(x);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorChart(ageChart);
    }
    private void setAgeData(List<SchoolChartVO.AgeData> data, BloodPressureAndSpinalCurvatureNum num){
        data.get(0).getData().add(num.highBloodPressureRatio);
        data.get(1).getData().add(num.abnormalSpineCurvatureRatio);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureAgeVariableVO(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
        Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getBloodPressureAndSpinalCurvatureNum(age,list,bloodPressureAndSpinalCurvatureNumMap));

        if (bloodPressureAndSpinalCurvatureNumMap.size() >= 2){
            SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVariableVO ageVariableVO = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVariableVO();
            ageVariableVO.setAbnormalSpineCurvatureRatio(getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr));
            ageVariableVO.setHighBloodPressureRatio(getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr));
            ageVO.setBloodPressureAndSpinalCurvatureAgeVariableVO(ageVariableVO);
        }
    }

    private SchoolBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio getAgeRatio(Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap, Function<BloodPressureAndSpinalCurvatureNum,Integer> function, Function<BloodPressureAndSpinalCurvatureNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = ReportUtil.getMaxMap(bloodPressureAndSpinalCurvatureNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = ReportUtil.getMinMap(bloodPressureAndSpinalCurvatureNumMap, function,mapper);
        SchoolBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio ageRatio = new SchoolBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
        return ageRatio;
    }


    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureAgeMonitorTableList(List<StatConclusion> statConclusionList, SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getBloodPressureAndSpinalCurvatureAgeTable(age,list,tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000,statConclusionList,tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum = new BloodPressureAndSpinalCurvatureNum().build(conclusionlist).ratioNotSymbol();

        SchoolBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = buildTable(bloodPressureAndSpinalCurvatureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(itemName);
        tableList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class BloodPressureAndSpinalCurvatureNum extends EntityFunction{

        /**
         * 筛查人数
         */
        private Integer validScreeningNum;
        /**
         * 血压偏高人数
         */
        private Integer highBloodPressureNum;

        /**
         * 脊柱弯曲异常人数
         */
        private Integer abnormalSpineCurvatureNum;


        //============= 不带% ============
        /**
         * 血压偏高率
         */
        private BigDecimal highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        private BigDecimal abnormalSpineCurvatureRatio;


        //============= 带% ============
        /**
         * 血压偏高率
         */
        private String highBloodPressureRatioStr;

        /**
         * 脊柱弯曲异常率
         */
        private String abnormalSpineCurvatureRatioStr;

        /**
         * 性别
         */
        private Integer gender;

        public BloodPressureAndSpinalCurvatureNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();
            this.abnormalSpineCurvatureNum = getCount(statConclusionList,StatConclusion::getIsSpinalCurvature);
            this.highBloodPressureNum = (int)statConclusionList.stream()
                    .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

            return this;
        }

        /**
         * 不带%
         */
        public BloodPressureAndSpinalCurvatureNum ratioNotSymbol(){
            this.abnormalSpineCurvatureRatio = getRatioNotSymbol(abnormalSpineCurvatureNum,getTotal());
            this.highBloodPressureRatio = getRatioNotSymbol(highBloodPressureNum,getTotal());
            return this;
        }

        /**
         * 带%
         */
        public BloodPressureAndSpinalCurvatureNum ratio(){
            this.abnormalSpineCurvatureRatioStr = getRatio(abnormalSpineCurvatureNum,getTotal());
            this.highBloodPressureRatioStr = getRatio(highBloodPressureNum,getTotal());
            return this;
        }

        public BloodPressureAndSpinalCurvatureNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }

    private static Integer getTotal(){
        return map.get(0);
    }

}
