package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.service.StatReportService;
import com.wupol.myopia.business.management.service.StatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
@Log4j2
public class StatController {
    @Autowired
    private StatService statService;

    @Autowired
    private StatReportService statReportService;

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
     * 获取区域筛查报告
     * @param notificationId 通知ID
     * @param districtId 区域ID
     * @return
     */
    @GetMapping("/getDistrictReport")
    public ApiResult getDistrictReport(@RequestParam("notificationId") Integer notificationId,
            @RequestParam("districtId") Integer districtId) {
        try {
            return ApiResult.success(
                    statReportService.getDistrictStatData(notificationId, districtId));
        } catch (IOException e) {
            log.error(e);
            return ApiResult.failure("internal error");
        }
    }

    /**
     * 获取学校筛查报告
     * @param notificationId 通知ID
     * @param schoolId 学校ID
     * @return
     */
    @GetMapping("/getSchoolReport")
    public ApiResult getSchoolReport(@RequestParam("notificationId") Integer notificationId,
            @RequestParam("schoolId") Integer schoolId) {
        try {
            return ApiResult.success(statReportService.getSchoolStatData(notificationId, schoolId));
        } catch (IOException e) {
            log.error(e);
            return ApiResult.failure("internal error");
        }
    }

    /**
     * 分类统计数据
     *
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
}
