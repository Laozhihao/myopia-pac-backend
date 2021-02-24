package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.dto.stat.FocusObjectsStatisticVO;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningMonitorStatisticVO;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningSchoolVisionStatisticVO;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningVisionStatisticVO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
public class StatController {
    @Autowired
    private StatService statService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;
    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private SchoolVisionStatisticService schoolVisionStatisticService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    /**
     * 获取预警信息
     */
    @GetMapping("warningList")
    public ApiResult getWarningList() {
        // TODO: Mocking Data
        return ApiResult.success(statService.getWarningList());
    }

    /**
     * 获取年度通知列表
     */
    @GetMapping("briefNotificationList")
    public ApiResult getBriefNotificationList() {
        return ApiResult.success(statService.getBriefNotificationList());
    }

    /**
     * 获取筛查对比数据
     * @param contrastTypeCode 对比类型
     * @param notificationId1 1号通知ID
     * @param notificationId2 2号通知ID
     * @param districtId 区域ID
     * @param schoolAge 学龄代码
     * @return
     */
    @GetMapping("/dataContrast")
    public ApiResult getScreeningDataContrast(
            @RequestParam("contrastType") Integer contrastTypeCode,
            @RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) {
        return ApiResult.success(statService.getScreeningDataContrast(
                contrastTypeCode, notificationId1, notificationId2, districtId, schoolAge));
    }

    /**
     * 分类统计数据
     * @param notificationId 通知ID
     * @return
     */
    @GetMapping("/dataClass")
    public ApiResult getScreeningClassStat(@RequestParam("nid") Integer notificationId) {
        return ApiResult.success(statService.getScreeningClassStat(notificationId));
    }

    /**
     * 重点视力对象
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/attentive-objects-statistic")
    public FocusObjectsStatisticVO getAttenticeObjectsStatistic(Integer districtId) throws IOException {
        //下级层级
        List<District> districts = districtService.getChildDistrictByParentCodePriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        districtIds.add(districtId);
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = districtAttentiveObjectsStatisticService.getStatisticDtoByDistrictIdAndTaskId(districtIds);
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics)) {
            return FocusObjectsStatisticVO.getEmptyInstance();
        }
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);
        //获取数据
        return FocusObjectsStatisticVO.getInstance(districtAttentiveObjectsStatistics, districtId, currentRangeName, districtIdNameMap);
    }

    /**
     * 地区视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-vision-result")
    public ScreeningVisionStatisticVO getDistrictVisionStatistic(
            @NotNull Integer districtId, @NotNull Long noticeId) {
        //下级层级
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        Set<Integer> notices = new HashSet<>();
        notices.add(screeningNotice.getId());
        List<ScreeningTask> screeningTasks = screeningTaskService.getTaskByNoticeIds(notices);
        Set<Integer> screeningTaskIds = screeningTasks.stream().map(ScreeningTask::getId).collect(Collectors.toSet());
        //根据层级获取数据
        List<DistrictVisionStatistic> districtVisionStatistics = districtVisionStatisticService.getStatisticDtoByTaskIds(screeningTaskIds);
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return ScreeningVisionStatisticVO.getEmptyInstance();
        }
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        List<Integer> districtIds = screeningTasks.stream().map(ScreeningTask::getDistrictId).collect(Collectors.toList());
        List<District> districts = districtService.getDistrictByIds(districtIds);
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);
        //获取数据
        return ScreeningVisionStatisticVO.getInstance(districtVisionStatistics, districtId, currentRangeName, screeningNotice, districtIdNameMap);
    }

    /**
     * 地区监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-monitor-result")
    public ScreeningMonitorStatisticVO getDistrictMonitorStatistic(
            Integer districtId, Long noticeId) throws IOException {
        //下级层级
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        Set<Integer> notices = new HashSet<>();
        notices.add(screeningNotice.getId());
        List<ScreeningTask> screeningTasks = screeningTaskService.getTaskByNoticeIds(notices);
        Set<Integer> screeningTaskIds = screeningTasks.stream().map(ScreeningTask::getId).collect(Collectors.toSet());
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictMonitorStatistic> districtMonitorStatistics = districtMonitorStatisticService.getStatisticDtoByTaskIds(screeningTaskIds);
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return ScreeningMonitorStatisticVO.getEmptyInstance();
        }
        //获取task详情
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        List<Integer> districtIds = screeningTasks.stream().map(ScreeningTask::getDistrictId).collect(Collectors.toList());
        List<District> districts = districtService.getDistrictByIds(districtIds);
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);
        //获取数据
        return ScreeningMonitorStatisticVO.getInstance(districtMonitorStatistics, districtId, currentRangeName, screeningNotice, districtIdNameMap);
    }

    /**
     * 学校视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/school/screening-vision-result")
    public ScreeningSchoolVisionStatisticVO getSchoolVisionStatistic(@NotNull Integer districtId, @NotNull Integer noticeId) throws IOException {
        // 获取当前层级下，所有参与任务的学校
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        Set<Integer> notices = new HashSet<>();
        notices.add(screeningNotice.getId());
        List<ScreeningTask> screeningTasks = screeningTaskService.getTaskByNoticeIds(notices);
        Set<Integer> screeningTaskIds = screeningTasks.stream().map(ScreeningTask::getId).collect(Collectors.toSet());
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<SchoolVisionStatistic> districtVisionStatistics = schoolVisionStatisticService.getStatisticDtoByTaskIdsAndOrgId(screeningTaskIds, CurrentUserUtil.getCurrentUser());
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return ScreeningSchoolVisionStatisticVO.getEmptyInstance();
        }
        //获取当前范围名
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        //获取数据
        return ScreeningSchoolVisionStatisticVO.getInstance(districtVisionStatistics, districtName, screeningNotice);
    }

    /**
     * 学校监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/school/screening-monitor-result")
    public ScreeningMonitorStatisticVO getSchoolMonitorStatistic(Integer districtId, Integer taskId) throws IOException {
        // 获取当前层级下，所有参与任务的学校
        List<District> districts = districtService.getChildDistrictByParentCodePriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        districtIds.add(districtId);
        Set<Integer> schoolIds = screeningPlanService.getBaseMapper().selectSchoolIds(districtIds, taskId);
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictMonitorStatistic> districtMonitorStatistics = null;//s.getStatisticDtoByDistrictIdAndTaskId(districtIds, taskId);
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return ScreeningMonitorStatisticVO.getEmptyInstance();
        }
        //获取task详情
        ScreeningTask screeningTask = screeningTaskService.getById(taskId);
        //获取当前范围名
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        //获取数据
        return null;
    }

}
