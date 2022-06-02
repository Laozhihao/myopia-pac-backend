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

    private static Map<Integer,Integer> map = Maps.newConcurrentMap();

    /**
     * 龋齿监测结果
     */
    public void getSchoolSaprodontiaMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolSaprodontiaMonitorVO schoolSaprodontiaMonitorVO = new SchoolSaprodontiaMonitorVO();

        map.put(0,statConclusionList.size());
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

        if (saprodontiaSexList.size() >= 1){
            SchoolSaprodontiaMonitorVO.SaprodontiaSexVariableVO saprodontiaSexVariableVO = new SchoolSaprodontiaMonitorVO.SaprodontiaSexVariableVO();
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

    private SchoolSaprodontiaMonitorVO.SaprodontiaSex getRatioCompare(List<SaprodontiaNum> saprodontiaSexList, Function<SaprodontiaNum,BigDecimal> function, Function<SaprodontiaNum,String> mapper) {
        if (CollectionUtil.isEmpty(saprodontiaSexList)){
            return null;
        }
        CollectionUtil.sort(saprodontiaSexList, Comparator.comparing(function).reversed());
        SchoolSaprodontiaMonitorVO.SaprodontiaSex sex = new SchoolSaprodontiaMonitorVO.SaprodontiaSex();
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
                               SchoolSaprodontiaMonitorVO.SaprodontiaSex sex,
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

    private void setSymbol(SchoolSaprodontiaMonitorVO.SaprodontiaSex sex, String forwar, String back) {
        if (Objects.equals(forwar, back)){
            sex.setSymbol("=");
        }else {
            sex.setSymbol(">");
        }
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
        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge())));
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
    private void getSaprodontiaAgeMonitorTableList(List<StatConclusion> statConclusionList, SchoolSaprodontiaMonitorVO.SaprodontiaAgeVO saprodontiaAgeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> ReportUtil.getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
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
            this.saprodontiaLossAndRepairRatioStr = getRatio(saprodontiaLossAndRepairNum,getTotal());
            this.saprodontiaLossAndRepairTeethRatioStr =getRatio(saprodontiaLossAndRepairTeethNum,dmftNum);
            return this;
        }
    }

    private static Integer getTotal(){
        return map.get(0);
    }
}
