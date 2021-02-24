package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.stat.FocusObjectsStatisticVO;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningMonitorStatisticVO;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningVisionStatisticVO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.extern.log4j.Log4j2;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
@Log4j2
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

    /**
     * 获取预警信息
     */
    @GetMapping("warningList")
    public ApiResult getWarningList() {
        try {
            return ApiResult.success(statService.getWarningList());
        } catch (IOException e) {
            log.error(e);
        }
        return ApiResult.failure("internal error");
    }

    /**
     * 获取筛查对比数据
     * @param notificationId1 1号通知ID
     * @param notificationId2 2号通知ID
     * @param districtId 区域ID
     * @param schoolAge 学龄代码
     * @return
     */
    @GetMapping("/dataContrast")
    public ApiResult getScreeningDataContrast(@RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) {
        try {
            return ApiResult.success(statService.getScreeningDataContrast(
                    notificationId1, notificationId2, districtId, schoolAge));
        } catch (IOException e) {
            log.error(e);
        }
        return ApiResult.failure("internal error");
    }

    /**
     * 导出筛查对比数据
     * @param notificationId1 1号通知ID
     * @param notificationId2 2号通知ID
     * @param districtId 区域ID
     * @param schoolAge 学龄代码
     * @return
     */
    @GetMapping("/exportContrast")
    public ApiResult exportScreeningDataContrast(@RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) {
        try {
            statService.exportStatContrast(notificationId1, notificationId2, districtId, schoolAge);
            return ApiResult.success();
        } catch (IOException e) {
            log.error(e);
        } catch (UtilException e) {
            log.error(e);
        }
        return ApiResult.failure("internal error");
    }
    /**
     * 分类统计数据
     * @param notificationId 通知ID
     * @return
     */
    @GetMapping("/dataClass")
    public ApiResult getScreeningClassStat(@RequestParam("nid") Integer notificationId) {
        try {
            return ApiResult.success(statService.getScreeningClassStat(notificationId));
        } catch (IOException e) {
            log.error(e);
        }
        return ApiResult.failure("internal error");
    }

    /**
     * 重点视力对象
     *
     * @param districtId
     * @return
     */
    @GetMapping("/attentive-objects-statistic")
    public FocusObjectsStatisticVO getAttenticeObjectsStatistic(Integer districtId, Integer taskId) throws IOException {
        //下级层级
        List<District> districts = districtService.getChildDistrictByParentCodePriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        districtIds.add(districtId);
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = districtAttentiveObjectsStatisticService.getStatisticDtoByDistrictIdAndTaskId(districtIds, taskId);
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics))  {
            return  FocusObjectsStatisticVO.getEmptyInstance();
        }
        //获取task详情
        ScreeningTask screeningTask = screeningTaskService.getById(taskId);
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId,currentRangeName);
        //获取数据
        return FocusObjectsStatisticVO.getInstance(districtAttentiveObjectsStatistics, districtId, currentRangeName, screeningTask,districtIdNameMap);
    }

    /**
     * 地区视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-vision-result")
    public ScreeningVisionStatisticVO getDistrictVisionStatistic(
            @NotNull Integer districtId, @NotNull Long taskId) throws IOException {
        //下级层级
        List<District> districts = districtService.getChildDistrictByParentCodePriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        districtIds.add(districtId);
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictVisionStatistic> districtVisionStatistics = districtVisionStatisticService.getStatisticDtoByDistrictIdAndTaskId(districtIds, taskId);
        if (CollectionUtils.isEmpty(districtVisionStatistics))  {
            return  ScreeningVisionStatisticVO.getEmptyInstance();
        }
        //获取task详情
        ScreeningTask screeningTask = screeningTaskService.getById(taskId);
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId,currentRangeName);
        //获取数据
        return ScreeningVisionStatisticVO.getInstance(districtVisionStatistics, districtId, currentRangeName, screeningTask,districtIdNameMap);
    }

    /**
     * 地区监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-monitor-result")
    public ScreeningMonitorStatisticVO getDistrictMonitorStatistic(
           Integer districtId, Long taskId) throws IOException {
        //下级层级
        List<District> districts = districtService.getChildDistrictByParentCodePriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        districtIds.add(districtId);
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictMonitorStatistic> districtMonitorStatistics = districtMonitorStatisticService.getStatisticDtoByDistrictIdAndTaskId(districtIds, taskId);
        if (CollectionUtils.isEmpty(districtMonitorStatistics))  {
            return  ScreeningMonitorStatisticVO.getEmptyInstance();
        }
        //获取task详情
        ScreeningTask screeningTask = screeningTaskService.getById(taskId);
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId,currentRangeName);
        //获取数据
        return ScreeningMonitorStatisticVO.getInstance(districtMonitorStatistics, districtId, currentRangeName, screeningTask,districtIdNameMap);
    }
}
