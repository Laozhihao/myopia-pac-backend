package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.constant.AgeSegmentEnum;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolHeightAndWeightMonitorVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 身高体重监测
 *
 * @author hang.yuan 2022/5/25 15:05
 */
@Service
public class SchoolHeightAndWeightMonitorService {

    /**
     * 体重身高监测结果
     */
    public void getSchoolHeightAndWeightMonitorVO(List<StatConclusion> statConclusionList, SchoolCommonDiseasesAnalysisVO schoolCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO = new SchoolHeightAndWeightMonitorVO();

        //说明变量
        getHeightAndWeightMonitorVariableVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同性别
        getHeightAndWeightSexVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同年级
        getHeightAndWeightGradeVO(statConclusionList,schoolHeightAndWeightMonitorVO);
        //不同年龄段
        getHeightAndWeightAgeVO(statConclusionList,schoolHeightAndWeightMonitorVO);

        schoolCommonDiseasesAnalysisVO.setSchoolHeightAndWeightMonitorVO(schoolHeightAndWeightMonitorVO);
    }

    /**
     * 体重身高监测结果-说明变量
     */
    private void getHeightAndWeightMonitorVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightNum heightAndWeight = new HeightAndWeightNum().build(statConclusionList).ratio();

        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorVariableVO();
        heightAndWeightMonitorVariableVO.setOverweightRatio(heightAndWeight.overweightRatio);
        heightAndWeightMonitorVariableVO.setObeseRatio(heightAndWeight.obeseRatio);
        heightAndWeightMonitorVariableVO.setStuntingRatio(heightAndWeight.stuntingRatio);
        heightAndWeightMonitorVariableVO.setMalnourishedRatio(heightAndWeight.malnourishedRatio);

        schoolHeightAndWeightMonitorVO.setHeightAndWeightMonitorVariableVO(heightAndWeightMonitorVariableVO);
    }

    /**
     * 体重身高监测结果-不同性别
     */
    private void getHeightAndWeightSexVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO schoolHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO();
        getHeightAndWeightSexVariableVO(statConclusionList,heightAndWeightSexVO);
        getHeightAndWeightSexMonitorTableList(statConclusionList,heightAndWeightSexVO);
        schoolHeightAndWeightMonitorVO.setHeightAndWeightSexVO(heightAndWeightSexVO);
    }

    /**
     * 体重身高监测结果-不同性别-说明变量
     */
    private void getHeightAndWeightSexVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO heightAndWeightSexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<Integer, List<StatConclusion>> genderMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<HeightAndWeightNum> heightAndWeightSexList= Lists.newArrayList();
        genderMap.forEach((gender,list)-> getHeightAndWeightNum(gender,list,heightAndWeightSexList));

        if (heightAndWeightSexList.size() >= 2){
            SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO heightAndWeightSexVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVariableVO();
            heightAndWeightSexVariableVO.setOverweightRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            heightAndWeightSexVariableVO.setObeseRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            heightAndWeightSexVariableVO.setStuntingRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            heightAndWeightSexVariableVO.setMalnourishedRatioCompare(getRatioCompare(heightAndWeightSexList, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            heightAndWeightSexVO.setHeightAndWeightSexVariableVO(heightAndWeightSexVariableVO);
        }

    }

    private void getHeightAndWeightNum(Integer gender, List<StatConclusion> statConclusionList,List<HeightAndWeightNum> heightAndWeightSexList){
        HeightAndWeightNum build = new HeightAndWeightNum()
                .setGender(gender)
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightSexList.add(build);
    }
    private <K>void getHeightAndWeightNum(K key, List<StatConclusion> statConclusionList,Map<K, HeightAndWeightNum> heightAndWeightNumMap){
        HeightAndWeightNum build = new HeightAndWeightNum()
                .build(statConclusionList).ratioNotSymbol().ratio();
        heightAndWeightNumMap.put(key,build);
    }


    private SchoolHeightAndWeightMonitorVO.HeightAndWeightSex getRatioCompare(List<HeightAndWeightNum> heightAndWeightNumList, Function<HeightAndWeightNum,Integer> function,Function<HeightAndWeightNum,String> mapper) {
        if (CollectionUtil.isEmpty(heightAndWeightNumList)){
            return null;
        }
        CollectionUtil.sort(heightAndWeightNumList, Comparator.comparing(function));
        SchoolHeightAndWeightMonitorVO.HeightAndWeightSex sex = new SchoolHeightAndWeightMonitorVO.HeightAndWeightSex();
        for (int i = 0; i < heightAndWeightNumList.size(); i++) {
            HeightAndWeightNum num = heightAndWeightNumList.get(i);
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
     * 体重身高监测结果-不同性别-表格数据
     */
    private void getHeightAndWeightSexMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightSexVO sexVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList =Lists.newArrayList();
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable maleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.MALE.type);
        if (Objects.nonNull(maleTable)){
            tableList.add(maleTable);
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable femaleTable = getHeightAndWeightSexTable(statConclusionList,GenderEnum.FEMALE.type);
        if (Objects.nonNull(femaleTable)){
            tableList.add(femaleTable);
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable totalTable = getHeightAndWeightSexTable(statConclusionList,10);
        if (Objects.nonNull(totalTable)){
            tableList.add(totalTable);
        }
        sexVO.setHeightAndWeightSexMonitorTableList(tableList);

    }
    private SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable getHeightAndWeightSexTable(List<StatConclusion> statConclusionList, Integer gender) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return null;
        }
        List<StatConclusion> conclusionlist;

        if (Objects.equals(10,gender)){
            conclusionlist = statConclusionList;
        }else {
            conclusionlist = statConclusionList.stream().filter(sc -> Objects.equals(sc.getGender(), gender)).collect(Collectors.toList());
        }

        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol();

        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        if (Objects.equals(10,gender)){
            heightAndWeightMonitorTable.setItemName("合计");
        }else {
            heightAndWeightMonitorTable.setItemName(GenderEnum.getName(gender));
        }
        return heightAndWeightMonitorTable;
    }

    public SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable buildTable(HeightAndWeightNum heightAndWeightNum){
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= new SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable();
        heightAndWeightMonitorTable.setValidScreeningNum(heightAndWeightNum.validScreeningNum);
        heightAndWeightMonitorTable.setOverweightNum(heightAndWeightNum.overweightNum);
        heightAndWeightMonitorTable.setOverweightRatio(heightAndWeightNum.overweightRatio);
        heightAndWeightMonitorTable.setObeseNum(heightAndWeightNum.obeseNum);
        heightAndWeightMonitorTable.setObeseRatio(heightAndWeightNum.obeseRatio);
        heightAndWeightMonitorTable.setStuntingNum(heightAndWeightNum.stuntingNum);
        heightAndWeightMonitorTable.setStuntingRatio(heightAndWeightNum.stuntingRatio);
        heightAndWeightMonitorTable.setMalnourishedNum(heightAndWeightNum.malnourishedNum);
        heightAndWeightMonitorTable.setMalnourishedRatio(heightAndWeightNum.malnourishedRatio);
        return heightAndWeightMonitorTable;
    }

    /**
     * 体重身高监测结果-不同学龄段
     */
    private void getHeightAndWeightGradeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO();
        getHeightAndWeightGradeVariableVO(statConclusionList,heightAndWeightGradeVO);
        getHeightAndWeightGradeMonitorTableList(statConclusionList,heightAndWeightGradeVO);

        districtHeightAndWeightMonitorVO.setHeightAndWeightGradeVO(heightAndWeightGradeVO);

    }

    /**
     * 体重身高监测结果-不同学龄段-说明变量
     */
    private void getHeightAndWeightGradeVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVariableVO schoolAgeVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVariableVO();

        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        Map<String, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        gradeCodeMap.forEach((gradeCode,list)->{
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(gradeCode);
            getHeightAndWeightNum(gradeCodeEnum.getName(),list,heightAndWeightNumMap);
        });

        if (heightAndWeightNumMap.size() >= 2){
            schoolAgeVariableVO.setOverweightRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            schoolAgeVariableVO.setObeseRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            schoolAgeVariableVO.setStuntingRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            schoolAgeVariableVO.setMalnourishedRatio(getGradeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            heightAndWeightGradeVO.setHeightAndWeightGradeVariableVO(schoolAgeVariableVO);
        }
    }


    private SchoolHeightAndWeightMonitorVO.GradeRatio getGradeRatio(Map<String, HeightAndWeightNum> heightAndWeightNumMap,Function<HeightAndWeightNum,Integer> function ,Function<HeightAndWeightNum,String> mapper){
        if (CollectionUtil.isEmpty(heightAndWeightNumMap)){
            return null;
        }
        SchoolHeightAndWeightMonitorVO.GradeRatio gradeRatio = new SchoolHeightAndWeightMonitorVO.GradeRatio();
        TwoTuple<String, String> maxTuple = getMaxMap(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr);
        TwoTuple<String, String> minTuple = getMinMap(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr);
        gradeRatio.setMaxGrade(maxTuple.getFirst());
        gradeRatio.setMaxRatio(maxTuple.getSecond());
        gradeRatio.setMinRatio(minTuple.getFirst());
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
     * 体重身高监测结果-不同学龄段-表格数据
     */
    private void getHeightAndWeightGradeMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightGradeVO heightAndWeightGradeVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        Map<String, List<StatConclusion>> gradeCodeMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        MapUtil.sort(gradeCodeMap);
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        gradeCodeMap.forEach((grade,list)-> getHeightAndWeightGrade(list,grade,tableList));
        getHeightAndWeightGrade(statConclusionList,"合计",tableList);

        heightAndWeightGradeVO.setHeightAndWeightGradeMonitorTableList(tableList);
    }


    private void getHeightAndWeightGrade(List<StatConclusion> statConclusionList,String grade,List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> gradeList) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(statConclusionList).ratioNotSymbol();
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable= buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(grade);
        gradeList.add(heightAndWeightMonitorTable);
    }

    /**
     * 体重身高监测结果-不同年龄段
     */
    private void getHeightAndWeightAgeVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO districtHeightAndWeightMonitorVO) {
        if(CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO();
        getHeightAndWeightAgeVariableVO(statConclusionList,ageVO);
        getHeightAndWeightAgeMonitorTableList(statConclusionList,ageVO);

        districtHeightAndWeightMonitorVO.setHeightAndWeightAgeVO(ageVO);
    }

    /**
     * 体重身高监测结果-不同年龄段-说明变量
     */
    private void getHeightAndWeightAgeVariableVO(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge())));
        Map<Integer, HeightAndWeightNum> heightAndWeightNumMap = Maps.newHashMap();
        ageMap.forEach((age,list)->getHeightAndWeightNum(age,list,heightAndWeightNumMap));

        if (heightAndWeightNumMap.size() >= 2){
            SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO ageVariableVO = new SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVariableVO();
            ageVariableVO.setOverweightRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getOverweightNum, HeightAndWeightNum::getOverweightRatioStr));
            ageVariableVO.setObeseRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getObeseNum, HeightAndWeightNum::getObeseRatioStr));
            ageVariableVO.setStuntingRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getStuntingNum, HeightAndWeightNum::getStuntingRatioStr));
            ageVariableVO.setMalnourishedRatio(getAgeRatio(heightAndWeightNumMap, HeightAndWeightNum::getMalnourishedNum, HeightAndWeightNum::getMalnourishedRatioStr));
            ageVO.setHeightAndWeightAgeVariableVO(ageVariableVO);
        }

    }

    private SchoolHeightAndWeightMonitorVO.AgeRatio getAgeRatio(Map<Integer, HeightAndWeightNum> heightAndWeightNumMap, Function<HeightAndWeightNum,Integer> function,Function<HeightAndWeightNum,String> mapper) {
        TwoTuple<Integer, String> maxTuple = getMaxMap(heightAndWeightNumMap, function,mapper);
        TwoTuple<Integer, String> minTuple = getMinMap(heightAndWeightNumMap, function,mapper);
        SchoolHeightAndWeightMonitorVO.AgeRatio ageRatio = new SchoolHeightAndWeightMonitorVO.AgeRatio();
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
     * 体重身高监测结果-不同年龄段-表格数据
     */
    private void getHeightAndWeightAgeMonitorTableList(List<StatConclusion> statConclusionList, SchoolHeightAndWeightMonitorVO.HeightAndWeightAgeVO ageVO) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Map<Integer, List<StatConclusion>> ageMap = statConclusionList.stream().collect(Collectors.groupingBy(sc -> getLessAge(sc.getAge()),TreeMap::new,Collectors.toList()));
        List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList = Lists.newArrayList();
        ageMap.forEach((age,list)->getHeightAndWeightAgeTable(age,list,tableList));
        getHeightAndWeightAgeTable(1000,statConclusionList,tableList);
        ageVO.setHeightAndWeightAgeMonitorTableList(tableList);
    }

    private void getHeightAndWeightAgeTable(Integer age, List<StatConclusion> conclusionlist, List<SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable> tableList) {
        if (CollectionUtil.isEmpty(conclusionlist)){
            return;
        }

        String itemName;
        if (age == 1000){
            itemName = "合计";
        }else {
            itemName = AgeSegmentEnum.get(age).getDesc();
        }
        HeightAndWeightNum heightAndWeightNum = new HeightAndWeightNum().build(conclusionlist).ratioNotSymbol();
        SchoolHeightAndWeightMonitorVO.HeightAndWeightMonitorTable heightAndWeightMonitorTable = buildTable(heightAndWeightNum);
        heightAndWeightMonitorTable.setItemName(itemName);
        tableList.add(heightAndWeightMonitorTable);
    }


    @Data
    private static class HeightAndWeightNum{

        /**
         * 筛查人数
         */
        private Integer validScreeningNum;
        /**
         * 超重数
         */
        private Integer overweightNum;
        /**
         * 肥胖数
         */
        private Integer obeseNum;
        /**
         * 营养不良数
         */
        private Integer malnourishedNum;
        /**
         * 生长迟缓数据
         */
        private Integer stuntingNum;


        //============= 不带% ============
        /**
         * 超重率
         */
        private BigDecimal overweightRatio;
        /**
         * 肥胖率
         */
        private BigDecimal obeseRatio;

        /**
         * 营养不良率
         */
        private BigDecimal malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private BigDecimal stuntingRatio;


        //============= 带% ============
        /**
         * 超重率
         */
        private String overweightRatioStr;
        /**
         * 肥胖率
         */
        private String obeseRatioStr;

        /**
         * 营养不良率
         */
        private String malnourishedRatioStr;

        /**
         * 生长迟缓率
         */
        private String stuntingRatioStr;

        /**
         * 性别
         */
        private Integer gender;


        public HeightAndWeightNum build(List<StatConclusion> statConclusionList){
            this.validScreeningNum = statConclusionList.size();

            this.overweightNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsOverweight)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.obeseNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsObesity)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.malnourishedNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsMalnutrition)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();
            this.stuntingNum = (int)statConclusionList.stream()
                    .map(StatConclusion::getIsStunting)
                    .filter(Objects::nonNull).filter(Boolean::booleanValue).count();

            return this;
        }

        /**
         * 不带%
         */
        public HeightAndWeightNum ratioNotSymbol(){
            if (Objects.nonNull(overweightNum)){
                this.overweightRatio = MathUtil.ratioNotSymbol(overweightNum,validScreeningNum);
            }
            if (Objects.nonNull(obeseNum)){
                this.obeseRatio = MathUtil.ratioNotSymbol(obeseNum,validScreeningNum);
            }
            if (Objects.nonNull(stuntingNum)){
                this.stuntingRatio = MathUtil.ratioNotSymbol(stuntingNum,validScreeningNum);
            }
            if (Objects.nonNull(malnourishedNum)){
                this.malnourishedRatio = MathUtil.ratioNotSymbol(malnourishedNum,validScreeningNum);
            }

            return this;
        }

        /**
         * 带%
         */
        public HeightAndWeightNum ratio(){
            if (Objects.nonNull(overweightNum)){
                this.overweightRatioStr = MathUtil.ratio(overweightNum,validScreeningNum);
            }
            if (Objects.nonNull(obeseNum)){
                this.obeseRatioStr = MathUtil.ratio(obeseNum,validScreeningNum);
            }
            if (Objects.nonNull(stuntingNum)){
                this.stuntingRatioStr = MathUtil.ratio(stuntingNum,validScreeningNum);
            }
            if (Objects.nonNull(malnourishedNum)){
                this.malnourishedRatioStr = MathUtil.ratio(malnourishedNum,validScreeningNum);
            }
            return this;
        }

        public HeightAndWeightNum setGender(Integer gender) {
            this.gender = gender;
            return this;
        }
    }
}
