package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.stat.TaskBriefNotification;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        Long keyObjNum = 123455L;
        Long lastKeyObjNum = 164321L;
        return ApiResult.success(
                WarningInfo.builder()
                        .statTime(System.currentTimeMillis())
                        .keyObjNum(keyObjNum)
                        .keyObjPercentage(keyObjNum * 100f / normalTotal)
                        .lastStatTime(getYearMillis(-1))
                        .lastKeyObjNum(lastKeyObjNum)
                        .lastKeyObjPercentage(lastKeyObjNum * 100f / normalTotal)
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
        int startYear = 2017;
        Long period = 3 * 30 * 24 * 60 * 60 * 1000L;
        List<TaskBriefNotification> list = new ArrayList<TaskBriefNotification>() {
            {
                for (int i = 0; i < 4; i++) {
                    add(new TaskBriefNotification(i, title + (startYear + i), getYearMillis(i),
                            getYearMillis(i) + period));
                }
            }
        };
        return ApiResult.success(list);
    }

    @GetMapping("rescreenInfo")
    public ApiResult getRescreenInfo() {
        return ApiResult.success(list);
    }

    private Long getYearMillis(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, year);
        return cal.getTimeInMillis();
    }
}
