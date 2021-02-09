package com.wupol.myopia.business.management.service;

import com.wupol.myopia.business.management.constant.ScreeningDataContrastType;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.RescreenStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningDataContrast;
import com.wupol.myopia.business.management.domain.dto.stat.TaskBriefNotification;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class StatService {
    /**
     * 预警信息
     * @return
     */
    public WarningInfo getWarningList() {
        Integer total = 617225;
        Integer normalTotal = 493824;
        Integer focusTargetsNum = 123455;
        Integer lastFocusTargetsNum = 164321;
        return WarningInfo.builder()
                .statTime(System.currentTimeMillis())
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(convertToRatio(focusTargetsNum * 1f / normalTotal))
                .lastStatTime(getYearMillis(-1))
                .lastFocusTargetsNum(lastFocusTargetsNum)
                .lastFocusTargetsPercentage(convertToRatio(lastFocusTargetsNum * 1f / normalTotal))
                .warningLevelInfoList(new ArrayList<WarningLevelInfo>() {
                    {
                        add(new WarningLevelInfo(0, 123443, convertToRatio(123443 * 1f / total)));
                        add(new WarningLevelInfo(1, 123278, convertToRatio(123278 * 1f / total)));
                        add(new WarningLevelInfo(2, 113445, convertToRatio(113445 * 1f / total)));
                        add(new WarningLevelInfo(3, 33445, convertToRatio(33445 * 1f / total)));
                    }
                })
                .build();
    }

    /**
     * 通知列表
     * @return
     */
    public List<TaskBriefNotification> getBriefNotificationList() {
        // TODO: get notification list

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
        return list;
    }

    /**
     * 对比统计数据
     * @param contrastTypeCode 对比类型
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @param districtId 区域ID
     * @param schoolAge 学龄段
     * @return
     */
    public Map getScreeningDataContrast(Integer contrastTypeCode, Integer notificationId1,
            Integer notificationId2, Integer districtId, Integer schoolAge) {
        ScreeningDataContrastType contrastType = ScreeningDataContrastType.get(contrastTypeCode);
        if (contrastType == null) {
            return null;
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
                        .rescreenItemNum(Math.round(actualScrNum * 0.25f * 0.35f) * 4
                                + Math.round(actualScrNum * 0.25f * 0.65f) * 6)
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
                        .rescreenItemNum(Math.round(actualScrNum2 * 0.25f * 0.35f) * 4
                                + Math.round(actualScrNum2 * 0.25f * 0.65f) * 6)
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
        return new HashMap() {
            {
                put("result1", data1);
                if (notificationId2 != null && notificationId2 > 0) {
                    put("result2", data2);
                }
            }
        };
    }

    /**
     * 分类统计
     * @param notificationId 通知ID
     * @return
     */
    public ScreeningClassStat getScreeningClassStat(Integer notificationId) {
        int screeningNum = 123456;
        int actualScreeningNum = 111321;
        int rescreenNum = 44498;
        int wearingGlassesRescreenNum = 35898;
        int wearingGlassesRescreenIndexNum = 6;
        int withoutGlassesRescreenNum = 23898;
        int withoutGlassesRescreenIndexNum = 4;
        int rescreenItemNum = wearingGlassesRescreenNum * wearingGlassesRescreenIndexNum
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
                                          .validScreeningNum(actualScreeningNum - 1000)
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
        return stat;
    }

    /**
     * 转换为百分比并保留4位小数
     * @param num
     * @return
     */
    private Float convertToRatio(Float num) {
        return Math.round(num * 1000000) / 10000f;
    }

    /**
     * 获取当前时间增加的年份时间戳
     * @param year
     * @return
     */
    private Long getYearMillis(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, year);
        return cal.getTimeInMillis();
    }
}
