package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.GenderEnum;
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
import com.wupol.myopia.business.management.domain.model.StatConclusion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
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
        // List<Integer> districtIds = this.getCurrentUserDistrictIds();
        List<Integer> districtIds = null;
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
     * @deprecated
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
        List<Integer> districtIds = this.getCurrentUserDistrictIds();
        List<StatConclusion> statConclusions =
                statConclusionService.listByNoticeId(notificationId, districtIds);

        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream()
                        .filter(x -> x.getIsRescreen() == false)
                        .collect(Collectors.toList());

        List<StatConclusion> lowVisionConclusions = firstScreenConclusions.stream()
                                                            .filter(x -> x.getIsLowVision() == true)
                                                            .collect(Collectors.toList());

        List<StatConclusion> refractiveErrorConclusions =
                firstScreenConclusions.stream()
                        .filter(x -> x.getIsRefractiveError() == true)
                        .collect(Collectors.toList());

        List<StatConclusion> wearingGlassesConclusions =
                firstScreenConclusions.stream()
                        .filter(x -> x.getIsWearingGlasses() == true)
                        .collect(Collectors.toList());

        List<StatConclusion> myopiaConclusions = firstScreenConclusions.stream()
                                                         .filter(x -> x.getIsMyopia() == true)
                                                         .collect(Collectors.toList());

        int totalFirstScreeningNum = firstScreenConclusions.size();
        List<ClassStat> tabGender = new ArrayList<ClassStat>() {
            {
                add(composeGenderClassStat(
                        StatClassLabel.LOW_VISION, totalFirstScreeningNum, lowVisionConclusions));
                add(composeGenderClassStat(StatClassLabel.REFRACTIVE_ERROR, totalFirstScreeningNum,
                        refractiveErrorConclusions));
                add(composeGenderClassStat(StatClassLabel.WEARING_GLASSES, totalFirstScreeningNum,
                        wearingGlassesConclusions));
                add(composeGenderClassStat(
                        StatClassLabel.MYOPIA, totalFirstScreeningNum, myopiaConclusions));
            }
        };

        List<ClassStat> tabSchoolAge = new ArrayList<ClassStat>() {
            {
                add(composeSchoolAgeClassStat(
                        StatClassLabel.LOW_VISION, totalFirstScreeningNum, lowVisionConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.REFRACTIVE_ERROR,
                        totalFirstScreeningNum, refractiveErrorConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.WEARING_GLASSES,
                        totalFirstScreeningNum, wearingGlassesConclusions));
                add(composeSchoolAgeClassStat(
                        StatClassLabel.MYOPIA, totalFirstScreeningNum, myopiaConclusions));
            }
        };

        List<StatConclusion> rescreenConclusions = statConclusions.stream()
                                                           .filter(x -> x.getIsRescreen() == true)
                                                           .collect(Collectors.toList());
        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        AverageVision averageVision = this.calculateAverageVision(firstScreenConclusions);

        return ScreeningClassStat.builder()
                .notificationId(notificationId)
                .screeningNum(totalFirstScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(totalFirstScreeningNum - 1000)
                // .screeningFinishedRatio(convertToRatio(
                // totalFirstScreeningNum * 1f / screeningNum))
                .averageVisionLeft(averageVision.averageVisionLeft)
                .averageVisionRight(averageVision.getAverageVisionRight())
                .tabGender(tabGender)
                .tabSchoolAge(tabSchoolAge)
                .rescreenStat(rescreenStat)
                .build();
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
     * 保留2位小数
     * @param num
     * @return
     */
    private Float roundDigits(Double num) {
        return Math.round(num * 100) / 100f;
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

    /**
     * 获取当前用户所有权限的区域ID
     * @return
     */
    private List<Integer> getCurrentUserDistrictIds() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        GovDept govDept = govDeptService.getById(currentUser.getOrgId());
        District userDistrict = districtService.getById(govDept.getDistrictId());
        List<District> districts;
        try {
            districts =
                    districtService.getChildDistrictByParentIdPriorityCache(userDistrict.getId());
        } catch (IOException e) {
            //  TODO: add log
            return null;
        }
        districts.add(userDistrict);
        return districts.stream().map(District::getId).collect(Collectors.toList());
    }

    /**
     * 按性别统计数据
     * @param label 统计标签
     * @param totalFirstScreeningNum 实际筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeGenderClassStat(StatClassLabel label, long totalFirstScreeningNum,
            List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long statMaleNum =
                statConclusions.stream().filter(x -> x.getGender() == GenderEnum.MALE.type).count();
        long statFemaleNum = statConclusions.size() - statMaleNum;
        BasicStatParams statMaleStatParams = new BasicStatParams(
                GenderEnum.MALE.name, convertToRatio(statMaleNum * 1f / statNum), statMaleNum);
        BasicStatParams statFemaleStatParams = new BasicStatParams(
                GenderEnum.FEMALE.name, convertToRatio(statFemaleNum * 1f / statNum), statMaleNum);
        return ClassStat.builder()
                .title(label.desc)
                .num(statNum)
                .ratio(convertToRatio(statNum * 1f / totalFirstScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(statMaleStatParams);
                        add(statFemaleStatParams);
                    }
                })
                .build();
    }

    /**
     * 按学龄统计数据
     * @param label 统计标签
     * @param totalFirstScreeningNum 实际筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeSchoolAgeClassStat(StatClassLabel label, int totalFirstScreeningNum,
            List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long statMaleNum =
                statConclusions.stream().filter(x -> x.getGender() == GenderEnum.MALE.type).count();
        long statFemaleNum = statConclusions.size() - statMaleNum;
        BasicStatParams statMaleStatParams = new BasicStatParams(
                GenderEnum.MALE.name, convertToRatio(statMaleNum * 1f / statNum), statMaleNum);
        BasicStatParams statFemaleStatParams = new BasicStatParams(
                GenderEnum.FEMALE.name, convertToRatio(statFemaleNum * 1f / statNum), statMaleNum);
        return ClassStat.builder()
                .title(label.desc)
                .num(statNum)
                .ratio(convertToRatio(statNum * 1f / totalFirstScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(statMaleStatParams);
                        add(statFemaleStatParams);
                    }
                })
                .build();
    }

    /**
     * 获取复查统计汇总
     * @param rescreenConclusions 复查统计记录
     * @return
     */
    private RescreenStat composeRescreenConclusion(List<StatConclusion> rescreenConclusions) {
        long totalScreeningNum = rescreenConclusions.size();
        long wearingGlassesNum =
                rescreenConclusions.stream().filter(x -> x.getIsWearingGlasses() == true).count();
        long wearingGlassesIndexNum = wearingGlassesNum * 6;
        long withoutGlassesNum = totalScreeningNum - wearingGlassesNum;
        long withoutGlassesIndexNum = withoutGlassesNum * 4;
        long errorIndexNum =
                rescreenConclusions.stream().mapToLong(x -> x.getRescreenErrorNum()).sum();
        return RescreenStat.builder()
                .rescreenNum(rescreenConclusions.size())
                .wearingGlassesRescreenNum(wearingGlassesNum)
                .wearingGlassesRescreenIndexNum(wearingGlassesIndexNum)
                .withoutGlassesRescreenNum(withoutGlassesNum)
                .withoutGlassesRescreenIndexNum(withoutGlassesIndexNum)
                .rescreenItemNum(wearingGlassesIndexNum + withoutGlassesIndexNum)
                .incorrectItemNum(errorIndexNum)
                .incorrectRatio(convertToRatio(
                        errorIndexNum * 1f / (wearingGlassesIndexNum + withoutGlassesIndexNum)))
                .build();
    }

    /**
     * 计算平均筛查视力
     * @param statConclusions
     * @return
     */
    private AverageVision calculateAverageVision(List<StatConclusion> statConclusions) {
        int size = statConclusions.size();
        double sumVisionL = statConclusions.stream().mapToDouble(x -> x.getVisionL()).sum();
        double sumVisionR = statConclusions.stream().mapToDouble(x -> x.getVisionR()).sum();
        float avgVisionL = roundDigits(sumVisionL / size);
        float avgVisionR = roundDigits(sumVisionR / size);
        return AverageVision.builder()
                .averageVisionLeft(avgVisionL)
                .averageVisionRight(avgVisionR)
                .build();
    }
    @Data
    @Builder
    public static class AverageVision {
        private float averageVisionLeft;
        private float averageVisionRight;
    }
}
