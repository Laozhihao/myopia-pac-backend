package com.wupol.myopia.business.api.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.domain.dto.ContrastTypeYearItemsDTO;
import com.wupol.myopia.business.api.management.domain.dto.DataContrastFilterDTO;
import com.wupol.myopia.business.api.management.domain.dto.FilterParamsDTO;
import com.wupol.myopia.business.api.management.domain.vo.DistrictScreeningMonitorStatisticVO;
import com.wupol.myopia.business.api.management.domain.vo.FocusObjectsStatisticVO;
import com.wupol.myopia.business.api.management.domain.vo.RescreenReportVO;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningVisionStatisticVO;
import com.wupol.myopia.business.common.utils.constant.ContrastTypeEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.StatClassLabel;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictAttentiveObjectsStatisticService;
import com.wupol.myopia.business.core.stat.service.DistrictMonitorStatisticService;
import com.wupol.myopia.business.core.stat.service.DistrictVisionStatisticService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatService {
    private static final int WEARING_GLASSES_RESCREEN_INDEX_NUM = 6;
    private static final int WITHOUT_GLASSES_RESCREEN_INDEX_NUM = 4;

    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private DistrictBizService districtBizService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;
    @Autowired
    private SchoolBizService schoolBizService;
    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;
    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;
    @Autowired
    private StatRescreenService statRescreenService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;
    @Autowired
    private ScreeningTaskBizService screeningTaskBizService;

    @Value("classpath:excel/ExportStatContrastTemplate.xlsx")
    private Resource exportStatContrastTemplate;

    /**
     * 预警信息
     *
     * @return
     */
    public WarningInfo getWarningList() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> districtIds = this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQueryDTO lastOneQuery = new StatConclusionQueryDTO();
        lastOneQuery.setDistrictIds(districtIds);
        lastOneQuery.setIsValid(true);
        lastOneQuery.setIsRescreen(false);
        StatConclusion lastConclusion = statConclusionService.getLastOne(lastOneQuery);
        if (lastConclusion == null) {
            return WarningInfo.builder().build();
        }
        ZoneId zoneId = ZoneId.of("UTC+8");
        LocalDate endDate = DateUtil.convertToLocalDate(lastConclusion.getCreateTime(), zoneId).plusDays(1);
        LocalDate startDate = endDate.plusYears(-1);
        StatConclusionQueryDTO warningListQuery = new StatConclusionQueryDTO();
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
        ArrayList<WarningLevelInfo> warningLevelInfoArrayList = new ArrayList<>();
        warningLevelInfoArrayList.add(new WarningLevelInfo(0, warning0Num, convertToPercentage(warning0Num * 1f / total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(1, warning1Num, convertToPercentage(warning1Num * 1f / total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(2, warning2Num, convertToPercentage(warning2Num * 1f / total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(3, warning3Num, convertToPercentage(warning3Num * 1f / total)));
        return WarningInfo.builder()
                .statTime(startDate.atStartOfDay(zoneId).toInstant().toEpochMilli())
                .endTime(endDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1)
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(convertToPercentage(focusTargetsNum * 1f / total))
                .warningLevelInfoList(warningLevelInfoArrayList)
                .build();
    }

    /**
     * 对比统计数据
     *
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @param districtId      区域ID
     * @param schoolAge       学龄段
     * @deprecated
     */
    @Deprecated
    public Map<String, ScreeningDataContrast> getScreeningDataContrast(
            Integer notificationId1, Integer notificationId2, Integer districtId, Integer schoolAge) throws IOException {
        if (notificationId1 == null || notificationId1 < 0) {
            return null;
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        StatConclusionQueryDTO query = composeDistrictQuery(districtId, currentUser);
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

        Map<String, ScreeningDataContrast> result = new HashMap<>();
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
                managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(notificationId, currentUser);
        Set<Integer> districtIds = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(
                screeningPlans.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));
        if (currentUser.isPlatformAdminUser()) {
            return new ArrayList<>(districtIds);
        }
        List<Integer> validDistrictIds = new ArrayList<>();
        List<District> validDistricts =
                districtBizService.getValidDistrictTree(currentUser, districtIds);
        districtService.getAllIds(validDistrictIds, validDistricts);
        return validDistrictIds;
    }

    /**
     * 获取用户对应权限的可对比区域ID
     *
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @return
     * @throws IOException
     */
    public List<District> getDataContrastDistrictTree(
            Integer notificationId1, Integer notificationId2) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<ScreeningPlan> screeningPlans1 =
                managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(
                        notificationId1, currentUser);
        Set<Integer> districtIds1 = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(
                screeningPlans1.stream().map(ScreeningPlan::getId).collect(Collectors.toList()));

        if (notificationId2 != null) {
            List<ScreeningPlan> screeningPlans2 =
                    managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(
                            notificationId1, currentUser);
            Set<Integer> districtIds2 = schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(
                    screeningPlans2.stream()
                            .map(ScreeningPlan::getId)
                            .collect(Collectors.toList()));
            districtIds1.retainAll(districtIds2);
        }
        return districtBizService.getValidDistrictTree(currentUser, districtIds1);
    }

    /**
     * 分类统计
     * @param notificationId 通知ID
     * @return
     * @throws IOException
     */
    public ScreeningClassStat getScreeningClassStat(Integer notificationId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> validDistrictIds = this.getValidDistrictIdsByNotificationId(notificationId, currentUser);

        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(validDistrictIds);
        query.setSrcScreeningNoticeId(notificationId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return ScreeningClassStat.builder().build();
        }

        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream().filter(x -> Boolean.FALSE.equals(x.getIsRescreen())).collect(Collectors.toList());

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
                validConclusions.stream().filter(StatConclusion::getIsMyopia).collect(Collectors.toList());

        long totalFirstScreeningNum = firstScreenConclusions.size();
        long validFirstScreeningNum = validConclusions.size();

        List<ClassStat> tabGender = new ArrayList<>();
        tabGender.add(composeGenderClassStat(StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
        tabGender.add(composeGenderClassStat(StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
        tabGender.add(composeGenderClassStat(StatClassLabel.REFRACTIVE_ERROR, validFirstScreeningNum, refractiveErrorConclusions));
        tabGender.add(composeGenderClassStat(StatClassLabel.WEARING_GLASSES, validFirstScreeningNum, wearingGlassesConclusions));

        List<ClassStat> tabSchoolAge = new ArrayList<>();
        tabSchoolAge.add(composeSchoolAgeClassStat(StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
        tabSchoolAge.add(composeSchoolAgeClassStat(StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
        tabSchoolAge.add(composeSchoolAgeClassStat(StatClassLabel.REFRACTIVE_ERROR, validFirstScreeningNum, refractiveErrorConclusions));
        tabSchoolAge.add(composeSchoolAgeClassStat(StatClassLabel.WEARING_GLASSES, validFirstScreeningNum, wearingGlassesConclusions));

        List<StatConclusion> rescreenConclusions =
                statConclusions.stream()
                        .filter(x
                                -> Boolean.TRUE.equals(x.getIsRescreen())
                                && Boolean.TRUE.equals(x.getIsValid()))
                        .collect(Collectors.toList());

        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        int planScreeningNum = getPlanScreeningStudentNum(notificationId, validDistrictIds);

        return ScreeningClassStat.builder().notificationId(notificationId)
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .screeningFinishedRatio(planScreeningNum > 0 ?
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum) : 0)
                .averageVisionLeft(averageVision.getAverageVisionLeft())
                .averageVisionRight(averageVision.getAverageVisionRight())
                .tabGender(tabGender)
                .tabSchoolAge(tabSchoolAge)
                .rescreenStat(rescreenStat)
                .build();
    }

    /**
     * 获取区域学生计划筛查数量
     *
     * @param notificationId   通知ID
     * @param validDistrictIds 筛选区域ID
     * @return
     */
    private int getPlanScreeningStudentNum(int notificationId, List<Integer> validDistrictIds) {
        Map<Integer, Long> planDistrictStudentMap =
                screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(notificationId);
        int planStudentNum = 0;
        for (Map.Entry<Integer, Long> entry : planDistrictStudentMap.entrySet()) {
            if (!validDistrictIds.contains(entry.getKey())) {
                continue;
            }
            planStudentNum += entry.getValue();
        }
        return planStudentNum;
    }

    /**
     * 获取区域学生计划划筛查数量
     *
     * @param contrastId       通知ID
     * @param validDistrictIds 筛选区域ID
     * @return
     */
    private Integer getScreeningStudentNum(int contrastId, ContrastTypeEnum contrastTypeEnum, List<Integer> validDistrictIds) {
        Map<Integer, Long> planDistrictStudentMap;
        int planStudentNum = 0;
        switch (contrastTypeEnum) {
            case NOTIFICATION:
                planDistrictStudentMap = screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(contrastId);
                break;
            case PLAN:
                planDistrictStudentMap = screeningPlanSchoolStudentService.getDistrictPlanStudentCountByScreeningPlanId(contrastId);
                break;
            case TASK:
                planDistrictStudentMap = screeningPlanSchoolStudentService.getDistrictPlanStudentCountByScreeningTaskId(contrastId);
                break;
            default:
                return planStudentNum;
        }
        if (CollectionUtils.isEmpty(validDistrictIds)) {
            return (int) planDistrictStudentMap.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
        }
        return planDistrictStudentMap.entrySet().stream().filter(x -> validDistrictIds.contains(x.getKey())).mapToInt(x -> x.getValue().intValue()).sum();
    }

    /**
     * 导出统计对比数据
     *
     * @param notificationId1 通知1 ID
     * @param notificationId2 通知2 ID
     * @param districtId      区域ID
     * @param schoolAge       学龄
     * @throws IOException
     * @throws UtilException
     * @deprecated
     */
    @Deprecated
    public void exportStatContrast(Integer notificationId1, Integer notificationId2,
                                   Integer districtId, Integer schoolAge) throws IOException, UtilException {
        Map<String, ScreeningDataContrast> contrastResultMap =
                getScreeningDataContrast(notificationId1, notificationId2, districtId, schoolAge);
        ScreeningDataContrast result1 = contrastResultMap.get("result1");
        ScreeningDataContrast result2 = contrastResultMap.get("result2");
        List<ScreeningDataContrastDTO> exportList = new ArrayList<>();
        if (result1 != null) {
            exportList.add(composeScreeningDataContrastDTO("对比项1", result1));
        }
        if (result2 != null) {
            exportList.add(composeScreeningDataContrastDTO("对比项2", result2));
        }
        excelFacade.exportStatContrast(CurrentUserUtil.getCurrentUser().getId(), exportList,
                exportStatContrastTemplate.getInputStream());
    }

    /**
     * 构建区域及其下级所有符合当前用户范围的区域的搜索条件
     *
     * @param districtId  区域 id
     * @param currentUser 当前用户
     * @return
     */
    private StatConclusionQueryDTO composeDistrictQuery(Integer districtId, CurrentUser currentUser) {
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(this.getValidDistrictIds(districtId, currentUser));
        return query;
    }

    /**
     * @param districtId
     * @param currentUser
     * @return
     */
    private List<Integer> getValidDistrictIds(Integer districtId, CurrentUser currentUser) {
        List<Integer> userDistrictIds = getCurrentUserDistrictIds(currentUser);
        if (districtId != null && districtId >= 0) {
            List<Integer> selectDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            if (CollectionUtils.isNotEmpty(userDistrictIds)) {
                selectDistrictIds.retainAll(userDistrictIds);
            }
            return selectDistrictIds;
        } else {
            return userDistrictIds;
        }
    }

    /**
     * 构造用于文件导出的对比筛查数据
     *
     * @param title    标题
     * @param contrast 对比筛查数据
     * @return
     */
    private ScreeningDataContrastDTO composeScreeningDataContrastDTO(
            String title, ScreeningDataContrast contrast) {
        if (contrast == null) {
            return null;
        }
        RescreenStat rs = contrast.getRescreenStat();
        return ScreeningDataContrastDTO.builder()
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
     *
     * @param currentUser 当前用户
     * @return
     */
    private List<Integer> getCurrentUserDistrictIds(CurrentUser currentUser) {
        if (currentUser.isPlatformAdminUser() || currentUser.getOrgId() == null) {
            return Collections.emptyList();
        }
        GovDept govDept = govDeptService.getById(currentUser.getOrgId());
        District userDistrict = districtService.getById(govDept.getDistrictId());
        return districtService.getSpecificDistrictTreeAllDistrictIds(userDistrict.getId());
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
        List<BasicStatParams> basicStatParams = new ArrayList<>();
        basicStatParams.add(composeBasicParams(GenderEnum.MALE.name(), maleNum, statNum));
        basicStatParams.add(composeBasicParams(GenderEnum.FEMALE.name(), femaleNum, statNum));
        return ClassStat.builder()
                .title(label.name())
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(basicStatParams)
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

        List<BasicStatParams> basicStatParams = new ArrayList<>();
        basicStatParams.add(composeBasicParams(
                SchoolAge.KINDERGARTEN.name(), kindergartenNum, statNum));
        basicStatParams.add(composeBasicParams(SchoolAge.PRIMARY.name(), primaryNum, statNum));
        basicStatParams.add(composeBasicParams(SchoolAge.JUNIOR.name(), juniorNum, statNum));
        basicStatParams.add(composeBasicParams(SchoolAge.HIGH.name(), highNum, statNum));
        basicStatParams.add(composeBasicParams(
                SchoolAge.VOCATIONAL_HIGH.name(), vocationalHighNum, statNum));
        return ClassStat.builder()
                .title(label.name())
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(basicStatParams)
                .build();
    }

    /**
     * 获取复查统计汇总
     * @param rescreenConclusions 复查统计记录
     * @return
     */
    public RescreenStat composeRescreenConclusion(List<StatConclusion> rescreenConclusions) {
        long totalScreeningNum = rescreenConclusions.size();
        long wearingGlassesNum =
                rescreenConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long wearingGlassesIndexNum = wearingGlassesNum * WEARING_GLASSES_RESCREEN_INDEX_NUM;
        long withoutGlassesNum = totalScreeningNum - wearingGlassesNum;
        long withoutGlassesIndexNum = withoutGlassesNum * WITHOUT_GLASSES_RESCREEN_INDEX_NUM;
        long errorIndexNum = rescreenConclusions.stream().mapToLong(StatConclusion::getRescreenErrorNum).sum();
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
                resultConclusion.stream().filter(x -> Boolean.FALSE.equals(x.getIsRescreen())).collect(Collectors.toList());
        List<StatConclusion> validConclusions =
                firstScreeningConclusions.stream().filter(StatConclusion::getIsValid).collect(Collectors.toList());
        long lowVisionNum = validConclusions.stream().filter(StatConclusion::getIsLowVision).count();
        long refractiveErrorNum = validConclusions.stream().filter(StatConclusion::getIsRefractiveError).count();
        long wearingGlassesNum = validConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long myopiaNum = validConclusions.stream().filter(StatConclusion::getIsMyopia).count();
        long totalFirstScreeningNum = firstScreeningConclusions.size();
        long validFirstScreeningNum = validConclusions.size();
        long recommendVisitNum = validConclusions.stream().filter(StatConclusion::getIsRecommendVisit).count();
        long warning0Num =
                validConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel())).count();
        long warning1Num =
                validConclusions.stream().filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel())).count();
        long warning2Num =
                validConclusions.stream().filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel())).count();
        long warning3Num =
                validConclusions.stream().filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel())).count();

        List<StatConclusion> rescreenConclusions =
                resultConclusion.stream().filter(x -> x.getIsRescreen() && x.getIsValid()).collect(Collectors.toList());
        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        return ScreeningDataContrast.builder().screeningNum(planScreeningNum).actualScreeningNum(totalFirstScreeningNum).validScreeningNum(validFirstScreeningNum).averageVisionLeft(averageVision.getAverageVisionLeft()).averageVisionRight(averageVision.getAverageVisionRight()).lowVisionRatio(convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum)).refractiveErrorRatio(convertToPercentage(refractiveErrorNum * 1f / validFirstScreeningNum)).wearingGlassesRatio(convertToPercentage(wearingGlassesNum * 1f / validFirstScreeningNum)).myopiaNum(myopiaNum).myopiaRatio(convertToPercentage(myopiaNum * 1f / validFirstScreeningNum)).focusTargetsNum(warning0Num + warning1Num + warning2Num + warning3Num).warningLevelZeroRatio(convertToPercentage(warning0Num * 1f / validFirstScreeningNum)).warningLevelOneRatio(convertToPercentage(warning1Num * 1f / validFirstScreeningNum)).warningLevelTwoRatio(convertToPercentage(warning2Num * 1f / validFirstScreeningNum)).warningLevelThreeRatio(convertToPercentage(warning3Num * 1f / validFirstScreeningNum)).recommendVisitNum(recommendVisitNum).screeningFinishedRatio(convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum)).rescreenStat(rescreenStat).build();
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
        double sumVisionL = statConclusions.stream().mapToDouble(StatConclusion::getVisionL).sum();
        double sumVisionR = statConclusions.stream().mapToDouble(StatConclusion::getVisionR).sum();
        float avgVisionL = round2Digits(sumVisionL / size);
        float avgVisionR = round2Digits(sumVisionR / size);
        return AverageVision.builder().averageVisionLeft(avgVisionL).averageVisionRight(avgVisionR).build();
    }

    /**
     * 获取用户相关的历年通知、任务、计划用户统计对比筛选项
     *
     * @return
     */
    public Map<Integer, List<ContrastTypeYearItemsDTO>> composeContrastTypeFilter() {
        Map<Integer, List<ContrastTypeYearItemsDTO>> contrastTypeFilterMap = new HashMap<>();
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();

        // Notification list
        List<ScreeningNotice> noticeList = screeningNoticeBizService.getRelatedNoticeByUser(currentUser);
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearNoticeMap = new HashMap<>();
        for (ScreeningNotice notice : noticeList) {
            Date startTimeDate = notice.getStartTime();
            Date endTimeDate = notice.getEndTime();
            if (startTimeDate == null || endTimeDate == null) {
                continue;
            }
            int id = notice.getId();
            String title = notice.getTitle();
            Integer startYear = DateUtil.getYear(startTimeDate);
            long startTime = startTimeDate.getTime();
            long endTime = endTimeDate.getTime();
            ContrastTypeYearItemsDTO.YearItemDTO yearItemDTO = new ContrastTypeYearItemsDTO.YearItemDTO(id, title, startTime, endTime);
            if (yearNoticeMap.containsKey(startYear)) {
                yearNoticeMap.get(startYear).add(yearItemDTO);
            } else {
                yearNoticeMap.put(startYear, new ArrayList(Collections.singletonList(yearItemDTO)));
            }
        }
        List<ContrastTypeYearItemsDTO> contrastTypeYearNotificationList = yearNoticeMap.keySet().stream().sorted()
                .map(x -> new ContrastTypeYearItemsDTO(x, yearNoticeMap.get(x))).collect(Collectors.toList());
        contrastTypeFilterMap.put(ContrastTypeEnum.NOTIFICATION.code, contrastTypeYearNotificationList);

        // Plan List
        List<ScreeningPlan> planList = managementScreeningPlanBizService.getScreeningPlanByUser(currentUser);
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearPlanMap = new HashMap<>();
        for (ScreeningPlan plan : planList) {
            Date startTimeDate = plan.getStartTime();
            Date endTimeDate = plan.getEndTime();
            if (startTimeDate == null || endTimeDate == null) {
                continue;
            }
            int id = plan.getId();
            String title = plan.getTitle();
            Integer startYear = DateUtil.getYear(startTimeDate);
            long startTime = startTimeDate.getTime();
            long endTime = endTimeDate.getTime();
            ContrastTypeYearItemsDTO.YearItemDTO yearItemDTO = new ContrastTypeYearItemsDTO.YearItemDTO(id, title, startTime, endTime);
            if (yearPlanMap.containsKey(startYear)) {
                yearPlanMap.get(startYear).add(yearItemDTO);
            } else {
                yearPlanMap.put(startYear, new ArrayList(Collections.singletonList(yearItemDTO)));
            }
        }
        List<ContrastTypeYearItemsDTO> contrastTypeYearPlanList = yearPlanMap.keySet().stream().sorted()
                .map(x -> new ContrastTypeYearItemsDTO(x, yearPlanMap.get(x))).collect(Collectors.toList());
        contrastTypeFilterMap.put(ContrastTypeEnum.PLAN.code, contrastTypeYearPlanList);

        // Task List
        List<ScreeningTask> taskList = screeningTaskBizService.getScreeningPlanByUser(currentUser);
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearTaskMap = new HashMap<>();
        for (ScreeningTask task : taskList) {
            Date startTimeDate = task.getStartTime();
            Date endTimeDate = task.getEndTime();
            if (startTimeDate == null || endTimeDate == null) {
                continue;
            }
            int id = task.getId();
            String title = task.getTitle();
            Integer startYear = DateUtil.getYear(startTimeDate);
            long startTime = startTimeDate.getTime();
            long endTime = endTimeDate.getTime();
            ContrastTypeYearItemsDTO.YearItemDTO yearItemDTO = new ContrastTypeYearItemsDTO.YearItemDTO(id, title, startTime, endTime);
            if (yearTaskMap.containsKey(startYear)) {
                yearTaskMap.put(startYear, new ArrayList(Collections.singletonList(yearItemDTO)));
            } else {
                yearTaskMap.put(startYear, Collections.singletonList(yearItemDTO));
            }
        }
        List<ContrastTypeYearItemsDTO> contrastTypeYearTaskList = yearTaskMap.keySet().stream().sorted()
                .map(x -> new ContrastTypeYearItemsDTO(x, yearTaskMap.get(x))).collect(Collectors.toList());
        contrastTypeFilterMap.put(ContrastTypeEnum.TASK.code, contrastTypeYearTaskList);

        return contrastTypeFilterMap;
    }

    /**
     * 返回数据对比的筛查项
     *
     * @param contrastType
     * @param contrastId
     * @param districtId
     * @param schoolAge
     * @param schoolId
     * @param schoolGradeCode
     * @param schoolClass
     * @return
     */
    public Map<String, Object> getDataContrastFilter(
            Integer contrastType, Integer contrastId, Integer districtId, Integer schoolAge,
            Integer schoolId, String schoolGradeCode, String schoolClass) throws IOException {
        if (contrastId == null) {
            return null;
        }

        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        StatConclusionQueryDTO query;
        switch (ContrastTypeEnum.get(contrastType)) {
            case NOTIFICATION:
                query = new StatConclusionQueryDTO();
                query.setSrcScreeningNoticeId(contrastId);
                break;
            case PLAN:
                query = new StatConclusionQueryDTO();
                query.setPlanId(contrastId);
                break;
            case TASK:
                query = new StatConclusionQueryDTO();
                query.setTaskId(contrastId);
                break;
            default:
                return null;
        }

        List<Integer> validDistrictIds = this.getValidDistrictIds(districtId, currentUser);
        Integer planScreeningStudentNum = getScreeningStudentNum(contrastId, ContrastTypeEnum.get(contrastType), validDistrictIds);

        query.setDistrictIds(validDistrictIds)
                .setSchoolAge(schoolAge)
                .setSchoolId(schoolId)
                .setIsValid(true)
                .setIsRescreen(false);
        if (schoolId != null) {
            if (schoolClass != null) {
                query.setSchoolClassName(schoolClass);
            }
            if (schoolGradeCode != null) {
                query.setSchoolGradeCode(schoolGradeCode);
            }
        }
        List<StatConclusion> statConclusionList = statConclusionService.listByQuery(query);
        Map<String, Object> data = new HashMap<>();
        data.put("filter", getDataContrastFilter(statConclusionList, schoolId, schoolGradeCode));
        data.put("result", composeScreeningDataContrast(statConclusionList, planScreeningStudentNum));
        return data;
    }

    /**
     * @param statConclusionList
     * @param schoolId
     * @param schoolGradeCode
     * @return
     * @throws IOException
     */
    public DataContrastFilterDTO getDataContrastFilter(
            List<StatConclusion> statConclusionList,
            Integer schoolId, String schoolGradeCode) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Set<Integer> districtIds = statConclusionList.stream().map(StatConclusion::getDistrictId)
                .collect(Collectors.toSet());

        List<District> districtList = districtBizService.getValidDistrictTree(currentUser, districtIds);
        List<Integer> schoolIds = statConclusionList.stream().map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
        List<FilterParamsDTO<Integer, String>> schoolFilterList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<School> schoolList = schoolService.getByIds(schoolIds);
            schoolFilterList = schoolList.stream().map(x -> new FilterParamsDTO<>(x.getId(), x.getName())).collect(Collectors.toList());
        }
        List<Integer> schoolAgeList = statConclusionList.stream().map(StatConclusion::getSchoolAge)
                .distinct().sorted().collect(Collectors.toList());
        List<FilterParamsDTO<Integer, String>> schoolAgeFilterList = schoolAgeList.stream().map(x -> new FilterParamsDTO<>(x, SchoolAge.get(x).desc)).collect(Collectors.toList());

        DataContrastFilterDTO dataContrastFilterDTO = new DataContrastFilterDTO();
        dataContrastFilterDTO.setDistrictList(districtList);
        dataContrastFilterDTO.setSchoolList(schoolFilterList);
        dataContrastFilterDTO.setSchoolAgeList(schoolAgeFilterList);
        dataContrastFilterDTO.setSchoolGradeList(Collections.emptyList());
        dataContrastFilterDTO.setSchoolClassList(Collections.emptyList());
        if (schoolId != null) {
            List<String> schoolGradeCodes = statConclusionList.stream().map(StatConclusion::getSchoolGradeCode).distinct().sorted().collect(Collectors.toList());
            List<FilterParamsDTO<String, String>> schoolGradeFilterList = schoolGradeCodes.stream().map(x -> new FilterParamsDTO<>(x, GradeCodeEnum.getByCode(x).getName())).collect(Collectors.toList());
            dataContrastFilterDTO.setSchoolGradeList(schoolGradeFilterList);
            if (StringUtils.isNotEmpty(schoolGradeCode)) {
                dataContrastFilterDTO.setSchoolClassList(statConclusionList.stream().map(StatConclusion::getSchoolClassName).distinct().sorted().collect(Collectors.toList()));
            }
        }
        return dataContrastFilterDTO;
    }


    /**
     * 平均视力
     */
    @Data
    @Builder
    public static class AverageVision {
        private float averageVisionLeft;
        private float averageVisionRight;
    }

    /**
     * 获取重点视力对象
     *
     * @param districtId  区域ID
     * @param districts   区域列表
     * @param districtIds 区域ID列表
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
            currentDistrictAttentiveObjectsStatistic = currentAttentiveObjectsStatistics.stream().findFirst().orElse(null);
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
            currentDistrictVisionStatistic = currentDistrictVisionStatistics.stream().findFirst().orElse(null);
        }
        //获取数据
        return ScreeningVisionStatisticVO.getInstance(districtVisionStatistics, districtId,
                currentRangeName, screeningNotice, districtIdNameMap, currentDistrictVisionStatistic);
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
            currentDistrictMonitorStatistic = currentDistrictMonitorStatistics.stream().findFirst().orElse(null);
        }
        //获取数据
        return DistrictScreeningMonitorStatisticVO.getInstance(districtMonitorStatistics,
                districtId, currentRangeName, screeningNotice, districtIdNameMap,currentDistrictMonitorStatistic);
    }

    /**
     * 获取复测报告信息
     * @param planId
     * @param schoolId
     * @param qualityControllerName
     * @param qualityControllerCommander
     * @return
     */
    public List<RescreenReportVO> getRescreenStatInfo(Integer planId, Integer schoolId, String qualityControllerName, String qualityControllerCommander) {
        List<RescreenReportVO> rrvos = new ArrayList<>();
        List<StatRescreen> rescreens = statRescreenService.getList(planId, schoolId);
        if (CollectionUtils.isEmpty(rescreens)) {
            return rrvos;
        }
        String orgName = screeningOrganizationService.getNameById(rescreens.get(0).getScreeningOrgId());
        String schoolName = schoolService.getNameById(schoolId);
        rescreens.forEach(rescreen -> {
            RescreenReportVO rrvo = new RescreenReportVO();
            BeanUtils.copyProperties(rescreen, rrvo);
            rrvo.setQualityControllerName(qualityControllerName)
                    .setQualityControllerCommander(qualityControllerCommander)
                    .setOrgName(orgName)
                    .setSchoolName(schoolName);
            rrvos.add(rrvo);
        });
        return rrvos;
    }

    @Transactional
    public int rescreenStat(Date screeningTime) {
        List<StatRescreen> statRescreens = new ArrayList<>();
        // 获取昨日有进行复测的计划及学校信息
        List<ScreenPlanSchoolDTO> rescreenInfo = statConclusionService.getRescreenPlanSchoolByTime(screeningTime);
        // 按计划 + 学校统计复测数据
        rescreenInfo.forEach(rescreen -> {
            List<StatConclusion> rescreenInfoByTime = getRescreenInfo(screeningTime, rescreen.getPlanId(), rescreen.getSchoolId());
            if (com.wupol.framework.core.util.CollectionUtils.isNotEmpty(rescreenInfoByTime)) {
                // 组建统计数据
                StatRescreen statRescreen = new StatRescreen();
                StatConclusion conclusion = rescreenInfoByTime.get(0);
                statRescreen.setScreeningOrgId(conclusion.getScreeningOrgId())
                        .setSrcScreeningNoticeId(conclusion.getSrcScreeningNoticeId())
                        .setTaskId(conclusion.getTaskId())
                        .setPlanId(conclusion.getPlanId())
                        .setSchoolId(conclusion.getSchoolId())
                        .setScreeningTime(screeningTime);
                RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenInfoByTime);
                BeanUtils.copyProperties(rescreenStat, statRescreen);
                statRescreens.add(statRescreen);
            }
        });
        statRescreenService.deleteByScreeningTime(screeningTime);
        statRescreenService.saveBatch(statRescreens);
        return statRescreens.size();
    }

    private List<StatConclusion> getRescreenInfo(Date screeningTime, Integer planId, Integer schoolId) {
        LocalDate startDate = DateUtil.convertToLocalDate(DateUtil.getStartTime(screeningTime), DateUtil.ZONE_UTC_8);
        LocalDate endDate = startDate.plusDays(1l);
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setStartTime(startDate)
                .setEndTime(endDate)
                .setIsRescreen(true)
                .setIsValid(true)
                .setPlanId(planId)
                .setSchoolId(schoolId);
        return statConclusionService.listByQuery(query);
    }

}
