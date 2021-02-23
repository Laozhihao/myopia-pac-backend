package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.ScreeningDataContrastType;
import com.wupol.myopia.business.management.constant.StatClassLabel;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.RescreenStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningDataContrast;
import com.wupol.myopia.business.management.domain.dto.stat.TaskBriefNotification;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class StatService {
    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    @Autowired
    private ScreeningTaskService screeningTaskService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Autowired
    private GovDeptService govDeptService;

    /**
     * 预警信息
     * @return
     * @throws IOException
     */
    public WarningInfo getWarningList() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        GovDept govDept = govDeptService.getById(currentUser.getOrgId());
        District userDistrict = districtService.getById(govDept.getDistrictId());
        List<District> districts;
        try {
            districts =
                    districtService.getChildDistrictByParentIdPriorityCache2(userDistrict.getId());
        } catch (IOException e) {
            //  TODO: add log
            return null;
        }
        districts.add(userDistrict);
        List<Integer> districtIds =
                districts.stream().map(District::getId).collect(Collectors.toList());

        StatConclusion lastConclusion = statConclusionService.getLastOne(districtIds);
        Date endDate = lastConclusion.getCreateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.YEAR, -1);
        Date startDate = calendar.getTime();

        // DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        // String startDateStr = dateFormat.format(startDate);
        // String endDateStr = dateFormat.format(endDate);

        List<StatConclusion> statConclusions =
                statConclusionService.listByDateRange(districtIds, startDate, endDate);

        long total = statConclusions.size();
        long warningZeroNum = statConclusions.stream().map(x -> x.getWarningLevel() == 0).count();
        long warningOneNum = statConclusions.stream().map(x -> x.getWarningLevel() == 1).count();
        long warningTwoNum = statConclusions.stream().map(x -> x.getWarningLevel() == 2).count();
        long warningThreeNum = statConclusions.stream().map(x -> x.getWarningLevel() == 3).count();
        long focusTargetsNum = warningOneNum + warningTwoNum + warningThreeNum;

        return WarningInfo.builder()
                .statTime(startDate.getTime())
                .endTime(endDate.getTime())
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(convertToRatio(focusTargetsNum * 1f / total))
                .warningLevelInfoList(new ArrayList<WarningLevelInfo>() {
                    {
                        add(new WarningLevelInfo(
                                0, warningZeroNum, convertToRatio(warningZeroNum * 1f / total)));
                        add(new WarningLevelInfo(
                                1, warningOneNum, convertToRatio(warningOneNum * 1f / total)));
                        add(new WarningLevelInfo(
                                2, warningTwoNum, convertToRatio(warningTwoNum * 1f / total)));
                        add(new WarningLevelInfo(
                                3, warningThreeNum, convertToRatio(warningThreeNum * 1f / total)));
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

        BasicStatParams male = new BasicStatParams(
                "男", convertToRatio(maleNum * 1f / actualScreeningNum), maleNum);
        BasicStatParams female = new BasicStatParams(
                "女", convertToRatio(maleNum * 1f / actualScreeningNum), maleNum);

        List<ClassStat> tabGender = new ArrayList<ClassStat>() {
            {
                for (StatClassLabel label : StatClassLabel.values()) {
                    add(ClassStat.builder()
                                    .title(label.name())
                                    .num(lowVisionNum)
                                    .ratio(convertToRatio(lowVisionNum * 1f / actualScreeningNum))
                                    .items(new ArrayList() {
                                        {
                                            add(male);
                                            add(female);
                                        }
                                    })
                                    .build());
                }
            }
        };

        List<ClassStat> tabSchoolAge = new ArrayList<ClassStat>() {
            {
                for (StatClassLabel label : StatClassLabel.values()) {
                    add(ClassStat.builder()
                                    .title(label.name())
                                    .num(lowVisionNum)
                                    .ratio(convertToRatio(lowVisionNum * 1f / actualScreeningNum))
                                    .items(new ArrayList() {
                                        {
                                            for (SchoolAge age : SchoolAge.values()) {
                                                if (age.equals(SchoolAge.UNIVERSITY)) {
                                                    continue;
                                                }
                                                add(new BasicStatParams(age.desc,
                                                        convertToRatio(
                                                                maleNum * 1f / actualScreeningNum),
                                                        maleNum));
                                            }
                                        }
                                    })
                                    .build());
                }
            }
        };

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
                                          .tabGender(tabGender)
                                          .tabSchoolAge(tabSchoolAge)
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
