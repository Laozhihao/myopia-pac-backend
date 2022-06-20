package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.util.MapUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 筛查数据结论
     */
    public void screeningToConclusion(Integer planId,Boolean isAll){
        if(Objects.equals(isAll,Boolean.TRUE)){
            List<VisionScreeningResult> visionScreeningResults;
            if(Objects.nonNull(planId)){
                LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.ge(VisionScreeningResult::getPlanId,planId);
                visionScreeningResults = visionScreeningResultService.list(queryWrapper);
            }else {
                visionScreeningResults = visionScreeningResultService.list();
            }
            screeningToConclusion(visionScreeningResults);
        }else {
            if (planId != null){
                screeningToConclusionByPlanIds(Lists.newArrayList(planId));
            }
        }

    }
    /**
     * 根据筛查计划Id 将筛查结果转为筛查数据结论
     * @param planIds
     */
    public void screeningToConclusionByPlanIds(List<Integer> planIds){
        if (CollectionUtil.isEmpty(planIds)){
            return;
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIds(planIds);
        screeningToConclusion(visionScreeningResults);
    }

    private void screeningToConclusion(List<VisionScreeningResult> visionScreeningResults){
        if (CollectionUtil.isEmpty(visionScreeningResults)){
            return;
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        log.info("筛查数据结论,数据处理开始");
        //筛查结果分组
        Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap = visionScreeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getPlanId));
        if (CollectionUtil.isNotEmpty(visionScreeningResultMap)){
            List<Map<Integer, List<VisionScreeningResult>>> mapList = MapUtil.splitMap(visionScreeningResultMap, 30);

            List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
            mapList.forEach(list->{
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> consumerMap(list,currentUser.getClientId()), asyncServiceExecutor);
                completableFutureList.add(future);
            });
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[mapList.size()])).join();

        }
        log.info("筛查数据结论,数据处理完成");
    }


    @Data
    static class DataProcessBO{
        private Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap;
        private Map<Integer, SchoolGrade> schoolGradeMap;
        private Map<String, StatConclusion> statConclusionMap;
        private String clientId;
    }

    private void consumerMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap,String clientId) {
        if (CollectionUtil.isEmpty(visionScreeningResultMap)){
            return;
        }
        log.info("开始处理的筛查计划ID集合：planIds:{}",CollectionUtil.join(visionScreeningResultMap.keySet(),","));
        int count = visionScreeningResultMap.values().stream().mapToInt(List::size).sum();
        log.info("筛查结果数据共{}条",count);

        Set<Integer> screeningPlanSchoolStudentIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toSet());

        DataProcessBO dataProcessBO = new DataProcessBO();
        dataProcessBO.setClientId(clientId);

        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(screeningPlanSchoolStudentIds));
        if (CollectionUtil.isNotEmpty(screeningPlanSchoolStudents)){
            Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
            Set<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());
            dataProcessBO.setScreeningPlanSchoolStudentMap(screeningPlanSchoolStudentMap);

            List<SchoolGrade> schoolGrades = schoolGradeService.getByIds(Lists.newArrayList(gradeIds));
            Map<Integer, SchoolGrade> schoolGradeMap = schoolGrades.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
            if (CollectionUtil.isNotEmpty(schoolGrades)) {
                dataProcessBO.setSchoolGradeMap(schoolGradeMap);
            }
        }

        Set<Integer> resultIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getId).collect(Collectors.toSet());
        List<StatConclusion> statConclusions = statConclusionService.getByResultIds(Lists.newArrayList(resultIds));
        if (CollectionUtil.isNotEmpty(statConclusions)){
            Map<String, StatConclusion> statConclusionMap = statConclusions.stream().collect(Collectors.toMap(sc -> getKey(sc.getResultId(), sc.getIsRescreen()), Function.identity()));
            dataProcessBO.setStatConclusionMap(statConclusionMap);
        }


        Map<Integer, Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>>> map = getMap(visionScreeningResultMap);

        List<StatConclusion> statConclusionList= Lists.newArrayList();

        map.keySet().forEach(planId->{
            Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>> typeMap = map.get(planId);
            if (CollectionUtil.isNotEmpty(typeMap)){
                typeMap.forEach((type,tuple)-> screeningConclusionResult(tuple,statConclusionList,dataProcessBO));
            }
        });

        if(CollectionUtil.isNotEmpty(statConclusionList)){
            log.info("生成筛查数据结论数据{}条",statConclusionList.size());
            statConclusionService.batchUpdateOrSave(statConclusionList);
        }
    }

    /**
     * 筛查结论结果
     */
    private void screeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple,
                                           List<StatConclusion> statConclusionList,DataProcessBO dataProcessBO){

        VisionScreeningResult currentVisionScreeningResult = tuple.getFirst();
        VisionScreeningResult secondVisionScreeningResult = tuple.getSecond();
        if (Objects.isNull(currentVisionScreeningResult)){
            return;
        }
        result(statConclusionList,dataProcessBO, currentVisionScreeningResult, null);

        if (Objects.nonNull(secondVisionScreeningResult)){
            result(statConclusionList,dataProcessBO, secondVisionScreeningResult, currentVisionScreeningResult);
        }

    }

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

        // 根据是否复查，查找结论表
        StatConclusion statConclusion = statConclusionMap.get(getKey(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen()));

        //需要新增
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult)
                .setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .setGradeCode(schoolGradeCode)
                .setClientId(dataProcessBO.getClientId())
                .build();
        statConclusionList.add(statConclusion);
    }

    /**
     *  map结构：筛查计划ID - 筛查类型 - 初筛/复筛数据
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
