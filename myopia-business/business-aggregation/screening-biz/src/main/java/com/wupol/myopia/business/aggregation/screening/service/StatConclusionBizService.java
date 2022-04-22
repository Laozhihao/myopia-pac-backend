package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * 全部
     */
    public void screeningToConclusionAll(){
        LambdaQueryWrapper<VisionScreeningResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(VisionScreeningResult::getPlanId,127);
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.list(queryWrapper);
        screeningToConclusion(visionScreeningResults);
        log.info("success");
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
        log.info("success");
    }

    private void screeningToConclusion(List<VisionScreeningResult> visionScreeningResults){
        //1.历史初筛和复筛的数据
        if (CollectionUtil.isEmpty(visionScreeningResults)){
            return;
        }

        //2.筛查结果分组
        Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap = visionScreeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getPlanId));

        List<Map<Integer, List<VisionScreeningResult>>> mapList = MapUtil.splitMap(visionScreeningResultMap, 30);

        log.info("共{}批次",mapList.size());
        mapList.forEach(this::consumerMap);

    }

    private void consumerMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap) {
        log.info("开始处理的筛查计划ID集合：planIds:{}",CollectionUtil.join(visionScreeningResultMap.keySet(),","));
        int count = visionScreeningResultMap.values().stream().mapToInt(List::size).sum();
        log.info("筛查结果数据共{}条",count);

        Set<Integer> screeningPlanSchoolStudentIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toSet());
        Set<Integer> resultIds = visionScreeningResultMap.values().stream().flatMap(List::stream).map(VisionScreeningResult::getId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(screeningPlanSchoolStudentIds));
        List<StatConclusion> statConclusions = statConclusionService.getByResultIds(Lists.newArrayList(resultIds));

        Map<String, StatConclusion> statConclusionMap = statConclusions.stream().collect(Collectors.toMap(sc -> getKey(sc.getResultId(), sc.getIsRescreen()), Function.identity()));
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        Set<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());

        List<SchoolGrade> schoolGrades = schoolGradeService.getByIds(Lists.newArrayList(gradeIds));
        Map<Integer, SchoolGrade> schoolGradeMap = schoolGrades.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));

        Map<Integer, Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>>> map = getMap(visionScreeningResultMap);

        List<StatConclusion> statConclusionList= Lists.newArrayList();

        map.keySet().forEach(planId->{
            Map<String, TwoTuple<VisionScreeningResult, VisionScreeningResult>> typeMap = map.get(planId);
            typeMap.forEach((type,tuple)-> screeningConclusionResult(tuple,statConclusionList,screeningPlanSchoolStudentMap,schoolGradeMap,statConclusionMap));
        });

        if(CollectionUtil.isNotEmpty(statConclusionList)){
            log.info("筛查数据结论数据共{}条",statConclusionList.size());
            statConclusionService.batchUpdateOrSave(statConclusionList);
        }
        log.info("完成处理");
    }

    /**
     * 筛查结论结果
     */
    private void screeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple,
                                           List<StatConclusion> statConclusionList,
                                           Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap,
                                           Map<Integer, SchoolGrade> schoolGradeMap,
                                           Map<String, StatConclusion> statConclusionMap){
        VisionScreeningResult currentVisionScreeningResult = tuple.getFirst();
        VisionScreeningResult secondVisionScreeningResult = tuple.getSecond();
        result(statConclusionList,screeningPlanSchoolStudentMap,schoolGradeMap,statConclusionMap, currentVisionScreeningResult, secondVisionScreeningResult);
        if (secondVisionScreeningResult != null){
            currentVisionScreeningResult=secondVisionScreeningResult;
            result(statConclusionList,screeningPlanSchoolStudentMap,schoolGradeMap,statConclusionMap, currentVisionScreeningResult, secondVisionScreeningResult);
        }
    }

    private void result(List<StatConclusion> statConclusionList,
                        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap,
                        Map<Integer, SchoolGrade> schoolGradeMap,
                        Map<String, StatConclusion> statConclusionMap,
                        VisionScreeningResult currentVisionScreeningResult,
                        VisionScreeningResult secondVisionScreeningResult) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentMap.get(currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        if (screeningPlanSchoolStudent == null) {
            log.error("数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = {}" , currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
            return;
        }
        // 根据是否复查，查找结论表
        StatConclusion statConclusion = statConclusionMap.get(getKey(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen()));
        //需要新增
        SchoolGrade schoolGrade = schoolGradeMap.get(screeningPlanSchoolStudent.getGradeId());
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult)
                .setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .setGradeCode(schoolGrade.getGradeCode())
                .build();
        statConclusionList.add(statConclusion);
    }

    /**
     *  map结构：筛查计划ID - 筛查类型 - 初筛/复筛数据
     */
    private Map<Integer, Map<String,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> getMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap){
        Map<Integer, Map<String,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> map= Maps.newHashMap();
        visionScreeningResultMap.forEach((planId,results)->{
            Map<String, List<VisionScreeningResult>> typeMap = results.stream().collect(Collectors.groupingBy(vs->getKey(vs.getScreeningType(),vs.getStudentId())));
            Map<String, TwoTuple<VisionScreeningResult,VisionScreeningResult>> typeResult = Maps.newHashMap();
            typeMap.forEach((type,list)->{
                TwoTuple<VisionScreeningResult,VisionScreeningResult> result = new TwoTuple<>();
                for (VisionScreeningResult visionScreeningResult : list) {
                    if (visionScreeningResult.getIsDoubleScreen()) {
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
