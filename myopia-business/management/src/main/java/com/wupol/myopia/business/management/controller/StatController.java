package com.wupol.myopia.business.management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.ScreeningDataContrastType;
import com.wupol.myopia.business.management.domain.dto.stat.*;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.RescreenStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningDataContrast;
import com.wupol.myopia.business.management.domain.dto.stat.TaskBriefNotification;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.management.service.StatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
public class StatController {
    @Autowired
    private StatService statService;

    @GetMapping("warningList")
    public ApiResult getWarningList() {
        // TODO: Mocking Data
        return ApiResult.success(statService.getWarningList());
    }

    @GetMapping("briefNotificationList")
    public ApiResult getBriefNotificationList() {
        return ApiResult.success(statService.getBriefNotificationList());
    }

    @GetMapping("/dataContrast")
    public ApiResult getScreeningDataContrast(
            @RequestParam("contrastType") Integer contrastTypeCode,
            @RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) {
        return ApiResult.success(statService.getScreeningDataContrast(
                contrastTypeCode, notificationId1, notificationId2, districtId, schoolAge));
    }

    @GetMapping("/dataClass")
    public ApiResult getScreeningClassStat(@RequestParam("nid") Integer notificationId) {
        return ApiResult.success(statService.getScreeningClassStat(notificationId));
    }

    @GetMapping("tastStat")
    public ApiResult getTaskStat(Integer nid) {
        return ApiResult.success();
    }

    private Float convertToRatio(Float num) {
        return Math.round(num * 1000000) / 10000f;
    }

    private Long getYearMillis(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, year);
        return cal.getTimeInMillis();
    }


    /**
     * 重点视力对象
     *
     * @param districtId
     * @return
     */
    @GetMapping("/attentive-objects-statistic")
    public FocusObjectsStatisticDTO getAttenticeObjectsStatistic(Long districtId) {
        return new FocusObjectsStatisticDTO();
    }

    /**
     * 地区视力情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-vision-result")
    public ScreeningVisionStatisticDTO getDistrictVisionStatistic(@NotNull Long districtId, @NotNull Long taskId) throws JsonProcessingException {
        return new ScreeningVisionStatisticDTO();
    }

    /**
     * 地区监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/district/screening-monitor-result")
    public ScreeningMonitorStatisticDTO getDistrictMonitorStatistic(@NotNull Long districtId, @NotNull Long taskId) throws JsonProcessingException {
        return new ScreeningMonitorStatisticDTO();
    }


    /**
     * 学校视力情况
     *
     * @param schoolId
     * @return
     */
    @GetMapping("/school/screening-vision-result")
    public ScreeningSchoolVisionStatisticDTO getSchoolVisionStatistic(@NotNull Long schoolId, @NotNull Long taskId) throws JsonProcessingException {
        return new ScreeningSchoolVisionStatisticDTO();
    }

    /**
     * 学校监控情况
     *
     * @param districtId
     * @return
     */
    @GetMapping("/school/screening-monitor-result")
    public ScreeningMonitorStatisticDTO getSchoolMonitorStatistic(@NotNull Long districtId, @NotNull Long taskId) throws JsonProcessingException {
        return new ScreeningMonitorStatisticDTO();
    }

}
