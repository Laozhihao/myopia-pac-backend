package com.wupol.myopia.business.management.schedule;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @date 2021/02/19
 */
@Component
@Slf4j
public class ScheduledTasksExecutor {
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;
    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;
    @Autowired
    private SchoolVisionStatisticService schoolVisionStatisticService;
    @Autowired
    private SchoolMonitorStatisticService schoolMonitorStatisticService;

    /**
     * 筛查数据统计
     */
    //@Scheduled(cron = "0 5 0 * * ?", zone = "GMT+8:00")
    @Scheduled(cron = "0 * * * * ?", zone = "GMT+8:00")
    public void statistic() {
        //1. 查询出需要统计的通知（根据筛查数据vision_screening_result的更新时间判断）
        List<Integer> yesterdayScreeningPlanIds = visionScreeningResultService.getYesterdayScreeningPlanIds();
        if (CollectionUtils.isEmpty(yesterdayScreeningPlanIds)) {
            log.info("筛查数据统计：前一天无筛查数据，无需统计");
            return;
        }
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
        List<DistrictMonitorStatistic> districtMonitorStatistics = new ArrayList<>();
        List<DistrictVisionStatistic> districtVisionStatistics = new ArrayList<>();
        List<SchoolVisionStatistic> schoolVisionStatistics = new ArrayList<>();
        List<SchoolMonitorStatistic> schoolMonitorStatistics = new ArrayList<>();
        genDistrictStatistics(yesterdayScreeningPlanIds, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics);
        genSchoolStatistics(yesterdayScreeningPlanIds, schoolVisionStatistics, schoolMonitorStatistics);
//        districtAttentiveObjectsStatisticService.saveBatch(districtAttentiveObjectsStatistics);
//        districtMonitorStatisticService.saveBatch(districtMonitorStatistics);
//        districtVisionStatisticService.saveBatch(districtVisionStatistics);
//        schoolVisionStatisticService.saveBatch(schoolVisionStatistics);
//        schoolMonitorStatisticService.saveBatch(schoolMonitorStatistics);
    }

    /**
     * 按学校生成统计数据
     * @param yesterdayScreeningPlanIds
     * @param schoolVisionStatistics
     */
    private void genSchoolStatistics(List<Integer> yesterdayScreeningPlanIds, List<SchoolVisionStatistic> schoolVisionStatistics, List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        //3. 分别处理每个学校的统计
        yesterdayScreeningPlanIds.forEach(screeningPlanId -> {
            //3.1 查出计划对应的筛查数据(结果)
            List<StatConclusionVo> statConclusions = statConclusionService.getVoByScreeningPlanId(screeningPlanId);
            if (CollectionUtils.isEmpty(statConclusions)) {
                return;
            }
            Map<Integer, List<StatConclusionVo>> schoolIdStatConslusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusionVo::getSchoolId));
            ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
            Map<Integer, School> schoolIdMap = schoolService.getByIds(new ArrayList<>(schoolIdStatConslusions.keySet())).stream().collect(Collectors.toMap(School::getId, Function.identity()));
            ScreeningOrganization screeningOrg = screeningOrganizationService.getById(screeningPlan.getScreeningOrgId());
            //3.2 每个学校分别统计
            schoolIdStatConslusions.keySet().forEach(schoolId -> {
                List<StatConclusionVo> schoolStatConclusion = schoolIdStatConslusions.get(schoolId);
                Map<Boolean, List<StatConclusionVo>> isValidMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
                Map<Boolean, List<StatConclusionVo>> isRescreenTotalMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                List<StatConclusionVo> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
                Map<Boolean, List<StatConclusionVo>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                schoolVisionStatistics.add(SchoolVisionStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlanId, screeningPlan.getDistrictId(), isRescreenMap.getOrDefault(false, Collections.emptyList()), screeningPlan.getStudentNumbers(), isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
                schoolMonitorStatistics.add(SchoolMonitorStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlan.getDistrictId(), isRescreenMap.getOrDefault(true, Collections.emptyList()), screeningPlan.getStudentNumbers(), isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
            });
        });
    }

    /**
     * 按区域层级生成统计数据
     * @param yesterdayScreeningPlanIds
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     */
    private void genDistrictStatistics(List<Integer> yesterdayScreeningPlanIds, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics) {
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsByIds(yesterdayScreeningPlanIds);
        //2. 分别处理每个通知的区域层级统计
        screeningNoticeIds.forEach(screeningNoticeId -> {
            if (CommonConst.DEFAULT_ID.equals(screeningNoticeId)) {
                // 单点筛查机构创建的数据不需要统计
                return;
            }
            //2.1 查出对应的筛查数据(结果)
            List<StatConclusion> statConclusions = statConclusionService.getBySrcScreeningNoticeId(screeningNoticeId);
            if (CollectionUtils.isEmpty(statConclusions)) {
                return;
            }
            List<ScreeningPlan> screeningPlans = screeningPlanService.getBySrcScreeningNoticeId(screeningNoticeId);
            //2.2 层级维度统计
            Map<Integer, List<StatConclusion>> districtStatConclusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));
            Map<Integer, List<ScreeningPlan>> districtScreeningPlans = screeningPlans.stream().collect(Collectors.groupingBy(ScreeningPlan::getDistrictId));
            //2.3 查出通知对应的顶级层级
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            genStatisticsByDistrictId(screeningNoticeId, screeningNotice.getDistrictId(), districtScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions);
        });
    }

    /**
     * 生成层级的统计数据
     * @param screeningNoticeId
     * @param districtId
     * @param districtScreeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param districtStatConclusions 所有的筛查数据
     */
    private void genStatisticsByDistrictId(Integer screeningNoticeId, Integer districtId, Map<Integer, List<ScreeningPlan>> districtScreeningPlans, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics, Map<Integer, List<StatConclusion>> districtStatConclusions) {
        List<District> childDistrictList = new ArrayList<>();
        try {
            childDistrictList = districtService.getSpecificDistrictTree(districtId);
        } catch (IOException e) {
            log.error("获取区域层级失败", e);
        }
        // 合计的要包括自己层级的筛查数据
        List<Integer> childDistrictIds = new ArrayList<>();
        getAllIdsWithChild(childDistrictIds, childDistrictList);
        //2.4 层级循环处理并添加到对应的统计中
        List<Integer> haveStatConclusionsChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtStatConclusions.keySet());
        List<Integer> haveScreeningPlansChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtScreeningPlans.keySet());
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        List<ScreeningPlan> totalScreeningPlans = haveScreeningPlansChildDistrictIds.stream().map(districtScreeningPlans::get).flatMap(Collection::stream).collect(Collectors.toList());
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        List<ScreeningPlan> selfScreeningPlans = districtScreeningPlans.getOrDefault(districtId, Collections.emptyList());

        genTotalStatistics(screeningNoticeId, districtId, totalScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics,  totalStatConclusions);
        genSelfStatistics(screeningNoticeId, districtId, selfScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics,  selfStatConclusions);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //下层级有数据，需要递归统计
            haveStatConclusionsChildDistrictIds.stream().filter(childDistrictId -> !districtId.equals(childDistrictId))
                    .forEach(childDistrictId -> genStatisticsByDistrictId(screeningNoticeId, childDistrictId, districtScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions));
        }
    }

    /**
     * 生成自己层级的筛查数据
     * @param screeningNoticeId
     * @param districtId
     * @param screeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param selfStatConclusions
     */
    private void genSelfStatistics(Integer screeningNoticeId, Integer districtId, List<ScreeningPlan> screeningPlans,
                                   List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics,
                                   List<DistrictVisionStatistic> districtVisionStatistics, List<StatConclusion> selfStatConclusions) {
        if (CollectionUtils.isEmpty(selfStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级自己的筛查数据肯定属于同一个任务，所以只取第一个的就可以
        Integer screeningTaskId = selfStatConclusions.get(0).getTaskId();
        Integer totalPlanStudentNum = screeningPlans.stream().mapToInt(ScreeningPlan::getStudentNumbers).sum();
        districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList())));
        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
    }

    /**
     * 生成层级所能看到的总的筛查数据
     * @param screeningNoticeId
     * @param districtId
     * @param screeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param totalStatConclusions
     */
    private void genTotalStatistics(Integer screeningNoticeId, Integer districtId, List<ScreeningPlan> screeningPlans,
                                    List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics,
                                    List<DistrictVisionStatistic> districtVisionStatistics, List<StatConclusion> totalStatConclusions) {
        if (CollectionUtils.isEmpty(totalStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        Integer screeningTaskId = CommonConst.DEFAULT_ID;
        Integer totalPlanStudentNum = screeningPlans.stream().mapToInt(ScreeningPlan::getStudentNumbers).sum();
        districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList())));
        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
    }

    /**
     * 获取层级所有子孙层级的ID
     * @param childDistrictIds
     * @param childDistrictList
     */
    private void getAllIdsWithChild(List<Integer> childDistrictIds, List<District> childDistrictList) {
        childDistrictIds.addAll(childDistrictList.stream().map(District::getId).collect(Collectors.toList()));
        childDistrictList.forEach(district -> {
            if (CollectionUtils.hasLength(district.getChild())) {
                getAllIdsWithChild(childDistrictIds, district.getChild());
            }
        });
    }
}