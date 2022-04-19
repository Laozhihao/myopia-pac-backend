package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
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
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.list();
        screeningToConclusion(visionScreeningResults);
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
        //1.历史初筛和复筛的数据
        if (CollectionUtil.isEmpty(visionScreeningResults)){
            return;
        }

        //2.筛查结果分组
        Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap = visionScreeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getPlanId));
        Map<Integer, Map<Integer, TwoTuple<VisionScreeningResult, VisionScreeningResult>>> map = getMap(visionScreeningResultMap);

        List<StatConclusion> statConclusionList=Lists.newArrayList();

        map.keySet().forEach(planId->{
            Map<Integer, TwoTuple<VisionScreeningResult, VisionScreeningResult>> typeMap = map.get(planId);
            typeMap.forEach((type,tuple)-> screeningConclusionResult(tuple,statConclusionList));
        });

        if(CollectionUtil.isNotEmpty(statConclusionList)){
            statConclusionList.forEach(statConclusionService::saveOrUpdateStudentScreenData);
        }
    }

    /**
     * 筛查结论结果
     */
    private void screeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> tuple,List<StatConclusion> statConclusionList){
        VisionScreeningResult currentVisionScreeningResult = tuple.getFirst();
        VisionScreeningResult secondVisionScreeningResult = tuple.getSecond();
        result(statConclusionList, currentVisionScreeningResult, secondVisionScreeningResult);
        if (secondVisionScreeningResult != null){
            currentVisionScreeningResult=secondVisionScreeningResult;
            result(statConclusionList, currentVisionScreeningResult, secondVisionScreeningResult);
        }
    }

    private void result(List<StatConclusion> statConclusionList, VisionScreeningResult currentVisionScreeningResult, VisionScreeningResult secondVisionScreeningResult) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = " + currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        }
        // 根据是否复查，查找结论表
        StatConclusion statConclusion = statConclusionService.getStatConclusion(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen());
        //需要新增
        SchoolGrade schoolGrade = schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId());
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
    private Map<Integer, Map<Integer,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> getMap(Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap){
        Map<Integer, Map<Integer,TwoTuple<VisionScreeningResult,VisionScreeningResult>>> map= Maps.newHashMap();
        visionScreeningResultMap.forEach((planId,results)->{
            Map<Integer, List<VisionScreeningResult>> typeMap = results.stream().collect(Collectors.groupingBy(VisionScreeningResult::getScreeningType));
            Map<Integer, TwoTuple<VisionScreeningResult,VisionScreeningResult>> typeResult = Maps.newHashMap();
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

}
