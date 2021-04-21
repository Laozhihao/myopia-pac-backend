package com.wupol.myopia.business.api.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.core.government.domain.model.District;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.constant.SchoolAge;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictAttentiveObjectsStatisticService;
import com.wupol.myopia.business.core.stat.service.DistrictMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.DistrictVisionStatisticService;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.StatClassLabel;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningDataContrastVo;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatService {
    private final static int WEARING_GLASSES_RESCREEN_INDEX_NUM = 6;
    private final static int WITHOUT_GLASSES_RESCREEN_INDEX_NUM = 4;

    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private GovDeptService govDeptService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    ExcelFacade excelFacade;

    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;
    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;

    @Value("classpath:excel/ExportStatContrastTemplate.xlsx")
    private Resource exportStatContrastTemplate;

    /**
     * 预警信息
     * @return
     * @throws IOException
     */
    public WarningInfo getWarningList() throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> districtIds = this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQuery lastOneQuery = new StatConclusionQuery();
        lastOneQuery.setDistrictIds(districtIds);
        lastOneQuery.setIsValid(true);
        lastOneQuery.setIsRescreen(false);
        StatConclusion lastConclusion = statConclusionService.getLastOne(lastOneQuery);
        if (lastConclusion == null) {
            return WarningInfo.builder().build();
        }
        ZoneId zoneId = ZoneId.of("UTC+8");
        LocalDate endDate = convertToLocalDate(lastConclusion.getCreateTime(), zoneId).plusDays(1);
        LocalDate startDate = endDate.plusYears(-1);
        StatConclusionQuery warningListQuery = new StatConclusionQuery();
        warningListQuery.setStartTime(startDate)
                .setEndTime(endDate)
                .setDistrictIds(districtIds)
                .setIsValid(true)
                .setIsRescreen(false);
        List<StatConclusion> warningConclusions =
                statConclusionService.listByQuery(warningListQuery);
        long total = warningConclusions.size();
        long warning0Num = warningConclusions.stream()
                                   .filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel()))
                                   .count();
        long warning1Num = warningConclusions.stream()
                                   .filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel()))
                                   .count();
        long warning2Num = warningConclusions.stream()
                                   .filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel()))
                                   .count();
        long warning3Num = warningConclusions.stream()
                                   .filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel()))
                                   .count();
        long focusTargetsNum = warning0Num + warning1Num + warning2Num + warning3Num;
        return WarningInfo.builder()
                .statTime(startDate.atStartOfDay(zoneId).toInstant().toEpochMilli())
                .endTime(endDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1)
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(convertToPercentage(focusTargetsNum * 1f / total))
                .warningLevelInfoList(new ArrayList<WarningLevelInfo>() {
                    {
                        add(new WarningLevelInfo(
                                0, warning0Num, convertToPercentage(warning0Num * 1f / total)));
                        add(new WarningLevelInfo(
                                1, warning1Num, convertToPercentage(warning1Num * 1f / total)));
                        add(new WarningLevelInfo(
                                2, warning2Num, convertToPercentage(warning2Num * 1f / total)));
                        add(new WarningLevelInfo(
                                3, warning3Num, convertToPercentage(warning3Num * 1f / total)));
                    }
                })
                .build();
    }

    /**
     * 对比统计数据
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @param districtId 区域ID
     * @param schoolAge 学龄段
     * @return
     * @throws IOException
     */
    public Map<String, ScreeningDataContrast> getScreeningDataContrast(Integer notificationId1,
            Integer notificationId2, Integer districtId, Integer schoolAge) throws IOException {
        if (notificationId1 == null || notificationId1 < 0) {
            return null;
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        StatConclusionQuery query = composeDistrictQuery(districtId, currentUser);
        query.setSrcScreeningNoticeId(notificationId1);
        if (schoolAge != null && schoolAge > 0) {
            query.setSchoolAge(schoolAge);
        }

        List<StatConclusion> resultConclusion1 = statConclusionService.listByQuery(query);
        List<Integer> validDistrictIds1 =
                this.getValidDistrictIdsByNotificationId(notificationId1, currentUser);
        int planScreeningNum1 = this.getPlanScreeningStudentNum(notificationId1, validDistrictIds1);
        ScreeningDataContrast data1 =
                composeScreeningDataContrast(resultConclusion1, planScreeningNum1);

        Map<String, ScreeningDataContrast> result = new HashMap<String, ScreeningDataContrast>();
        result.put("result1", data1);
        if (notificationId2 != null && notificationId2 >= 0) {
            query.setSrcScreeningNoticeId(notificationId2);
            List<StatConclusion> resultConclusion2 = statConclusionService.listByQuery(query);
            List<Integer> validDistrictIds2 =
                    this.getValidDistrictIdsByNotificationId(notificationId2, currentUser);
            int planScreeningNum2 =
                    this.getPlanScreeningStudentNum(notificationId2, validDistrictIds2);
            result.put(
                    "result2", composeScreeningDataContrast(resultConclusion2, planScreeningNum2));
        }
        return result;
    }

    /**
     * 获取用户有效的区域Id列表
     * @param notificationId 筛查通知ID
     * @param currentUser 当前用户
     * @return
     * @throws IOException
     */
    public List<Integer> getValidDistrictIdsByNotificationId(
            int notificationId, CurrentUser currentUser) throws IOException {
        List<ScreeningPlan> screeningPlans =
                screeningPlanService.getScreeningPlanByNoticeIdAndUser(notificationId, currentUser);
        Set<Integer> districtIds = schoolService.getAllSchoolDistrictIdsByScreeningPlanIds(
                screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        if (currentUser.isPlatformAdminUser()) {
            return districtIds.stream().collect(Collectors.toList());
        }
        List<Integer> validDistrictIds = new ArrayList<>();
        List<District> validDistricts =
                districtService.getValidDistrictTree(currentUser, districtIds);
        districtService.getAllIds(validDistrictIds, validDistricts);
        return validDistrictIds;
    }

    /**
     * 获取用户对应权限的可对比区域ID
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @return
     * @throws IOException
     */
    public List<District> getDataContrastDistrictTree(
            Integer notificationId1, Integer notificationId2) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<ScreeningPlan> screeningPlans1 =
                screeningPlanService.getScreeningPlanByNoticeIdAndUser(
                        notificationId1, currentUser);
        Set<Integer> districtIds1 = schoolService.getAllSchoolDistrictIdsByScreeningPlanIds(
                screeningPlans1.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));

        if (notificationId2 != null) {
            List<ScreeningPlan> screeningPlans2 =
                    screeningPlanService.getScreeningPlanByNoticeIdAndUser(
                            notificationId1, currentUser);
            Set<Integer> districtIds2 = schoolService.getAllSchoolDistrictIdsByScreeningPlanIds(
                    screeningPlans2.stream()
                            .map(ScreeningPlan::getId)
                            .collect(Collectors.toList()));
            districtIds1.retainAll(districtIds2);
        }
        return districtService.getValidDistrictTree(currentUser, districtIds1);
    }

    /**
     * 分类统计
     * @param notificationId 通知ID
     * @return
     * @throws IOException
     */
    public ScreeningClassStat getScreeningClassStat(Integer notificationId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> validDistrictIds =
                this.getValidDistrictIdsByNotificationId(notificationId, currentUser);

        StatConclusionQuery query = new StatConclusionQuery();
        query.setDistrictIds(validDistrictIds);
        query.setSrcScreeningNoticeId(notificationId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return null;
        }

        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream()
                        .filter(x -> Boolean.FALSE.equals(x.getIsRescreen()))
                        .collect(Collectors.toList());

        List<StatConclusion> validConclusions =
                firstScreenConclusions.stream()
                        .filter(x -> Boolean.TRUE.equals(x.getIsValid()))
                        .collect(Collectors.toList());

        List<StatConclusion> lowVisionConclusions =
                validConclusions.stream()
                        .filter(x -> Boolean.TRUE.equals(x.getIsLowVision()))
                        .collect(Collectors.toList());

        List<StatConclusion> refractiveErrorConclusions =
                validConclusions.stream()
                        .filter(x -> Boolean.TRUE.equals(x.getIsRefractiveError()))
                        .collect(Collectors.toList());

        List<StatConclusion> wearingGlassesConclusions =
                validConclusions.stream()
                        .filter(x -> x.getGlassesType() > 0)
                        .collect(Collectors.toList());

        List<StatConclusion> myopiaConclusions =
                validConclusions.stream().filter(x -> x.getIsMyopia()).collect(Collectors.toList());

        long totalFirstScreeningNum = firstScreenConclusions.size();
        long validFirstScreeningNum = validConclusions.size();

        List<ClassStat> tabGender = new ArrayList<ClassStat>() {
            {
                add(composeGenderClassStat(
                        StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
                add(composeGenderClassStat(StatClassLabel.REFRACTIVE_ERROR, validFirstScreeningNum,
                        refractiveErrorConclusions));
                add(composeGenderClassStat(
                        StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
                add(composeGenderClassStat(StatClassLabel.WEARING_GLASSES, validFirstScreeningNum,
                        wearingGlassesConclusions));
            }
        };

        List<ClassStat> tabSchoolAge = new ArrayList<ClassStat>() {
            {
                add(composeSchoolAgeClassStat(
                        StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.REFRACTIVE_ERROR,
                        validFirstScreeningNum, refractiveErrorConclusions));
                add(composeSchoolAgeClassStat(
                        StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.WEARING_GLASSES,
                        validFirstScreeningNum, wearingGlassesConclusions));
            }
        };

        List<StatConclusion> rescreenConclusions =
                statConclusions.stream()
                        .filter(x
                                -> Boolean.TRUE.equals(x.getIsRescreen())
                                        && Boolean.TRUE.equals(x.getIsValid()))
                        .collect(Collectors.toList());

        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        AverageVision averageVision = this.calculateAverageVision(validConclusions);

        int planScreeningNum = getPlanScreeningStudentNum(notificationId, validDistrictIds);
        return ScreeningClassStat.builder()
                .notificationId(notificationId)
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .screeningFinishedRatio(
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum))
                .averageVisionLeft(averageVision.getAverageVisionLeft())
                .averageVisionRight(averageVision.getAverageVisionRight())
                .tabGender(tabGender)
                .tabSchoolAge(tabSchoolAge)
                .rescreenStat(rescreenStat)
                .build();
    }

    /**
     * 获取区域学生计划筛查数量
     * @param notificationId 通知ID
     * @param validDistrictIds 筛选区域ID
     * @return
     * @throws IOException
     */
    private Integer getPlanScreeningStudentNum(int notificationId, List<Integer> validDistrictIds)
            throws IOException {
        Map<Integer, Long> planDistrictStudentMap =
                screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(
                        notificationId);
        int planStudentNum = 0;
        for (Integer districtId : planDistrictStudentMap.keySet()) {
            if (!validDistrictIds.contains(districtId)) {
                continue;
            }
            planStudentNum += planDistrictStudentMap.get(districtId);
        }
        return planStudentNum;
    }

    /**
     * 导出统计对比数据
     * @param notificationId1 通知1 ID
     * @param notificationId2 通知2 ID
     * @param districtId 区域ID
     * @param schoolAge 学龄
     * @throws IOException
     * @throws UtilException
     */
    public void exportStatContrast(Integer notificationId1, Integer notificationId2,
            Integer districtId, Integer schoolAge) throws IOException, UtilException {
        Map<String, ScreeningDataContrast> contrastResultMap =
                getScreeningDataContrast(notificationId1, notificationId2, districtId, schoolAge);
        ScreeningDataContrast result1 = contrastResultMap.get("result1");
        ScreeningDataContrast result2 = contrastResultMap.get("result2");
        List<ScreeningDataContrastVo> exportList = new ArrayList<ScreeningDataContrastVo>() {
            {
                if (result1 != null) add(composeScreeningDataContrastVo("对比项1", result1));
                if (result2 != null) add(composeScreeningDataContrastVo("对比项2", result2));
            };
        };
        excelFacade.exportStatContrast(CurrentUserUtil.getCurrentUser().getId(), exportList,
                exportStatContrastTemplate.getInputStream());
    }

    /**
     * 构建区域及其下级所有符合当前用户范围的区域的搜索条件
     * @param districtId 区域 id
     * @param currentUser 当前用户
     * @return
     * @throws IOException
     */
    private StatConclusionQuery composeDistrictQuery(Integer districtId, CurrentUser currentUser)
            throws IOException {
        StatConclusionQuery query = new StatConclusionQuery();
        List<Integer> userDistrictIds = getCurrentUserDistrictIds(currentUser);
        if (districtId != null && districtId >= 0) {
            List<Integer> selectDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            if (userDistrictIds != null) {
                selectDistrictIds.retainAll(userDistrictIds);
            }
            query.setDistrictIds(selectDistrictIds);
        } else {
            query.setDistrictIds(userDistrictIds);
        }
        return query;
    }

    /**
     * 构造用于文件导出的对比筛查数据
     * @param title 标题
     * @param contrast 对比筛查数据
     * @return
     */
    private ScreeningDataContrastVo composeScreeningDataContrastVo(
            String title, ScreeningDataContrast contrast) {
        if (contrast == null) {
            return null;
        }
        RescreenStat rs = contrast.getRescreenStat();
        return ScreeningDataContrastVo.builder()
                .title(title)
                .screeningNum(contrast.getScreeningNum())
                .actualScreeningNum(contrast.getActualScreeningNum())
                .validScreeningNum(contrast.getValidScreeningNum())
                .averageVisionLeft(contrast.getAverageVisionLeft())
                .averageVisionRight(contrast.getAverageVisionRight())
                .lowVisionRatio(contrast.getLowVisionRatio() + "%")
                .refractiveErrorRatio(contrast.getRefractiveErrorRatio() + "%")
                .wearingGlassesRatio(contrast.getWearingGlassesRatio() + "%")
                .myopiaNum(contrast.getMyopiaNum())
                .myopiaRatio(contrast.getMyopiaRatio() + "%")
                .focusTargetsNum(contrast.getFocusTargetsNum())
                .warningLevelZeroRatio(contrast.getWarningLevelZeroRatio() + "%")
                .warningLevelOneRatio(contrast.getWarningLevelOneRatio() + "%")
                .warningLevelTwoRatio(contrast.getWarningLevelTwoRatio() + "%")
                .warningLevelThreeRatio(contrast.getWarningLevelThreeRatio() + "%")
                .recommendVisitNum(contrast.getRecommendVisitNum())
                .screeningFinishedRatio(contrast.getScreeningFinishedRatio() + "%")
                .rescreenNum(rs.getRescreenNum())
                .wearingGlassesRescreenNum(rs.getWearingGlassesRescreenNum())
                .wearingGlassesRescreenIndexNum(rs.getWearingGlassesRescreenIndexNum())
                .withoutGlassesRescreenNum(rs.getWithoutGlassesRescreenNum())
                .withoutGlassesRescreenIndexNum(rs.getWithoutGlassesRescreenIndexNum())
                .rescreenItemNum(rs.getRescreenItemNum())
                .incorrectItemNum(rs.getIncorrectItemNum())
                .incorrectRatio(rs.getIncorrectRatio() + "%")
                .build();
    }

    /**
     * 获取当前用户所有权限的区域ID
     * @param currentUser 当前用户
     * @return
     * @throws IOException
     */
    private List<Integer> getCurrentUserDistrictIds(CurrentUser currentUser) throws IOException {
        if (currentUser.isPlatformAdminUser() || currentUser.getOrgId() == null) {
            return null;
        }
        GovDept govDept = govDeptService.getById(currentUser.getOrgId());
        District userDistrict = districtService.getById(govDept.getDistrictId());
        List<Integer> districtIds =
                districtService.getSpecificDistrictTreeAllDistrictIds(userDistrict.getId());
        return districtIds;
    }

    /**
     * 按性别统计数据
     * @param label 统计标签
     * @param validScreeningNum 有效筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeGenderClassStat(
            StatClassLabel label, long validScreeningNum, List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long maleNum = statConclusions.stream()
                               .filter(x -> GenderEnum.MALE.type.equals(x.getGender()))
                               .count();
        long femaleNum = statConclusions.size() - maleNum;
        return ClassStat.builder()
                .title(label.name())
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(composeBasicParams(GenderEnum.MALE.name(), maleNum, statNum));
                        add(composeBasicParams(GenderEnum.FEMALE.name(), femaleNum, statNum));
                    }
                })
                .build();
    }

    /**
     * 按学龄统计数据
     * @param label 统计标签
     * @param validScreeningNum 实际有效筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeSchoolAgeClassStat(
            StatClassLabel label, long validScreeningNum, List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long kindergartenNum =
                statConclusions.stream()
                        .filter(x -> SchoolAge.KINDERGARTEN.code.equals(x.getSchoolAge()))
                        .count();
        long primaryNum = statConclusions.stream()
                                  .filter(x -> SchoolAge.PRIMARY.code.equals(x.getSchoolAge()))
                                  .count();
        long juniorNum = statConclusions.stream()
                                 .filter(x -> SchoolAge.JUNIOR.code.equals(x.getSchoolAge()))
                                 .count();
        long highNum = statConclusions.stream()
                               .filter(x -> SchoolAge.HIGH.code.equals(x.getSchoolAge()))
                               .count();
        long vocationalHighNum =
                statConclusions.stream()
                        .filter(x -> SchoolAge.VOCATIONAL_HIGH.code.equals(x.getSchoolAge()))
                        .count();

        return ClassStat.builder()
                .title(label.name())
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(composeBasicParams(
                                SchoolAge.KINDERGARTEN.name(), kindergartenNum, statNum));
                        add(composeBasicParams(SchoolAge.PRIMARY.name(), primaryNum, statNum));
                        add(composeBasicParams(SchoolAge.JUNIOR.name(), juniorNum, statNum));
                        add(composeBasicParams(SchoolAge.HIGH.name(), highNum, statNum));
                        add(composeBasicParams(
                                SchoolAge.VOCATIONAL_HIGH.name(), vocationalHighNum, statNum));
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
                rescreenConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long wearingGlassesIndexNum = wearingGlassesNum * WEARING_GLASSES_RESCREEN_INDEX_NUM;
        long withoutGlassesNum = totalScreeningNum - wearingGlassesNum;
        long withoutGlassesIndexNum = withoutGlassesNum * WITHOUT_GLASSES_RESCREEN_INDEX_NUM;
        long errorIndexNum =
                rescreenConclusions.stream().mapToLong(x -> x.getRescreenErrorNum()).sum();
        return RescreenStat.builder()
                .rescreenNum(rescreenConclusions.size())
                .wearingGlassesRescreenIndexNum(WEARING_GLASSES_RESCREEN_INDEX_NUM)
                .withoutGlassesRescreenIndexNum(WITHOUT_GLASSES_RESCREEN_INDEX_NUM)
                .wearingGlassesRescreenNum(wearingGlassesNum)
                .withoutGlassesRescreenNum(withoutGlassesNum)
                .rescreenItemNum(wearingGlassesIndexNum + withoutGlassesIndexNum)
                .incorrectItemNum(errorIndexNum)
                .incorrectRatio(convertToPercentage(
                        errorIndexNum * 1f / (wearingGlassesIndexNum + withoutGlassesIndexNum)))
                .build();
    }

    /**
     * 构造对比数据
     * @param resultConclusion 结论数据列表
     * @param planScreeningNum 计划筛查学生数
     * @return
     */
    private ScreeningDataContrast composeScreeningDataContrast(
            List<StatConclusion> resultConclusion, int planScreeningNum) {
        List<StatConclusion> firstScreeningConclusions =
                resultConclusion.stream()
                        .filter(x -> Boolean.FALSE.equals(x.getIsRescreen()))
                        .collect(Collectors.toList());
        List<StatConclusion> validConclusions = firstScreeningConclusions.stream()
                                                        .filter(x -> x.getIsValid())
                                                        .collect(Collectors.toList());
        long lowVisionNum = validConclusions.stream().filter(x -> x.getIsLowVision()).count();
        long refractiveErrorNum =
                validConclusions.stream().filter(x -> x.getIsRefractiveError()).count();
        long wearingGlassesNum =
                validConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long myopiaNum = validConclusions.stream().filter(x -> x.getIsMyopia()).count();
        long totalFirstScreeningNum = firstScreeningConclusions.size();
        long validFirstScreeningNum = validConclusions.size();
        long recommendVisitNum =
                validConclusions.stream().filter(x -> x.getIsRecommendVisit()).count();
        long warning0Num = validConclusions.stream()
                                   .filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel()))
                                   .count();
        long warning1Num = validConclusions.stream()
                                   .filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel()))
                                   .count();
        long warning2Num = validConclusions.stream()
                                   .filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel()))
                                   .count();
        long warning3Num = validConclusions.stream()
                                   .filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel()))
                                   .count();

        List<StatConclusion> rescreenConclusions =
                resultConclusion.stream()
                        .filter(x -> x.getIsRescreen() && x.getIsValid())
                        .collect(Collectors.toList());
        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        return ScreeningDataContrast.builder()
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .averageVisionLeft(averageVision.getAverageVisionLeft())
                .averageVisionRight(averageVision.getAverageVisionRight())
                .lowVisionRatio(convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum))
                .refractiveErrorRatio(
                        convertToPercentage(refractiveErrorNum * 1f / validFirstScreeningNum))
                .wearingGlassesRatio(
                        convertToPercentage(wearingGlassesNum * 1f / validFirstScreeningNum))
                .myopiaNum(myopiaNum)
                .myopiaRatio(convertToPercentage(myopiaNum * 1f / validFirstScreeningNum))
                .focusTargetsNum(warning0Num + warning1Num + warning2Num + warning3Num)
                .warningLevelZeroRatio(
                        convertToPercentage(warning0Num * 1f / validFirstScreeningNum))
                .warningLevelOneRatio(
                        convertToPercentage(warning1Num * 1f / validFirstScreeningNum))
                .warningLevelTwoRatio(
                        convertToPercentage(warning2Num * 1f / validFirstScreeningNum))
                .warningLevelThreeRatio(
                        convertToPercentage(warning3Num * 1f / validFirstScreeningNum))
                .recommendVisitNum(recommendVisitNum)
                .screeningFinishedRatio(
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum))
                .rescreenStat(rescreenStat)
                .build();
    }

    /**
     * 转换为百分比并保留2位小数
     * @param num
     * @return
     */
    private Float convertToPercentage(Float num) {
        return Math.round(num * 10000) / 100f;
    }

    /**
     * 保留2位小数
     * @param num
     * @return
     */
    private Float round2Digits(Double num) {
        return Math.round(num * 100) / 100f;
    }

    /**
     * 构造分类统计数据
     * @param name 分类名称
     * @param statNum 分类统计数量
     * @param totalStatNum 统计总量
     * @return
     */
    private BasicStatParams composeBasicParams(String name, long statNum, long totalStatNum) {
        return new BasicStatParams(name, convertToPercentage(statNum * 1f / totalStatNum), statNum);
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
        float avgVisionL = round2Digits(sumVisionL / size);
        float avgVisionR = round2Digits(sumVisionR / size);
        return AverageVision.builder()
                .averageVisionLeft(avgVisionL)
                .averageVisionRight(avgVisionR)
                .build();
    }

    /**
     * Date to LocalDate
     * @param date 日期
     * @param zoneId 时区ID
     * @return
     */
    private LocalDate convertToLocalDate(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(zoneId);
        return zdt.toLocalDate();
    }

    /** 平均视力 */
    @Data
    @Builder
    public static class AverageVision {
        private float averageVisionLeft;
        private float averageVisionRight;
    }

    /**
     * 获取重点视力对象
     * @param districtId
     * @param districts
     * @param districtIds
     * @return
     */
    public FocusObjectsStatisticVO getFocusObjectsStatisticVO(
            Integer districtId, List<District> districts, Set<Integer> districtIds) {
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics =
                districtAttentiveObjectsStatisticService.getStatisticDtoByDistrictIdsAndTaskId(districtIds,true);
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics)) {
            return FocusObjectsStatisticVO.getImmutableEmptyInstance();
        }
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Map<Integer, String> districtIdNameMap = districts.stream().collect(
                Collectors.toMap(District::getId, District::getName, (v1, v2) -> v2));
        districtIdNameMap.put(districtId, currentRangeName);

        List<DistrictAttentiveObjectsStatistic> currentAttentiveObjectsStatistics =
                districtAttentiveObjectsStatisticService.getStatisticDtoByCurrentDistrictIdAndTaskId(districtId,false);
        DistrictAttentiveObjectsStatistic  currentDistrictAttentiveObjectsStatistic = null;
        if (CollectionUtils.isNotEmpty(currentAttentiveObjectsStatistics)) {
            currentDistrictAttentiveObjectsStatistic = currentAttentiveObjectsStatistics.stream().findFirst().get();
        }
        //获取数据
        return FocusObjectsStatisticVO.getInstance(districtAttentiveObjectsStatistics, districtId,
                currentRangeName, districtIdNameMap,currentDistrictAttentiveObjectsStatistic);
    }

    /**
     * 获取筛查视力对象
     * @param districtId
     * @param noticeId
     * @param screeningNotice
     * @return
     * @throws IOException
     */
    public ScreeningVisionStatisticVO getScreeningVisionStatisticVO(Integer districtId,
            Integer noticeId, ScreeningNotice screeningNotice) throws IOException {
        //查找合计数据（当前层级 + 下级）
        List<DistrictVisionStatistic> districtVisionStatistics =
                districtVisionStatisticService.getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(
                        noticeId, districtId, CurrentUserUtil.getCurrentUser(),true);
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return ScreeningVisionStatisticVO.getImmutableEmptyInstance();
        }
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Set<Integer> districtIds = districtVisionStatistics.stream()
                                           .map(DistrictVisionStatistic::getDistrictId)
                                           .collect(Collectors.toSet());
        List<District> districts = districtService.getDistrictByIds(new ArrayList<>(districtIds));
        Map<Integer, String> districtIdNameMap =
                districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);

        //查找当前层级的数据（非合计数据）
        List<DistrictVisionStatistic> currentDistrictVisionStatistics =
                districtVisionStatisticService.getStatisticDtoByNoticeIdAndCurrentDistrictId(
                        noticeId, districtId, CurrentUserUtil.getCurrentUser(),false);
        DistrictVisionStatistic currentDistrictVisionStatistic = null;
        if (CollectionUtils.isNotEmpty(currentDistrictVisionStatistics)) {
            currentDistrictVisionStatistic = currentDistrictVisionStatistics.stream().findFirst().get();
        }
        //获取数据
        return ScreeningVisionStatisticVO.getInstance(districtVisionStatistics, districtId,
                currentRangeName, screeningNotice, districtIdNameMap,currentDistrictVisionStatistic);
    }

    /**
     * 获取地区监控情况
     * @param districtId
     * @param noticeId
     * @param screeningNotice
     * @return
     * @throws IOException
     */
    public DistrictScreeningMonitorStatisticVO getDistrictScreeningMonitorStatisticVO(
            Integer districtId, Integer noticeId, ScreeningNotice screeningNotice)
            throws IOException {
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictMonitorStatistic> districtMonitorStatistics =
                districtMonitorStatisticService.getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(
                        noticeId, districtId, CurrentUserUtil.getCurrentUser(),true);
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return DistrictScreeningMonitorStatisticVO.getImmutableEmptyInstance();
        }
        //获取task详情
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Set<Integer> districtIds = districtMonitorStatistics.stream()
                                           .map(DistrictMonitorStatistic::getDistrictId)
                                           .collect(Collectors.toSet());
        List<District> districts = districtService.getDistrictByIds(new ArrayList<>(districtIds));
        Map<Integer, String> districtIdNameMap =
                districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);

        //查找当前层级的数据（非合计数据）
        List<DistrictMonitorStatistic> currentDistrictMonitorStatistics =
                districtMonitorStatisticService.getStatisticDtoByNoticeIdAndCurrentDistrictId(
                        noticeId, districtId, CurrentUserUtil.getCurrentUser(),false);
        DistrictMonitorStatistic  currentDistrictMonitorStatistic = null;
        if (CollectionUtils.isNotEmpty(currentDistrictMonitorStatistics)) {
            currentDistrictMonitorStatistic = currentDistrictMonitorStatistics.stream().findFirst().get();
        }
        //获取数据
        return DistrictScreeningMonitorStatisticVO.getInstance(districtMonitorStatistics,
                districtId, currentRangeName, screeningNotice, districtIdNameMap,currentDistrictMonitorStatistic);
    }
}
