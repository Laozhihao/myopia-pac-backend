package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.stat.TaskBriefNotification;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("warningList")
    public ApiResult getWarningList() {
        // TODO: Mocking Data
        Long total = 617225L;
        Long normalTotal = 493824L;
        Long focusTargetsNum = 123455L;
        Long lastFocusTargetsNum = 164321L;
        return ApiResult.success(
                WarningInfo.builder()
                        .statTime(System.currentTimeMillis())
                        .focusTargetsNum(focusTargetsNum)
                        .focusTargetsPercentage(focusTargetsNum * 100f / normalTotal)
                        .lastStatTime(getYearMillis(-1))
                        .lastFocusTargetsNum(lastFocusTargetsNum)
                        .lastFocusTargetsPercentage(lastFocusTargetsNum * 100f / normalTotal)
                        .warningLevelInfoList(new ArrayList<WarningLevelInfo>() {
                            {
                                add(new WarningLevelInfo(0, 123443L, 123443L * 100f / total));
                                add(new WarningLevelInfo(1, 123278L, 123278L * 100f / total));
                                add(new WarningLevelInfo(2, 113445L, 113445L * 100f / total));
                                add(new WarningLevelInfo(3, 33445L, 33445L * 100f / total));
                            }
                        })
                        .build());
    }

    @GetMapping("briefNotificationList")
    public ApiResult getBriefNotificationList() {
        String title = "筛查通知";
        Long period = 3 * 30 * 24 * 60 * 60 * 1000L;
        String[] year = {"2017", "2018", "2019", "2020"};
        List list = new ArrayList();
        for (int i = 0; i < 4; i++) {
            Map map = new HashMap();
            map.put("year", year[i]);
            List<TaskBriefNotification> briefNotificationLists = new ArrayList();
            for (int j = 0; j < 2; j++) {
                briefNotificationLists.add(new TaskBriefNotification(Integer.valueOf(i + "" + j),
                        title + year[i] + "第" + j + "次筛查", getYearMillis(i),
                        getYearMillis(i) + period));
            }
            map.put("notifications", briefNotificationLists);
            list.add(map);
        }
        return ApiResult.success(list);
    }

    @GetMapping("rescreenInfo")
    public ApiResult getRescreenInfo() {
        return ApiResult.success();
    }

    @GetMapping("/dataCompare")
    public ApiResult dataCompare(@RequestParam("compareType") Integer compareType,
            @RequestParam("compareOneId") Integer compareOneId,
            @RequestParam(value = "compareTwoId", required = false) Integer compareTwoId) {
        return ApiResult.success();
    }

    private Long getYearMillis(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, year);
        return cal.getTimeInMillis();
    }
}
