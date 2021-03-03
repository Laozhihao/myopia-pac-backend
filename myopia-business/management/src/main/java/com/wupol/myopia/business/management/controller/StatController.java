package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
public class StatController {
    @Autowired
    private StatService statService;

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
     *
     * @param contrastTypeCode 对比类型
     * @param notificationId1  1号通知ID
     * @param notificationId2  2号通知ID
     * @param districtId       区域ID
     * @param schoolAge        学龄代码
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
     *
     * @param notificationId 通知ID
     * @return
     */
    @GetMapping("/dataClass")
    public ApiResult getScreeningClassStat(@RequestParam("nid") Integer notificationId) {
        return ApiResult.success(statService.getScreeningClassStat(notificationId));
    }


}
