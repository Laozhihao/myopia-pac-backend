package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.api.management.domain.bo.StatisticResultBO;
import com.wupol.myopia.business.api.management.domain.builder.ScreeningResultStatisticBuilder;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按学校统计
 *
 * @author hang.yuan 2022/4/14 10:49
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Component
public class SchoolStatisticTask {

    private final StatConclusionService statConclusionService;
    private final ScreeningPlanService screeningPlanService;
    private final SchoolService schoolService;
    private final ScreeningOrganizationService screeningOrganizationService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final ScreeningResultStatisticService screeningResultStatisticService;


    /**
     * 按学校统计
     */
    public void schoolStatistics(List<Integer> yesterdayScreeningPlanIds) {

        //根据筛查计划ID 获取筛查数据结论
        List<StatConclusion> statConclusions = statConclusionService.getByScreeningPlanIds(yesterdayScreeningPlanIds);
        if(CollectionUtil.isEmpty(statConclusions)){
            log.error("未找到筛查数据结论，planIds:{}",CollectionUtil.join(yesterdayScreeningPlanIds,","));
            return;
        }

        //筛查数据结论 根据筛查类型分组 分别统计
        Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getScreeningType));

        //视力筛查
        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList = visionScreeningResultStatistic(screeningTypeStatConclusionMap);
        if (CollectionUtil.isNotEmpty(visionScreeningResultStatisticList)){
            for (VisionScreeningResultStatistic visionScreeningResultStatistic : visionScreeningResultStatisticList) {
                screeningResultStatisticService.saveVisionScreeningResultStatistic(visionScreeningResultStatistic);
            }
        }
        //常见病筛查
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = commonDiseaseScreeningResultStatistic(screeningTypeStatConclusionMap);
        if (CollectionUtil.isNotEmpty(commonDiseaseScreeningResultStatisticList)){
            for (CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic : commonDiseaseScreeningResultStatisticList) {
                screeningResultStatisticService.saveCommonDiseaseScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
            }
        }

    }

    /**
     * 视力筛查结果统计
     */
    private List<VisionScreeningResultStatistic> visionScreeningResultStatistic(Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap){
        List<VisionScreeningResultStatistic> visionScreeningResultStatisticList= Lists.newArrayList();
        List<StatConclusion> statConclusions = screeningTypeStatConclusionMap.get(0);
        if (CollectionUtil.isNotEmpty(statConclusions)){
            statistics(statConclusions,visionScreeningResultStatisticList,null);
        }
        return visionScreeningResultStatisticList;
    }

    /**
     *  常见病筛查结果统计
     */
    private List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatistic(Map<Integer, List<StatConclusion>> screeningTypeStatConclusionMap){
        List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList =Lists.newArrayList();
        List<StatConclusion> statConclusions = screeningTypeStatConclusionMap.get(1);
        if (CollectionUtil.isNotEmpty(statConclusions)){
            statistics(statConclusions,null,commonDiseaseScreeningResultStatisticList);
        }
        return commonDiseaseScreeningResultStatisticList;
    }

    /**
     * 统计逻辑
     */
    private void statistics(List<StatConclusion> statConclusions,
                            List<VisionScreeningResultStatistic> visionScreeningResultStatisticList,
                            List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList) {
        //根据筛查数据结论 按计划ID分组
        Map<Integer, List<StatConclusion>> statConclusionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getPlanId));

        Set<Integer> screeningPlanIds = statConclusionMap.keySet();

        //根据筛查计划ID 获取筛查计划数据
        List<ScreeningPlan> screeningPlans = screeningPlanService.getByIds(screeningPlanIds);
        if(CollectionUtil.isEmpty(screeningPlans)){
            return;
        }

        //根据筛查计划数据 按计划ID分组
        Map<Integer, ScreeningPlan> screeningPlanMap = screeningPlans.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));

        List<ScreeningPlanSchoolStudent> planSchoolStudents = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(screeningPlanIds));
        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = planSchoolStudents.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getScreeningPlanId));

        //分别统计每个学校的数据
        for (Integer screeningPlanId : screeningPlanIds) {
            // 排除空数据
            List<StatConclusion> statConclusionList = statConclusionMap.get(screeningPlanId);
            if (CollectionUtil.isEmpty(statConclusionList)){
                return;
            }
            ScreeningPlan screeningPlan = screeningPlanMap.get(screeningPlanId);
            if(Objects.isNull(screeningPlan)){
                return;
            }
            StatisticResultBO statisticResultBO = new StatisticResultBO()
                    .setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                    .setScreeningType(screeningPlan.getScreeningType())
                    .setScreeningOrgId(screeningPlan.getScreeningOrgId())
                    .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                    .setScreeningPlanId(screeningPlan.getId());

            //筛查数据结论 按学校ID分组
            Map<Integer, List<StatConclusion>> schoolIdStatConslusionMap = statConclusionList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));

            //获取学校信息
            List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIdStatConslusionMap.keySet()));
            if (CollectionUtil.isEmpty(schoolList)){
                return;
            }
            Map<Integer, School> schoolIdMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity()));

            //筛查计划Id 获取筛查学生
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = planSchoolStudentMap.get(screeningPlanId);


            Map<Integer, Long> planSchoolStudentNumMap= Maps.newHashMap();
            if (CollectionUtil.isNotEmpty(screeningPlanSchoolStudents)){
                Map<Integer, Long> collect = screeningPlanSchoolStudents.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
                planSchoolStudentNumMap.putAll(collect);
            }

            //3.2 每个学校分别统计
            schoolIdStatConslusionMap.forEach((schoolId,schoolStatConclusionList)->{

                int planSchoolScreeningNum = planSchoolStudentNumMap.getOrDefault(schoolId, 0L).intValue();
                School school = schoolIdMap.get(schoolId);
                statisticResultBO.setSchoolId(schoolId)
                        .setSchoolType(school.getType()).setDistrictId(school.getDistrictId())
                        .setPlanStudentCount(planSchoolScreeningNum);

                if (Objects.nonNull(visionScreeningResultStatisticList)){
                    ScreeningResultStatisticBuilder.visionScreening(statisticResultBO,schoolStatConclusionList,visionScreeningResultStatisticList);

                }
                if (Objects.nonNull(commonDiseaseScreeningResultStatisticList)){
                    ScreeningResultStatisticBuilder.commonDiseaseScreening(statisticResultBO,schoolStatConclusionList,commonDiseaseScreeningResultStatisticList);
                }
            });

        }
    }


}
