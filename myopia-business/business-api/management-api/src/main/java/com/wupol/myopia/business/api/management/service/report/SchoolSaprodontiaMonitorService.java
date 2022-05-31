package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolSaprodontiaMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
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
public class SchoolSaprodontiaMonitorService {

    /**
     * 龋齿监测结果
     */
    public void getSchoolSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO = new SchoolSaprodontiaMonitorVO();

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
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = buildSaprodontiaMonitorVariableVO(saprodontiaNum);
        schoolSaprodontiaMonitorVO.setSaprodontiaMonitorVariableVO(saprodontiaMonitorVariableVO);
    }
    private SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO buildSaprodontiaMonitorVariableVO(SaprodontiaNum saprodontiaNum){
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaMonitorVariableVO();
        saprodontiaMonitorVariableVO.setDmftRatio(saprodontiaNum.dmftRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRatio(saprodontiaNum.saprodontiaRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaRepairRatio(saprodontiaNum.saprodontiaRepairRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairRatio(saprodontiaNum.saprodontiaLossAndRepairRatio);
        saprodontiaMonitorVariableVO.setSaprodontiaLossAndRepairTeethRatio(saprodontiaNum.saprodontiaLossAndRepairTeethRatio);
        return saprodontiaMonitorVariableVO;
    }

    /**
     * 龋齿监测结果-不同性别
     */
    private void getSaprodontiaSexVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO = new SchoolSaprodontiaMonitorVO.SaprodontiaSexVO();
        getSaprodontiaSexVariableVO(statConclusionList,saprodontiaSexVO);
        getSaprodontiaSexMonitorTableList(statConclusionList,saprodontiaSexVO);
        schoolSaprodontiaMonitorVO.setSaprodontiaSexVO(saprodontiaSexVO);

    }

    /**
     * 龋齿监测结果-不同性别-说明变量
     */
    private void getSaprodontiaSexVariableVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<SaprodontiaNum> saprodontiaSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getSaprodontiaNum(gender,list,saprodontiaSexList));

        if (saprodontiaSexList.size() >= 2){
            SchoolSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
            saprodontiaSexVariableVO.setSaprodontiaRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaLossRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaSexVariableVO.setSaprodontiaRepairRatioCompare(getRatioCompare(saprodontiaSexList, SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRatioStr));
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

    private SchoolSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<SaprodontiaNum> saprodontiaSexList, Function<SaprodontiaNum,Integer> function, Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function));
        SchoolSaprodontiaMonitorVO.SaprodontiaSex sex = new SchoolSaprodontiaMonitorVO.SaprodontiaSex();
        for (int i = 0; i < saprodontiaSexList.size(); i++) {
            SaprodontiaNum num = saprodontiaSexList.get(i);
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
     * 龋齿监测结果-不同性别-表格数据
     */
    private void getSaprodontiaSexMonitorTableList(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaSexVO saprodontiaSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList =Lists.newArrayList();
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable maleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable femaleTable = getSaprodontiaSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable totalTable = getSaprodontiaSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        saprodontiaSexVO.setSaprodontiaSexMonitorTableList(tableList);

    }

    private SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable getSaprodontiaSexTable(List<StatConclusion> statConclusionList,Integer gender) {
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

        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);

        if (Objects.equals(10,gender)){
            saprodontiaMonitorTable.setItemName("合计");
        }else {
            saprodontiaMonitorTable.setItemName(GenderEnum.getName(gender));
        }
       return saprodontiaMonitorTable;
    }

    public SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable buildTable(SaprodontiaNum saprodontiaNum){
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable= new SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable();
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
    private void getSaprodontiaGradeVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO = new SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO();
        getSaprodontiaGradeVariableVO(statConclusionList,saprodontiaGradeVO);
        getSaprodontiaGradeMonitorTableList(statConclusionList,saprodontiaGradeVO);

        schoolSaprodontiaMonitorVO.setSaprodontiaGradeVO(saprodontiaGradeVO);

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
        TwoTuple<String, String> maxTuple = getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<String, String> minTuple = getMinMap(saprodontiaNumMap, function,mapper);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinGrade(minTuple.getFirst());
        gradeRatio.setMinRatio(minTuple.getSecond());
        return gradeRatio;
    }

    /**
     * 获取map中Value最大值及对应的Key
     */
    private <T,K>TwoTuple<K,String> getMaxMap(Map<K, T> map, Function<T,Integer> function,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,((o1, o2) -> Optional.ofNullable(o2.getValue()).map(function).orElse(0)- Optional.ofNullable(o1.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }


    /**
     * 龋齿监测结果-不同学龄段-表格数据
     */
    private void getSaprodontiaGradeMonitorTableList(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaGradeVO saprodontiaGradeVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(grade);
            getSaprodontiaGrade(list,gradeCodeEnum.getName(),tableList);
        });
        getSaprodontiaGrade(statConclusionList,"合计",tableList);

        saprodontiaGradeVO.setSaprodontiaGradeMonitorTableList(tableList);
    }


    private void getSaprodontiaGrade(List<StatConclusion> statConclusionList,String grade,List<SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(statConclusionList).ratioNotSymbol();
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaMonitorTable = buildTable(saprodontiaNum);
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
        SchoolSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO = new SchoolSaprodontiaMonitorVO.SaprodontiaAgeVO();
        getSaprodontiaAgeVariableVO(statConclusionList,saprodontiaAgeVO);
        getSaprodontiaAgeMonitorTableList(statConclusionList,saprodontiaAgeVO);

        schoolSaprodontiaMonitorVO.setSaprodontiaAgeVO(saprodontiaAgeVO);
    }

    /**
     * 龋齿监测结果-不同年龄-说明变量
     */
    private void getSaprodontiaAgeVariableVO(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, SaprodontiaNum> saprodontiaNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getSaprodontiaNum(age,list,saprodontiaNumMap));

        if (saprodontiaNumMap.size() >= 2){
            SchoolSaprodontiaMonitorVO.SaprodontiaAgeVariableVO saprodontiaAgeVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaAgeVariableVO();
            saprodontiaAgeVariableVO.setSaprodontiaRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaNum, SaprodontiaNum::getSaprodontiaRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaLossRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaLossNum, SaprodontiaNum::getSaprodontiaLossRatioStr));
            saprodontiaAgeVariableVO.setSaprodontiaRepairRatio(getAgeRatio(saprodontiaNumMap, SaprodontiaNum::getSaprodontiaRepairNum, SaprodontiaNum::getSaprodontiaRepairRatioStr));
            saprodontiaAgeVO.setSaprodontiaAgeVariableVO(saprodontiaAgeVariableVO);
        }
    }

    private SchoolSaprodontiaMonitorVO.AgeRatio getAgeRatio(Map<Integer, SaprodontiaNum> saprodontiaNumMap, Function<SaprodontiaNum,Integer> function,Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaNumMap)){
            return null;
        }
        SchoolSaprodontiaMonitorVO.AgeRatio ageRatio = new SchoolSaprodontiaMonitorVO.AgeRatio();
        TwoTuple<Integer, String> maxTuple = getMaxMap(saprodontiaNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = getMinMap(saprodontiaNumMap, function,mapper);
        ageRatio.setMaxAge(AgeSegmentEnum.get(maxTuple.getFirst()).getDesc());
        ageRatio.setMinAge(AgeSegmentEnum.get(minTuple.getFirst()).getDesc());
        ageRatio.setMaxRatio(maxTuple.getSecond());
        ageRatio.setMinRatio(minTuple.getSecond());
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
    private <T,K>TwoTuple<K,String> getMinMap(Map<K, T> map, Function<T,Integer> function,Function<T,String> mapper){
        List<Map.Entry<K, T>> entries = Lists.newArrayList(map.entrySet());
        CollectionUtil.sort(entries,Comparator.comparingInt(o -> Optional.ofNullable(o.getValue()).map(function).orElse(0)));
        Map.Entry<K, T> entry = entries.get(0);
        return TwoTuple.of(entry.getKey(),Optional.ofNullable(entry.getValue()).map(mapper).orElse(null));
    }

    /**
     * 龋齿监测结果-不同年龄-表格数据
     */
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getSaprodontiaAgeTable(age,list,tableList));
        getSaprodontiaAgeTable(1000,statConclusionList,tableList);
        saprodontiaAgeVO.setSaprodontiaAgeMonitorTableList(tableList);

    }


    private void getSaprodontiaAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }

        SaprodontiaNum saprodontiaNum = new SaprodontiaNum().build(conclusionlist).ratioNotSymbol();
        SchoolSaprodontiaMonitorVO.SaprodontiaMonitorTable saprodontiaAgeMonitorTable = buildTable(saprodontiaNum);
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
        private BigDecimal dmftRatio;

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
         * 性别
         */
        private Integer gender;

        public SaprodontiaNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }

        public SaprodontiaNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();

            Predicate<StatConclusion> predicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontia());
            ToIntFunction<StatConclusion> totalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaTeeth()).orElse(0);
            this.dmftNum = statConclusionList.stream().filter(Objects::nonNull).filter(predicateTrue).mapToInt(totalFunction).sum();

            Predicate<StatConclusion> lossAndRepairPredicateTrue = sc -> Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaLoss()) || Objects.equals(Boolean.TRUE, sc.getIsSaprodontiaRepair());
            ToIntFunction<StatConclusion> lossAndRepairTotalFunction = sc -> Optional.ofNullable(sc.getSaprodontiaLossTeeth()).orElse(0) + Optional.ofNullable(sc.getSaprodontiaRepairTeeth()).orElse(0);
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
            this.dmftRatio = Optional.ofNullable(MathUtil.numNotSymbol(dmftNum,validScreeningNum)).orElse(ReportConst.ZERO_BIG_DECIMAL);
            this.saprodontiaRatio = getRatioNotSymbol(saprodontiaNum,validScreeningNum);
            this.saprodontiaLossRatio =getRatioNotSymbol(saprodontiaLossNum,validScreeningNum);
            this.saprodontiaRepairRatio = getRatioNotSymbol(saprodontiaRepairNum,validScreeningNum);
            this.saprodontiaLossAndRepairRatio = getRatioNotSymbol(saprodontiaLossAndRepairNum,validScreeningNum);
            this.saprodontiaLossAndRepairTeethRatio =getRatioNotSymbol(saprodontiaLossAndRepairTeethNum,dmftNum);
            return this;
        }

        /**
         * 带%
         */
        public SaprodontiaNum ratio(){
            this.dmftRatio = Optional.ofNullable(MathUtil.numNotSymbol(dmftNum,validScreeningNum)).orElse(ReportConst.ZERO_BIG_DECIMAL);
            this.saprodontiaRatioStr = getRatio(saprodontiaNum,validScreeningNum);
            this.saprodontiaLossRatioStr =getRatio(saprodontiaLossNum,validScreeningNum);
            this.saprodontiaRepairRatioStr = getRatio(saprodontiaRepairNum,validScreeningNum);
            return this;
        }
    }
}
