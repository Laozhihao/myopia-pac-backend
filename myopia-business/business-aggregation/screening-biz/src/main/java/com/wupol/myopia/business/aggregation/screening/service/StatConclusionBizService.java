package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.aggregation.student.domain.builder.StudentInfoBuilder;
import com.wupol.myopia.business.common.utils.util.MyopiaMapUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.facade.VisionScreeningResultFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查结果转筛查数据结论
 *
 * @author hang.yuan 2022/4/18 19:34
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StatConclusionBizService {

    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final VisionScreeningResultService visionScreeningResultService;
    private final StatConclusionService statConclusionService;
    private final SchoolGradeService schoolGradeService;
    private final ThreadPoolTaskExecutor asyncServiceExecutor;
    private final StudentService studentService;
    private final SchoolStudentService schoolStudentService;
    private final SchoolClassService schoolClassService;

    /**
     * 筛查数据结论
     * @param planId 计划ID
     * @param isAll 是否全部 (true-全部,false-不是全部) 必填
     */
    public void screeningToConclusion(Integer planId,Boolean isAll,String exclude){
        if(Objects.equals(isAll,Boolean.TRUE)){
            List<VisionScreeningResult> list = visionScreeningResultService.list();
            if (StrUtil.isNotBlank(exclude)){
                list = list.stream().filter(visionScreeningResult -> !exclude.contains(visionScreeningResult.getPlanId().toString())).collect(Collectors.toList());
            }
            screeningToConclusion(list);
            return;
        }

        if(Objects.nonNull(planId)){
            screeningToConclusionByPlanIds(Lists.newArrayList(planId));
        }

    }
    /**
     * 根据筛查计划Id 将筛查结果转为筛查数据结论
     * @param planIds 计划ID集合
     */
    public void screeningToConclusionByPlanIds(List<Integer> planIds){
        if (CollectionUtil.isEmpty(planIds)){
            return;
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIds(planIds);
        screeningToConclusion(visionScreeningResults);
    }

    /**
     * 筛查结果数据转筛查数据结论处理
     * @param visionScreeningResults 筛查结果数据集合
     */
    private void screeningToConclusion(List<VisionScreeningResult> visionScreeningResults){
        if (CollectionUtil.isEmpty(visionScreeningResults)){
            return;
        }
        log.info("筛查数据结论,数据处理开始");
        //筛查结果分组
        Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap = visionScreeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getPlanId));
        if (CollectionUtil.isEmpty(visionScreeningResultMap)){
            return;
        }
        List<Map<Integer, List<VisionScreeningResult>>> mapList = MyopiaMapUtil.splitMap(visionScreeningResultMap, 2);

        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
        mapList.forEach(list->{
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> consumerMapCatchException(list), asyncServiceExecutor);
            completableFutureList.add(future);
        });
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[mapList.size()])).join();

        log.info("筛查数据结论,数据处理完成");
    }

    /**
     * 消费筛查结果统计数据，吃掉异常
     * @param visionScreeningResultMap 筛查结果统计数据
     */
    private void consumerMapCatchException(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap) {
        try {
            consumerMap(visionScreeningResultMap);
        } catch (Exception e) {
            log.error("更新statConclusion数据异常：{}", visionScreeningResultMap.keySet(), e);
        }
    }

        @Data
    static class DataProcessBO{
        private Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap;
        private Map<Integer, SchoolGrade> schoolGradeMap;
        private Map<Integer, SchoolClass> schoolClassMap;
        private Map<String, StatConclusion> statConclusionMap;
    }

    /**
     * 消费筛查结果统计数据
     * @param visionScreeningResultMap 筛查结果统计数据
     */
    private void consumerMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap) {
        if (CollectionUtil.isEmpty(visionScreeningResultMap)){
            return;
        }
        log.info("开始处理的筛查计划ID集合：planIds:{}",CollectionUtil.join(visionScreeningResultMap.keySet(),","));
        int count = visionScreeningResultMap.values().stream().mapToInt(List::size).sum();
        log.info("筛查结果数据共{}条",count);

        Set<Integer> screeningPlanSchoolStudentIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toSet());

        DataProcessBO dataProcessBO = new DataProcessBO();

        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(screeningPlanSchoolStudentIds));
        if (CollUtil.isNotEmpty(screeningPlanSchoolStudents)){
            Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
            Set<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());
            dataProcessBO.setScreeningPlanSchoolStudentMap(screeningPlanSchoolStudentMap);

            List<SchoolGrade> schoolGrades = schoolGradeService.getByIds(Lists.newArrayList(gradeIds));
            Map<Integer, SchoolGrade> schoolGradeMap = schoolGrades.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
            if (CollUtil.isNotEmpty(schoolGrades)) {
                dataProcessBO.setSchoolGradeMap(schoolGradeMap);
            }
            List<Integer> classIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList());
            Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIds);
            if (CollUtil.isNotEmpty(classMap)) {
                dataProcessBO.setSchoolClassMap(classMap);
            }
        }

        Set<Integer> resultIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getId).collect(Collectors.toSet());
        List<StatConclusion> statConclusions = statConclusionService.getByResultIds(Lists.newArrayList(resultIds));
        if (CollUtil.isNotEmpty(statConclusions)){
            Map<String, StatConclusion> statConclusionMap = statConclusions.stream().collect(Collectors.toMap(sc -> getKey(sc.getResultId(), sc.getIsRescreen()), Function.identity()));
            dataProcessBO.setStatConclusionMap(statConclusionMap);
        }

        Map<Integer, Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>>> map = getMap(visionScreeningResultMap);

        List<StatConclusion> statConclusionList= Lists.newArrayList();

        map.keySet().forEach(planId->{
            Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>> typeMap = map.get(planId);
            if (CollUtil.isNotEmpty(typeMap)){
                typeMap.forEach((type,tuple)-> screeningConclusionResult(tuple,statConclusionList,dataProcessBO));
            }
        });

        if(CollUtil.isNotEmpty(statConclusionList)){
            log.info("生成筛查数据结论数据{}条",statConclusionList.size());
            List<StatConclusion> saveList = statConclusionList.stream().filter(statConclusion -> Objects.isNull(statConclusion.getId())).collect(Collectors.toList());
            List<StatConclusion> updateList = statConclusionList.stream().filter(statConclusion -> Objects.nonNull(statConclusion.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(saveList)){
                statConclusionService.saveBatch(saveList,500);
            }
            if (CollUtil.isNotEmpty(updateList)){
                statConclusionService.updateBatchById(updateList,500);
            }
            updateRelatedTable(statConclusionList);
        }
    }

    /**
     * 筛查结论数据更新，同步更新相关表数据
     * @param statConclusionList 筛查结论数据集合
     */
    private void updateRelatedTable(List<StatConclusion> statConclusionList){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }

        Set<Integer> studentIds = statConclusionList.stream().map(StatConclusion::getStudentId).collect(Collectors.toSet());
        Map<Integer, Student> studentMap = getStudentMap(studentIds);
        Map<String, SchoolStudent> schoolStudentMap = getSchoolStudentMap(studentIds);
        Map<Integer, VisionScreeningResult> visionScreeningResultMap = getVisionScreeningResultMap(statConclusionList);
        // 更新是否绑定手机号码
        setIsBindMq(statConclusionList,studentMap);
        //更新学生表的数据（复测覆盖了初筛的结论）
        updateStudentVisionData(statConclusionList,studentMap,visionScreeningResultMap);
        //更新学校学生
        updateSchoolStudent(statConclusionList,schoolStudentMap,visionScreeningResultMap);
    }

    /**
     * 获取学生信息
     * @param studentIds 学生ID集合
     */
    private Map<Integer,Student> getStudentMap(Set<Integer> studentIds){
        Map<Integer,Student> studentMap=Maps.newHashMap();
        List<Student> studentList = studentService.listByIds(studentIds);
        if (CollectionUtil.isNotEmpty(studentList)){
            studentMap = studentList.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
        }
        return studentMap;
    }

    /**
     * 获取学校学生信息
     * @param studentIds 学生ID集合
     */
    private Map<String,SchoolStudent> getSchoolStudentMap(Set<Integer> studentIds){
        Map<String,SchoolStudent> schoolStudentMap=Maps.newHashMap();
        List<SchoolStudent> schoolStudentList = schoolStudentService.getByStudentIds(Lists.newArrayList(studentIds));
        if (CollectionUtil.isNotEmpty(schoolStudentList)){
            schoolStudentMap = schoolStudentList.stream().collect(Collectors.toMap(schoolStudent -> VisionScreeningResultFacade.getTwoKey(schoolStudent.getStudentId(),schoolStudent.getSchoolId()),Function.identity()));
        }
        return schoolStudentMap;
    }


    /**
     * 获取筛查结果数据
     * @param statConclusionList 筛查结论数据集合
     */
    private Map<Integer,VisionScreeningResult> getVisionScreeningResultMap(List<StatConclusion> statConclusionList){
        Map<Integer,VisionScreeningResult> visionScreeningResultMap =Maps.newHashMap();
        Set<Integer> resultIds = statConclusionList.stream().map(StatConclusion::getResultId).collect(Collectors.toSet());
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.listByIds(resultIds);
        if (CollectionUtil.isNotEmpty(visionScreeningResultList)){
            visionScreeningResultMap = visionScreeningResultList.stream().collect(Collectors.toMap(VisionScreeningResult::getId, Function.identity()));
        }
        return visionScreeningResultMap;
    }


    /**
     * 是否绑定公众号
     * @param statConclusionList 筛查结论数据集合
     */
    private void setIsBindMq(List<StatConclusion> statConclusionList,Map<Integer, Student> studentMap) {
        statConclusionList.forEach(statConclusion -> {
            Student student = studentMap.get(statConclusion.getStudentId());
            statConclusion.setIsBindMp(Objects.isNull(student) ? Boolean.FALSE : StringUtils.isNotBlank(student.getMpParentPhone()));
        });
    }

    /**
     * 更新学生数据
     * @param statConclusionList 筛查结论数据集合
     */
    private void updateStudentVisionData(List<StatConclusion> statConclusionList,Map<Integer, Student> studentMap,Map<Integer, VisionScreeningResult> visionScreeningResultMap) {
        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        List<Student> studentList = Lists.newArrayList();
        statConclusionList.forEach(statConclusion -> setStudentInfo(studentMap,visionScreeningResultMap,studentList,statConclusion));
        if (CollectionUtil.isNotEmpty(studentList)){
            studentService.updateBatchById(studentList,500);
        }
    }

    /**
     * 设置学生信息
     * @param studentMap 学生信息集合
     * @param visionScreeningResultMap 筛查结果集合
     * @param studentList 学生结果结果集合
     * @param statConclusion 筛查结论数据
     */
    private void setStudentInfo(Map<Integer, Student> studentMap,Map<Integer, VisionScreeningResult> visionScreeningResultMap,List<Student> studentList,StatConclusion statConclusion){
        Optional.ofNullable(studentMap.get(statConclusion.getStudentId()))
                .ifPresent(student -> {
                    //填充数据
                    Date lastScreeningTime = Optional.ofNullable(visionScreeningResultMap.get(statConclusion.getResultId())).map(VisionScreeningResult::getUpdateTime).orElse(null);
                    StudentInfoBuilder.setStudentInfoByStatConclusion(student,statConclusion,lastScreeningTime);
                    studentList.add(student);
                });
    }

    /**
     * 更新学校学生
     * @param statConclusionList 筛查结论数据集合
     * @param schoolStudentMap 学校学生数据集合
     * @param visionScreeningResultMap 筛查结果数据集合
     */
    private void updateSchoolStudent(List<StatConclusion> statConclusionList, Map<String, SchoolStudent> schoolStudentMap,Map<Integer, VisionScreeningResult> visionScreeningResultMap) {
        List<SchoolStudent> schoolStudentList =Lists.newArrayList();
        statConclusionList.forEach(statConclusion -> {
            SchoolStudent schoolStudent = schoolStudentMap.get(VisionScreeningResultFacade.getTwoKey(statConclusion.getStudentId(),statConclusion.getSchoolId()));
            VisionScreeningResult visionScreeningResult = visionScreeningResultMap.get(statConclusion.getResultId());
            if (Objects.nonNull(schoolStudent)){
                SchoolStudentInfoBuilder.setSchoolStudentInfoByStatConclusion(schoolStudent,statConclusion,visionScreeningResult.getUpdateTime());
                schoolStudentList.add(schoolStudent);
            }
        });

        if (CollUtil.isNotEmpty(schoolStudentList)){
            schoolStudentService.updateBatchById(schoolStudentList,500);
        }
    }

    /**
     * 筛查结论数据
     * @param tuple 筛查结果数据（初筛数据和复测数据）
     * @param statConclusionList 筛查结论数据集合
     * @param dataProcessBO 数据流转对象
     */
    private void screeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple,
                                           List<StatConclusion> statConclusionList,DataProcessBO dataProcessBO){

        VisionScreeningResult currentVisionScreeningResult = tuple.getFirst();
        VisionScreeningResult secondVisionScreeningResult = tuple.getSecond();
        if (Objects.isNull(currentVisionScreeningResult)){
            return;
        }
        //此时处理初筛数据结果，暂时没有复测数据
        result(statConclusionList,dataProcessBO, currentVisionScreeningResult, null);

        if (Objects.nonNull(secondVisionScreeningResult)){
            //此时是处理复测数据，对比数据是初筛数据（currentVisionScreeningResult），当前数据是复测数据（secondVisionScreeningResult）
            result(statConclusionList,dataProcessBO, secondVisionScreeningResult, currentVisionScreeningResult);
        }

    }

    /**
     * 处理数据结果
     * @param statConclusionList 筛查数据结论集合
     * @param dataProcessBO 数据流转对象
     * @param currentVisionScreeningResult 当前筛查数据结果
     * @param secondVisionScreeningResult 初筛筛查数据结果
     */
    private void result(List<StatConclusion> statConclusionList,DataProcessBO dataProcessBO,
                        VisionScreeningResult currentVisionScreeningResult,
                        VisionScreeningResult secondVisionScreeningResult) {
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = dataProcessBO.getScreeningPlanSchoolStudentMap();
        if (CollectionUtil.isEmpty(screeningPlanSchoolStudentMap)){
            return;
        }
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentMap.get(currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            log.error("数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = {}" , currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
            return;
        }
        Map<String, StatConclusion> statConclusionMap = Optional.ofNullable(dataProcessBO.getStatConclusionMap()).orElse(Maps.newHashMap());

        Map<Integer, SchoolGrade> schoolGradeMap = Optional.ofNullable(dataProcessBO.getSchoolGradeMap()).orElse(Maps.newHashMap());
        SchoolGrade schoolGrade = schoolGradeMap.get(screeningPlanSchoolStudent.getGradeId());
        String schoolGradeCode = Optional.ofNullable(schoolGrade).map(SchoolGrade::getGradeCode).orElse(null);

        Map<Integer, SchoolClass> schoolClassMap = Optional.ofNullable(dataProcessBO.getSchoolClassMap()).orElse(Maps.newHashMap());
        SchoolClass schoolClass = schoolClassMap.get(screeningPlanSchoolStudent.getClassId());

        // 根据是否复查，查找结论表
        StatConclusion statConclusion = statConclusionMap.get(getKey(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen()));

        //需要新增
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult)
                .setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .setGradeCode(schoolGradeCode)
                .setSchoolClass(schoolClass)
                .build();
        statConclusionList.add(statConclusion);
    }

    /**
     * map结构：筛查计划ID - 筛查类型 - 初筛/复筛数据
     * @param visionScreeningResultMap 筛查数据
     */
    private Map<Integer, Map<String,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> getMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap){
        Map<Integer, Map<String,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> map= Maps.newHashMap();
        if (CollectionUtil.isEmpty(visionScreeningResultMap)){
            return map;
        }
        visionScreeningResultMap.forEach((planId,results)->{
            Map<String, List<VisionScreeningResult>> typeMap = results.stream().collect(Collectors.groupingBy(vs->getKey(vs.getScreeningType(),vs.getStudentId())));
            Map<String, TwoTuple<VisionScreeningResult,VisionScreeningResult>> typeResult = Maps.newHashMap();
            typeMap.forEach((type,list)->{
                TwoTuple<VisionScreeningResult,VisionScreeningResult> result = new TwoTuple<>();
                for (VisionScreeningResult visionScreeningResult : list) {
                    if (Objects.equals(visionScreeningResult.getIsDoubleScreen(),Boolean.TRUE)) {
                        result.setSecond(visionScreeningResult);
                    }else {
                        result.setFirst(visionScreeningResult);
                    }
                }
                typeResult.put(type,result);
            });
            map.put(planId,typeResult);
        });
        return map;
    }

    private String getKey(Integer screeningType ,Integer studentId){
        return screeningType+"_"+studentId;
    }
    private String getKey(Integer id,Boolean isDoubleScreen){
        return id+"_"+isDoubleScreen.toString();
    }

}
