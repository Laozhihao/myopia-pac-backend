package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.api.management.domain.builder.DistrictAttentiveObjectsStatisticBuilder;
import com.wupol.myopia.business.api.management.domain.builder.SchoolMonitorStatisticBuilder;
import com.wupol.myopia.business.api.management.domain.builder.SchoolVisionStatisticBuilder;
import com.wupol.myopia.business.api.management.service.SchoolBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.dto.StudentExtraDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictAttentiveObjectsStatisticService;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author HaoHao
 * @Date 2022/8/24
 **/
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class StatisticScheduledTaskService {

    private final VisionScreeningResultService visionScreeningResultService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final ScreeningOrganizationService screeningOrganizationService;
    private final StatConclusionService statConclusionService;
    private final DistrictService districtService;
    private final SchoolService schoolService;
    private final DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    private final SchoolVisionStatisticService schoolVisionStatisticService;
    private final SchoolMonitorStatisticService schoolMonitorStatisticService;
    private final SchoolBizService schoolBizService;
    private final DistrictStatisticTask districtStatisticTask;
    private final SchoolStatisticTask schoolStatisticTask;
    private final ThreadPoolTaskExecutor asyncServiceExecutor;
    private final StudentService studentService;
    private final ScreeningResultStatisticService screeningResultStatisticService;


    /**
     * 生成统计数据
     *
     * @return void
     **/
    public void statistic() {
        log.info("开始统计昨天筛查数据......");
        //1. 查询出需要统计的计划（根据筛查数据vision_screening_result的更新时间判断）
        List<Integer> yesterdayScreeningPlanIds = visionScreeningResultService.getYesterdayScreeningPlanIds();
        if (CollectionUtil.isEmpty(yesterdayScreeningPlanIds)) {
            log.info("筛查数据统计：前一天无筛查数据，无需统计");
            return;
        }
        log.info("昨天有新数据需要统计......");
        //2. 生成学校视力和监测情况统计数据（主要用于统计分析菜单）
        statisticByPlanIds(yesterdayScreeningPlanIds);
        //3. 生成按区域统计、按学校统计数据
        screeningResultStatisticByPlanIds(yesterdayScreeningPlanIds, Collections.emptyList());
        log.info("统计完成。");
    }

    /**
     * 根据指定日期生成筛查结果统计数据
     * @param date 日期
     */
    public void statistic(String date,Integer planId,Boolean isAll,String exclude, Integer noticeId) {
        List<Integer> excludePlanIds = Optional.ofNullable(exclude).map(x -> Stream.of(x.split(",")).map(Integer::parseInt).collect(Collectors.toList())).orElse(Collections.emptyList());
        if(Objects.equals(isAll,Boolean.TRUE)){
            // 仅统计已经发布的计划
            List<Integer> yesterdayScreeningPlanIds = screeningPlanService.list().stream().filter(x -> CommonConst.STATUS_RELEASE.equals(x.getReleaseStatus())).map(ScreeningPlan::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(excludePlanIds)){
                yesterdayScreeningPlanIds = yesterdayScreeningPlanIds.stream().filter(id -> !excludePlanIds.contains(id)).collect(Collectors.toList());
            }
            if (CollectionUtil.isEmpty(yesterdayScreeningPlanIds)) {
                log.info("筛查数据统计：历史无筛查数据，无需统计");
                return;
            }
            log.info("筛查数据统计,共{}条筛查计划",yesterdayScreeningPlanIds.size());
            Collections.sort(yesterdayScreeningPlanIds);
            List<List<Integer>> planIdsList = ListUtil.split(yesterdayScreeningPlanIds, 20);
            for (int i = 0; i < planIdsList.size(); i++) {
                log.info("分批执行中...{}/{}",i+1,planIdsList.size());
                screeningResultStatisticByPlanIds(planIdsList.get(i), excludePlanIds);
            }
        } else {
            if (Objects.nonNull(planId)){
                log.info("通过筛查计划ID planId:{}生成筛查结果统计数据",planId);
                ScreeningPlan plan = screeningPlanService.getById(planId);
                Assert.isTrue(Objects.nonNull(plan) && CommonConst.STATUS_RELEASE.equals(plan.getReleaseStatus()), "不存在该计划，或该计划已作废/未发布！");
                screeningResultStatisticByPlanIds(Lists.newArrayList(planId), excludePlanIds);
            } else if (Objects.nonNull(noticeId)) {
                log.info("通过筛查筛查通知ID noticeId:{}，生成按区域统计数据", noticeId);
                screeningResultStatisticService.remove(new ScreeningResultStatistic().setScreeningNoticeId(noticeId));
                List<ScreeningPlan> planList = screeningPlanService.getAllReleasePlanByNoticeId(noticeId);
                screeningResultStatisticByPlanIds(planList.stream().map(ScreeningPlan::getId).collect(Collectors.toList()), excludePlanIds);
            } else if (StrUtil.isNotBlank(date)) {
                log.info("通过筛查计划日期 date:{} 生成筛查结果统计数据",date);
                List<Integer> planIds = visionScreeningResultService.getReleasePlanIdsByDate(date);
                if (CollectionUtil.isEmpty(planIds)){
                    log.info("筛查数据统计：{}无筛查数据，无需统计",date);
                    return;
                }
                screeningResultStatisticByPlanIds(planIds, excludePlanIds);
            }
        }
        log.info("筛查数据统计,数据处理完成");
    }

    /**
     * 根据筛查计划ID集合处理筛查结果统计
     * @param screeningPlanIds 筛查计划ID集合
     */
    private void screeningResultStatisticByPlanIds(List<Integer> screeningPlanIds, List<Integer> excludePlanIds){

        CompletableFuture<Void> districtFuture = CompletableFuture.runAsync(() -> {
            log.info("按区域统计开始");
            districtStatisticTask.districtStatistics(screeningPlanIds, excludePlanIds);
            log.info("按区域统计结束");
        }, asyncServiceExecutor);

        CompletableFuture<Void> schoolFuture = CompletableFuture.runAsync(() -> {
            log.info("按学校统计开始");
            schoolStatisticTask.schoolStatistics(screeningPlanIds);
            log.info("按学校统计结束");
        }, asyncServiceExecutor);

        CompletableFuture<Void> statisticFuture = CompletableFuture.runAsync(() -> {
            log.info("预警人群统计开始");
            List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
            genAttentiveObjectsStatistics(screeningPlanIds, districtAttentiveObjectsStatistics);
            districtAttentiveObjectsStatisticService.batchSaveOrUpdate(districtAttentiveObjectsStatistics);
            log.info("预警人群统计结束");
        },asyncServiceExecutor);

        CompletableFuture.allOf(districtFuture,schoolFuture,statisticFuture).join();
    }

    /**
     * 根据筛查计划ID进行筛查统计
     *
     * @param yesterdayScreeningPlanIds
     */
    private void statisticByPlanIds(List<Integer> yesterdayScreeningPlanIds) {
        List<SchoolVisionStatistic> schoolVisionStatistics = new ArrayList<>();
        List<SchoolMonitorStatistic> schoolMonitorStatistics = new ArrayList<>();
        genSchoolStatistics(yesterdayScreeningPlanIds, schoolVisionStatistics, schoolMonitorStatistics);
        schoolVisionStatisticService.batchSaveOrUpdate(schoolVisionStatistics);
        schoolMonitorStatisticService.batchSaveOrUpdate(schoolMonitorStatistics);
    }

    /**
     * 重点视力对象
     *
     * @param yesterdayScreeningPlanIds
     * @param districtAttentiveObjectsStatistics
     */
    private void genAttentiveObjectsStatistics(List<Integer> yesterdayScreeningPlanIds, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics) {
        // 1. 获取计划影响的学校的区域
        Set<Integer> schoolDistrictIds = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(yesterdayScreeningPlanIds);
        // 2. 根据学校的区域ID列表，组装需要重新计算的区域ID（省级到学校所在的区域所有层级）
        Set<Integer> districtIds = schoolDistrictIds.stream().map(districtId -> districtService.getDistrictPositionDetailById(districtId).stream().map(District::getId).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toSet());
        // 3. 组装省份的所有数据
        Map<Integer, Integer> districtIdProvinceIdMap = new HashMap<>(16);
        Set<Integer> provinceIdSet = new HashSet<>();
        Map<Integer, Map<Integer, List<StudentExtraDTO>>> provinceDistrictStudents = new HashMap<>(16);
        districtIds.forEach(districtId -> {
            Integer provinceId = districtService.getProvinceId(districtId);
            districtIdProvinceIdMap.put(districtId, provinceId);
            if (!provinceIdSet.contains(provinceId)) {
                provinceIdSet.add(provinceId);
                List<Integer> needGetStudentDistrictIds = CompareUtil.getRetain(districtService.getProvinceAllDistrictIds(provinceId), districtIds);
                provinceDistrictStudents.put(provinceId, studentService.getStudentsBySchoolDistrictIds(needGetStudentDistrictIds).stream().collect(Collectors.groupingBy(StudentExtraDTO::getDistrictId)));
            }
        });
        // 4. 循环districtIds，拿出所在省份的数据，然后分别统计自己/合计数据
        districtIds.forEach(districtId -> {
            Map<Integer, List<StudentExtraDTO>> districtStudents = provinceDistrictStudents.getOrDefault(districtIdProvinceIdMap.get(districtId), Collections.emptyMap());
            List<Integer> districtTreeAllIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            List<StudentExtraDTO> totalStudents = districtStudents.keySet().stream().filter(districtTreeAllIds::contains).map(id -> districtStudents.getOrDefault(id, Collections.emptyList())).flatMap(Collection::stream).collect(Collectors.toList());
            if (districtStudents.containsKey(districtId)) {
                districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatisticBuilder.build(districtId, CommonConst.NOT_TOTAL, districtStudents.get(districtId)));
            }
            districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatisticBuilder.build(districtId, CommonConst.IS_TOTAL, totalStudents));
        });
    }

    /**
     * 按学校生成统计数据
     *
     * @param yesterdayScreeningPlanIds
     * @param schoolVisionStatistics
     */
    private void genSchoolStatistics(List<Integer> yesterdayScreeningPlanIds, List<SchoolVisionStatistic> schoolVisionStatistics, List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        //3. 分别处理每个学校的统计
        yesterdayScreeningPlanIds.forEach(screeningPlanId -> {
            //3.1 查出计划对应的筛查数据(结果)
            List<StatConclusion> statConclusions = statConclusionService.getVoByScreeningPlanId(screeningPlanId);
            if (CollectionUtil.isEmpty(statConclusions)) {
                return;
            }
            Map<Integer, List<StatConclusion>> schoolIdStatConslusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
            ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
            if (Objects.isNull(screeningPlan)) {
                log.warn("按学校生成统计数据,筛查任务异常:{}", screeningPlanId);
                return;
            }
            Map<Integer, School> schoolIdMap = schoolService.getByIds(new ArrayList<>(schoolIdStatConslusions.keySet())).stream().collect(Collectors.toMap(School::getId, Function.identity()));
            ScreeningOrganization screeningOrg = screeningOrganizationService.getById(screeningPlan.getScreeningOrgId());
            Map<Integer, Long> planSchoolStudentNum = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
            //3.2 每个学校分别统计
            schoolIdStatConslusions.keySet().forEach(schoolId -> {
                List<StatConclusion> schoolStatConclusion = schoolIdStatConslusions.get(schoolId);
                Map<Boolean, List<StatConclusion>> isValidMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
                Map<Boolean, List<StatConclusion>> isRescreenTotalMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
                Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                int planSchoolScreeningNumbers = planSchoolStudentNum.getOrDefault(schoolId, 0L).intValue();
                int reslScreeningNumbers = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();
                schoolVisionStatistics.add(SchoolVisionStatisticBuilder.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlanId, isRescreenMap.getOrDefault(false, Collections.emptyList()), reslScreeningNumbers, planSchoolScreeningNumbers));
                schoolMonitorStatistics.add(SchoolMonitorStatisticBuilder.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlanId, isRescreenMap.getOrDefault(true, Collections.emptyList()), planSchoolScreeningNumbers, reslScreeningNumbers));
            });
        });
    }

}
