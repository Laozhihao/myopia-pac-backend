package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.service.StatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
