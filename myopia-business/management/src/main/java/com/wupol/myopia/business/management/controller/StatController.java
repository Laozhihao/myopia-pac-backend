package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.ScreeningDataContrastType;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.RescreenStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningDataContrast;
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
        Integer total = 617225;
        Integer normalTotal = 493824;
        Integer focusTargetsNum = 123455;
        Integer lastFocusTargetsNum = 164321;
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
                                add(new WarningLevelInfo(0, 123443, 123443 * 100f / total));
                                add(new WarningLevelInfo(1, 123278, 123278 * 100f / total));
                                add(new WarningLevelInfo(2, 113445, 113445 * 100f / total));
                                add(new WarningLevelInfo(3, 33445, 33445 * 100f / total));
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

    @GetMapping("/dataContrast")
    public ApiResult getScreeningDataContrast(
            @RequestParam("contrastType") Integer contrastTypeCode,
            @RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) {
        ScreeningDataContrastType contrastType = ScreeningDataContrastType.get(contrastTypeCode);
        if (contrastType == null) {
            return ApiResult.failure("数据不正确");
        }
        // TODO: Deal according different params
        switch (contrastType) {
            case TIME:
                break;
            case TIME_N_DISTRICT:
                break;
            case TIME_N_SCHOOL_AGE:
                break;
            case TIME_N_DISTRICT_N_SCHOOL_AGE:
        }
        Integer actualScrNum = 1111111;
        RescreenStat rescreenStat =
                RescreenStat.builder()
                        .rescreenNum(Math.round(actualScrNum * 0.25f))
                        .wearingGlassesRescreenNum(Math.round(actualScrNum * 0.25f * 0.35f))
                        .wearingGlassesRescreenIndexNum(
                                Math.round(actualScrNum * 0.25f * 0.35f) * 4)
                        .withoutGlassesRescreenNum(Math.round(actualScrNum * 0.25f * 0.65f))
                        .withoutGlassesRescreenIndexNum(
                                Math.round(actualScrNum * 0.25f * 0.65f) * 6)
                        .rescreenItemNum(Math.round(actualScrNum * 0.25d * 0.35d) * 4
                                + Math.round(actualScrNum * 0.25d * 0.65d) * 6)
                        .incorrectItemNum(Math.round(actualScrNum * 0.0015f))
                        .incorrectRatio(convertToRatio(0.0015f))
                        .build();
        ScreeningDataContrast data1 =
                ScreeningDataContrast.builder()
                        .screeningNum(1234565)
                        .actualScreeningNum(actualScrNum)
                        .averageVisionLeft(0.83f)
                        .averageVisionRight(0.85f)
                        .lowVisionRatio(convertToRatio(350000f / actualScrNum))
                        .refractiveErrorRatio(convertToRatio(0.23f))
                        .wearingGlassesRatio(convertToRatio(0.53f))
                        .myopiaNum(350000)
                        .myopiaRatio(convertToRatio(350000f / actualScrNum))
                        .focusTargetsNum(350000)
                        .warningLevelZeroRatio(convertToRatio(123430f / actualScrNum))
                        .warningLevelOneRatio(convertToRatio(23430f / actualScrNum))
                        .warningLevelTwoRatio(convertToRatio(10020f / actualScrNum))
                        .warningLevelThreeRatio(convertToRatio(8430f / actualScrNum))
                        .recommendVisitNum(123430)
                        .screeningFinishedRatio(convertToRatio(123430 * 0.2f / actualScrNum))
                        .rescreenStat(rescreenStat)
                        .build();

        Integer actualScrNum2 = 1010101;
        RescreenStat rescreenStat2 =
                RescreenStat.builder()
                        .rescreenNum(Math.round(actualScrNum2 * 0.25f))
                        .wearingGlassesRescreenNum(Math.round(actualScrNum2 * 0.25f * 0.35f))
                        .wearingGlassesRescreenIndexNum(
                                Math.round(actualScrNum2 * 0.25f * 0.35f) * 4)
                        .withoutGlassesRescreenNum(Math.round(actualScrNum2 * 0.25f * 0.65f))
                        .withoutGlassesRescreenIndexNum(
                                Math.round(actualScrNum2 * 0.25f * 0.65f) * 6)
                        .rescreenItemNum(Math.round(actualScrNum2 * 0.25d * 0.35d) * 4
                                + Math.round(actualScrNum2 * 0.25d * 0.65d) * 6)
                        .incorrectItemNum(Math.round(actualScrNum2 * 0.0015f))
                        .incorrectRatio(convertToRatio(0.0015f))
                        .build();
        ScreeningDataContrast data2 =
                ScreeningDataContrast.builder()
                        .screeningNum(2234565)
                        .actualScreeningNum(actualScrNum2)
                        .averageVisionLeft(0.83f)
                        .averageVisionRight(0.85f)
                        .lowVisionRatio(convertToRatio(0.43f))
                        .refractiveErrorRatio(convertToRatio(0.23f))
                        .wearingGlassesRatio(convertToRatio(0.53f))
                        .myopiaNum(350000)
                        .myopiaRatio(convertToRatio(350000 * 1.0f / actualScrNum2))
                        .focusTargetsNum(350000)
                        .warningLevelZeroRatio(convertToRatio(123430 * 1f / actualScrNum2))
                        .warningLevelOneRatio(convertToRatio(23430 * 1f / actualScrNum2))
                        .warningLevelTwoRatio(convertToRatio(10020 * 1f / actualScrNum2))
                        .warningLevelThreeRatio(convertToRatio(8430 * 1f / actualScrNum2))
                        .recommendVisitNum(123430)
                        .screeningFinishedRatio(convertToRatio(123430 * 0.2f / actualScrNum2))
                        .rescreenStat(rescreenStat2)
                        .build();
        Map result = new HashMap() {
            {
                put("result1", data1);
                if (notificationId2 != null && notificationId2 > 0) {
                    put("result2", data2);
                }
            }
        };
        return ApiResult.success(result);
    }

    @GetMapping("/dataClass")
    public ApiResult getScreeningClassStat(@RequestParam("nid") Integer notificationId) {
        int screeningNum = 123456;
        int actualScreeningNum = 111321;
        int rescreenNum = 44498;
        int wearingGlassesRescreenNum = 35898;
        int wearingGlassesRescreenIndexNum = 6;
        int withoutGlassesRescreenNum = 23898;
        int withoutGlassesRescreenIndexNum = 4;
        long rescreenItemNum = wearingGlassesRescreenNum * wearingGlassesRescreenIndexNum
                + withoutGlassesRescreenNum * withoutGlassesRescreenIndexNum;
        int incorrectItemNum = 12345;
        float incorrectRatio = convertToRatio(incorrectItemNum * 1f / actualScreeningNum);
        int maleNum = 54498;
        int lowVisionNum = 67789;

        BasicStatParams male =
                new BasicStatParams(convertToRatio(maleNum * 1f / actualScreeningNum), maleNum);
        BasicStatParams female = male;
        BasicStatParams kindergarten = male;
        BasicStatParams primary = male;
        BasicStatParams junior = male;
        BasicStatParams high = male;
        BasicStatParams vocationalHigh = male;

        ClassStat lowVision = new ClassStat(convertToRatio(lowVisionNum * 1f / actualScreeningNum),
                lowVisionNum, male, female, kindergarten, primary, junior, high, vocationalHigh);
        ClassStat refractiveError = lowVision;
        ClassStat wearingGlasses = lowVision;
        ClassStat myopia = lowVision;

        RescreenStat rescreenStat =
                RescreenStat.builder()
                        .rescreenNum(rescreenNum)
                        .wearingGlassesRescreenNum(wearingGlassesRescreenNum)
                        .wearingGlassesRescreenIndexNum(wearingGlassesRescreenIndexNum)
                        .withoutGlassesRescreenNum(withoutGlassesRescreenNum)
                        .withoutGlassesRescreenIndexNum(withoutGlassesRescreenIndexNum)
                        .rescreenItemNum(rescreenItemNum)
                        .incorrectItemNum(incorrectItemNum)
                        .incorrectRatio(incorrectRatio)
                        .build();

        ScreeningClassStat stat = ScreeningClassStat.builder()
                                          .notificationId(15)
                                          .screeningNum(screeningNum)
                                          .actualScreeningNum(actualScreeningNum)
                                          .screeningFinishedRatio(convertToRatio(
                                                  actualScreeningNum * 1f / screeningNum))
                                          .averageVisionLeft(0.5f)
                                          .averageVisionRight(0.48f)
                                          .lowVision(lowVision)
                                          .refractiveError(refractiveError)
                                          .wearingGlasses(wearingGlasses)
                                          .myopia(myopia)
                                          .rescreenStat(rescreenStat)
                                          .build();
        return ApiResult.success(stat);
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
}
