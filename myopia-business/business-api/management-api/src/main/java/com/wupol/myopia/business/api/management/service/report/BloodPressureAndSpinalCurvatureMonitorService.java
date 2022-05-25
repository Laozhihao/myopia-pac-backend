package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictBloodPressureAndSpinalCurvatureMonitorVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
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
public class BloodPressureAndSpinalCurvatureMonitorService {

    /**
     * 血压与脊柱弯曲异常监测结果
     */
    public void getDistrictBloodPressureAndSpinalCurvatureMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO();

        //说明变量
        getBloodPressureAndSpinalCurvatureMonitorVariableVO(statConclusionList,districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同性别
        getBloodPressureAndSpinalCurvatureSexVO(statConclusionList,districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同学龄段
        getBloodPressureAndSpinalCurvatureSchoolAgeVO(statConclusionList,districtBloodPressureAndSpinalCurvatureMonitorVO);
        //不同年龄段
        getBloodPressureAndSpinalCurvatureAgeVO(statConclusionList,districtBloodPressureAndSpinalCurvatureMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictBloodPressureAndSpinalCurvatureMonitorVO(districtBloodPressureAndSpinalCurvatureMonitorVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvature = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorVariableVO variableVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorVariableVO();
        variableVO.setAbnormalSpineCurvatureRatio(bloodPressureAndSpinalCurvature.abnormalSpineCurvatureRatio);
        variableVO.setHighBloodPressureRatio(bloodPressureAndSpinalCurvature.highBloodPressureRatio);

        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureMonitorVariableVO(variableVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别
     */
    private void getBloodPressureAndSpinalCurvatureSexVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO();
        getBloodPressureAndSpinalCurvatureSexVariableVO(statConclusionList,sexVO);
        getBloodPressureAndSpinalCurvatureSexMonitorTableList(statConclusionList,sexVO);

        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSexVO(sexVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSexVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getBloodPressureAndSpinalCurvatureNum(gender,list,bloodPressureAndSpinalCurvatureSexList));

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex abnormalSpineCurvature = getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureRatioStr);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex highBloodPressure = getRatioCompare(bloodPressureAndSpinalCurvatureSexList, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureRatioStr);

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVariableVO sexVariableVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVariableVO();
        sexVariableVO.setAbnormalSpineCurvatureRatioCompare(abnormalSpineCurvature);
        sexVariableVO.setHighBloodPressureRatioCompare(highBloodPressure);

        sexVO.setBloodPressureAndSpinalCurvatureSexVariableVO(sexVariableVO);

    }

    private void getBloodPressureAndSpinalCurvatureNum(Integer gender, List<StatConclusion> statConclusionList, List<BloodPressureAndSpinalCurvatureNum> sexList) {
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .setGender(gender)
                .build(statConclusionList);
        sexList.add(build);
    }

    private <K,E>void getBloodPressureAndSpinalCurvatureNum(K key, List<StatConclusion> statConclusionList,Map<K,BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap){
        BloodPressureAndSpinalCurvatureNum build = new BloodPressureAndSpinalCurvatureNum()
                .build(statConclusionList);
        bloodPressureAndSpinalCurvatureNumMap.put(key,build);
    }

    private DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex getRatioCompare(List<BloodPressureAndSpinalCurvatureNum> sexList, Function<BloodPressureAndSpinalCurvatureNum,Integer> function, Function<BloodPressureAndSpinalCurvatureNum,String> mapper) {
        if (CollectionUtil.isEmpty(sexList)){
            return null;
        }
        CollectionUtil.sort(sexList, Comparator.comparing(function));
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex sex = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSex();
        for (int i = 0; i < sexList.size(); i++) {
            BloodPressureAndSpinalCurvatureNum num = sexList.get(i);
            if (i==0){
                sex.setForwardSex(GenderEnum.getName(num.gender));
                sex.setForwardRatio(mapper.apply(num));
            }
            if (i==1){
                sex.setBackSex(GenderEnum.getName(num.gender));
                sex.setBackRatio(mapper.apply(num));
            }
        }
        return sex;
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同性别-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSexMonitorTableList(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList =Lists.newArrayList();
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable maleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable femaleTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable totalTable = getBloodPressureAndSpinalCurvatureSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setBloodPressureAndSpinalCurvatureSexMonitorTableList(tableList);
    }

    private DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable getBloodPressureAndSpinalCurvatureSexTable(List<StatConclusion> statConclusionList, Integer gender) {
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

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= buildTable(bloodPressureAndSpinalCurvatureNum);
        if (Objects.equals(10,gender)){
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName("合计");
        }else {
            bloodPressureAndSpinalCurvatureMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return bloodPressureAndSpinalCurvatureMonitorTable;
    }

    public DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable buildTable(BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum){
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable();
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
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVO();
        getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(statConclusionList,schoolAgeVO);
        getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(statConclusionList,schoolAgeVO);

        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureSchoolAgeVO(schoolAgeVO);

    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVariableVO schoolAgeVariableVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVariableVO();

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge primary = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList, SchoolAge.PRIMARY.code);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge junior = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList,SchoolAge.JUNIOR.code);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge high = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList,10);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge normalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList,SchoolAge.HIGH.code);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge vocationalHigh = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList,SchoolAge.VOCATIONAL_HIGH.code);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge university = getBloodPressureAndSpinalCurvatureSchoolAge(statConclusionList,SchoolAge.UNIVERSITY.code);

        schoolAgeVariableVO.setPrimarySchool(primary);
        schoolAgeVariableVO.setJuniorHighSchool(junior);
        schoolAgeVariableVO.setUniversity(university);
        if (Objects.nonNull(vocationalHigh)){
            schoolAgeVariableVO.setHighSchool(high);
            schoolAgeVariableVO.setNormalHighSchool(normalHigh);
            schoolAgeVariableVO.setVocationalHighSchool(vocationalHigh);
        }else {
            schoolAgeVariableVO.setHighSchool(high);
        }
        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeVariableVO(schoolAgeVariableVO);
    }

    private DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList, Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        Map<Integer, List<StatConclusion>> conclusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        if (Objects.equals(schoolAge,10)){
            List<StatConclusion> mergeList=Lists.newArrayList();
            List<StatConclusion> normalHigh = conclusionMap.get(SchoolAge.HIGH.code);
            if (CollectionUtil.isNotEmpty(normalHigh)){
                mergeList.addAll(normalHigh);
            }
            List<StatConclusion> vocationalHigh = conclusionMap.get(SchoolAge.VOCATIONAL_HIGH.code);
            if (CollectionUtil.isNotEmpty(vocationalHigh)){
                mergeList.addAll(vocationalHigh);
            }
            return getBloodPressureAndSpinalCurvatureSchoolAge(mergeList);
        }

        List<StatConclusion> conclusions = conclusionMap.get(schoolAge);

        return getBloodPressureAndSpinalCurvatureSchoolAge(conclusions);

    }
    private DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge getBloodPressureAndSpinalCurvatureSchoolAge(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }

        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge bloodPressureAndSpinalCurvatureSchoolAge = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAge();

        bloodPressureAndSpinalCurvatureSchoolAge.setAbnormalSpineCurvatureRatio(bloodPressureAndSpinalCurvatureNum.abnormalSpineCurvatureRatio);
        bloodPressureAndSpinalCurvatureSchoolAge.setHighBloodPressureRatio(bloodPressureAndSpinalCurvatureNum.highBloodPressureRatio);

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->getBloodPressureAndSpinalCurvatureNum(gradeCode,list,bloodPressureAndSpinalCurvatureNumMap));

        ThreeTuple<String, Integer,Integer> abnormalSpineCurvature = getMaxMap(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getValidScreeningNum);
        ThreeTuple<String, Integer,Integer> highBloodPressure = getMaxMap(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum,BloodPressureAndSpinalCurvatureNum::getValidScreeningNum);

        bloodPressureAndSpinalCurvatureSchoolAge.setMaxAbnormalSpineCurvatureRatio(new DistrictBloodPressureAndSpinalCurvatureMonitorVO.GradeRatio(abnormalSpineCurvature.getFirst(),MathUtil.ratio(abnormalSpineCurvature.getSecond(),abnormalSpineCurvature.getThird())));
        bloodPressureAndSpinalCurvatureSchoolAge.setMaxHighBloodPressureRatio(new DistrictBloodPressureAndSpinalCurvatureMonitorVO.GradeRatio(highBloodPressure.getFirst(),MathUtil.ratio(highBloodPressure.getSecond(),highBloodPressure.getThird())));

        return bloodPressureAndSpinalCurvatureSchoolAge;
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>ThreeTuple<K,Integer,Integer> getMaxMap(Map<K, T> map, Function<T,Integer> function ,Function<T,Integer> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return new ThreeTuple<>(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0),Optional.ofNullable(entry.getValue()).map(mapper).orElse(0));
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同学龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureSchoolAgeVO schoolAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList =Lists.newArrayList();
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> primaryList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,SchoolAge.PRIMARY.code);
        if (Objects.nonNull(primaryList)){
            tableList.addAll(primaryList);
        }
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> juniorList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,SchoolAge.JUNIOR.code);
        if (Objects.nonNull(juniorList)){
            tableList.addAll(juniorList);
        }

        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> highList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,10);
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,SchoolAge.HIGH.code);
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> vocationalHighList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList, SchoolAge.VOCATIONAL_HIGH.code);
        if (Objects.nonNull(vocationalHighList)){
            if (Objects.nonNull(highList)){
                tableList.addAll(highList);
            }
            if (Objects.nonNull(normalHighList)){
                tableList.addAll(normalHighList);
            }
            tableList.addAll(vocationalHighList);
        }else {
            if (Objects.nonNull(highList)){
                tableList.addAll(highList);
            }
        }

        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> universityList = getBloodPressureAndSpinalCurvatureSchoolAgeTable(statConclusionList,SchoolAge.UNIVERSITY.code);
        if (Objects.nonNull(universityList)){
            tableList.addAll(universityList);
        }

        schoolAgeVO.setBloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList(tableList);
    }

    private List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        if (Objects.equals(schoolAge,10)){
            List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> mergeList=Lists.newArrayList();
            List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> normalHighList = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, SchoolAge.HIGH.code);
            if (Objects.nonNull(normalHighList)){
                mergeList.addAll(normalHighList);
            }
            List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> vocationalHighList = getBloodPressureAndSpinalCurvatureGrade(statConclusionList, SchoolAge.HIGH.code);
            if (Objects.nonNull(vocationalHighList)){
                mergeList.addAll(vocationalHighList);
            }
            return mergeList;
        }

        return getBloodPressureAndSpinalCurvatureGrade(statConclusionList,schoolAge);
    }

    private List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> getBloodPressureAndSpinalCurvatureGrade(List<StatConclusion> statConclusionList,Integer schoolAge) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionList = statConclusionList.stream().filter(sc -> Objects.equals(sc.getSchoolAge(), schoolAge)).collect(Collectors.toList());
        Map<String, List<StatConclusion>> gradeCodeMap = conclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> gradeList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> getBloodPressureAndSpinalCurvatureSchoolAgeTable(list,grade,gradeList));
        getBloodPressureAndSpinalCurvatureSchoolAgeTable(conclusionList,SchoolAge.get(schoolAge).desc,gradeList);

        return gradeList;
    }

    private void getBloodPressureAndSpinalCurvatureSchoolAgeTable(List<StatConclusion> statConclusionList,String grade,List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        BloodPressureAndSpinalCurvatureNum bloodPressureAndSpinalCurvatureNum = new BloodPressureAndSpinalCurvatureNum().build(statConclusionList).ratioNotSymbol();
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable= buildTable(bloodPressureAndSpinalCurvatureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(grade);

        gradeList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段
     */
    private void getBloodPressureAndSpinalCurvatureAgeVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO districtBloodPressureAndSpinalCurvatureMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO();
        getBloodPressureAndSpinalCurvatureAgeVariableVO(statConclusionList,ageVO);
        getBloodPressureAndSpinalCurvatureAgeMonitorTableList(statConclusionList,ageVO);

        districtBloodPressureAndSpinalCurvatureMonitorVO.setBloodPressureAndSpinalCurvatureAgeVO(ageVO);
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-说明变量
     */
    private void getBloodPressureAndSpinalCurvatureAgeVariableVO(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getBloodPressureAndSpinalCurvatureNum(age,list,bloodPressureAndSpinalCurvatureNumMap));

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio abnormalSpineCurvature = getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getAbnormalSpineCurvatureNum, BloodPressureAndSpinalCurvatureNum::getValidScreeningNum);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio highBloodPressure = getAgeRatio(bloodPressureAndSpinalCurvatureNumMap, BloodPressureAndSpinalCurvatureNum::getHighBloodPressureNum, BloodPressureAndSpinalCurvatureNum::getValidScreeningNum);

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVariableVO ageVariableVO = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVariableVO();
        ageVariableVO.setAbnormalSpineCurvatureRatio(abnormalSpineCurvature);
        ageVariableVO.setHighBloodPressureRatio(highBloodPressure);

        ageVO.setBloodPressureAndSpinalCurvatureAgeVariableVO(ageVariableVO);
    }

    private DistrictBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio getAgeRatio(Map<Integer, BloodPressureAndSpinalCurvatureNum> bloodPressureAndSpinalCurvatureNumMap, Function<BloodPressureAndSpinalCurvatureNum,Integer> function, Function<BloodPressureAndSpinalCurvatureNum,Integer> mapper) {
        ThreeTuple<Integer, Integer,Integer> maxTuple = getMaxMap(bloodPressureAndSpinalCurvatureNumMap, function,mapper);
        ThreeTuple<Integer, Integer,Integer> minTuple = getMinMap(bloodPressureAndSpinalCurvatureNumMap, function,mapper);
        DistrictBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio ageRatio = new DistrictBloodPressureAndSpinalCurvatureMonitorVO.AgeRatio();
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(MathUtil.ratio(maxTuple.getSecond(),maxTuple.getThird()));
        ageRatio.setMinRatio(MathUtil.ratio(minTuple.getSecond(),minTuple.getThird()));
        return ageRatio;
    }

    private Integer getLessAge(Integer age){
        if (age < 6){
            return 6;
        }else if (age < 8){
            return 8;
        }else if (age < 10){
            return 10;
        }else if (age < 12){
            return 12;
        }else if (age < 14){
            return 14;
        }else if (age < 16){
            return 16;
        }else if (age < 18){
            return 18;
        }else {
            return 19;
        }
    }

    /**
     * 获取map中Value最小值及对应的Key
     */
    private <T,K>ThreeTuple<K,Integer,Integer> getMinMap(Map<K, T> map, Function<T,Integer> function,Function<T,Integer> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return new ThreeTuple<>(entry.getKey(),Optional.ofNullable(entry.getValue()).map(function).orElse(0),Optional.ofNullable(entry.getValue()).map(mapper).orElse(0));
    }

    /**
     * 血压与脊柱弯曲异常监测结果-不同年龄段-表格数据
     */
    private void getBloodPressureAndSpinalCurvatureAgeMonitorTableList(List<StatConclusion> statConclusionList, DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getBloodPressureAndSpinalCurvatureAgeTable(age,list,tableList));
        getBloodPressureAndSpinalCurvatureAgeTable(1000,statConclusionList,tableList);
        ageVO.setBloodPressureAndSpinalCurvatureAgeMonitorTableList(tableList);
    }

    private void getBloodPressureAndSpinalCurvatureAgeTable(Integer age, List<StatConclusion> conclusionlist, List<DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable> tableList) {
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

        DistrictBloodPressureAndSpinalCurvatureMonitorVO.BloodPressureAndSpinalCurvatureMonitorTable bloodPressureAndSpinalCurvatureMonitorTable = buildTable(bloodPressureAndSpinalCurvatureNum);
        bloodPressureAndSpinalCurvatureMonitorTable.setItemName(itemName);
        tableList.add(bloodPressureAndSpinalCurvatureMonitorTable);
    }

    @Data
    private static class BloodPressureAndSpinalCurvatureNum{

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

            this.abnormalSpineCurvatureNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsSpinalCurvature)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.highBloodPressureNum = (int)statConclusionList.stream()
                    .filter(sc->Objects.equals(Boolean.FALSE,sc.getIsNormalBloodPressure())).count();

            return this;
        }

        /**
         * 不带%
         */
        public BloodPressureAndSpinalCurvatureNum ratioNotSymbol(){
            if (Objects.nonNull(abnormalSpineCurvatureNum)){
                abnormalSpineCurvatureRatio = MathUtil.ratioNotSymbol(abnormalSpineCurvatureNum,validScreeningNum);
            }
            if (Objects.nonNull(highBloodPressureNum)){
                highBloodPressureRatio = MathUtil.ratioNotSymbol(highBloodPressureNum,validScreeningNum);
            }

            return this;
        }

        /**
         * 带%
         */
        public BloodPressureAndSpinalCurvatureNum ratio(){
            if (Objects.nonNull(abnormalSpineCurvatureNum)){
                abnormalSpineCurvatureRatioStr = MathUtil.ratio(abnormalSpineCurvatureNum,validScreeningNum);
            }
            if (Objects.nonNull(highBloodPressureNum)){
                highBloodPressureRatioStr = MathUtil.ratio(highBloodPressureNum,validScreeningNum);
            }

            return this;
        }

        public BloodPressureAndSpinalCurvatureNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }

}
