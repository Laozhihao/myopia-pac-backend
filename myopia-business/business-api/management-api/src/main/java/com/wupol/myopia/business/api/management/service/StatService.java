package com.wupol.myopia.business.api.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.domain.dto.*;
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
    public WarningInfo getWarningList(CurrentUser currentUser) {
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
     * 获取统计对比数据
     *
     * @param contrastType
     * @param params
     * @return
     */
    public ScreeningDataContrast getScreeningDataContrast(
            Integer contrastType, DataContrastFilterParamsDTO.Params params, CurrentUser currentUser) {
        Integer contrastId = params.getContrastId();
        if (contrastId == null || contrastId < 0) {
            return null;
        }
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        ContrastTypeEnum contrastTypeEnum = ContrastTypeEnum.get(contrastType);
        switch (contrastTypeEnum) {
            case NOTIFICATION:
                query.setSrcScreeningNoticeId(contrastId);
                break;
            case TASK:
                query.setTaskId(contrastId);
                break;
            case PLAN:
                query.setPlanId(contrastId);
                break;
            default:
                return null;
        }
        Integer districtId = params.getDistrictId();
        Integer schoolAge = params.getSchoolAge();
        Integer schoolId = params.getSchoolId();
        String schoolGradeCode = params.getSchoolGradeCode();
        String schoolClass = params.getSchoolClass();

        List<Integer> validDistrictIds = this.getValidDistrictIds(districtId, currentUser);
        query.setDistrictIds(validDistrictIds);
        query.setSchoolAge(schoolAge);
        query.setSchoolId(schoolId);
        query.setSchoolGradeCode(schoolGradeCode);
        query.setSchoolClassName(schoolClass);
        return composeScreeningDataContrast(statConclusionService.listByQuery(query),
                getPlanScreeningStudentNum(contrastId, contrastTypeEnum, validDistrictIds
                        , schoolAge, schoolId, schoolGradeCode, schoolClass));
    }

    /**
     * 获取用户有效的区域Id列表
     *
     * @param notificationId 筛查通知ID
     * @param currentUser    当前用户
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
     * 分类统计
     *
     * @param notificationId
     * @param currentUser
     * @return
     * @throws IOException
     */
    public ScreeningClassStat getScreeningClassStat(Integer notificationId, CurrentUser currentUser)
            throws IOException {
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
     * 获取学生计划划筛查数量
     *
     * @param contrastId
     * @param contrastTypeEnum
     * @param validDistrictIds
     * @param schoolAge
     * @param schoolId
     * @param schoolGradeCode
     * @param schoolClass
     * @return
     */
    private Integer getPlanScreeningStudentNum(
            int contrastId, ContrastTypeEnum contrastTypeEnum, List<Integer> validDistrictIds, Integer schoolAge,
            Integer schoolId, String schoolGradeCode, String schoolClass) {
        List<ScreeningPlanSchoolStudent> planSchoolStudentList =
                screeningPlanSchoolStudentService.getPlanStudentCountByScreeningItemId(contrastId, contrastTypeEnum);
        if (CollectionUtils.isNotEmpty(validDistrictIds)) {
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> validDistrictIds.contains(x.getSchoolDistrictId())).collect(Collectors.toList());
        }
        if (schoolAge != null) {
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> schoolAge.equals(x.getGradeType())).collect(Collectors.toList());
        }
        if (schoolId != null) {
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> schoolId.equals(x.getSchoolId())).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(schoolGradeCode)) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
            if (gradeCodeEnum == null) {
                return 0;
            }
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> gradeCodeEnum.getName().equals(x.getGradeName())).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(schoolClass)) {
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> schoolClass.equals(x.getClassName())).collect(Collectors.toList());
        }
        return planSchoolStudentList.size();
    }

    /**
     * @param districtId  区域ID
     * @param currentUser 当前用户
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
        return ScreeningDataContrast.builder().screeningNum(planScreeningNum).actualScreeningNum(totalFirstScreeningNum).
                validScreeningNum(validFirstScreeningNum).averageVisionLeft(averageVision.getAverageVisionLeft()).
                averageVisionRight(averageVision.getAverageVisionRight())
                .lowVisionRatio(convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum))
                .refractiveErrorRatio(convertToPercentage(refractiveErrorNum * 1f / validFirstScreeningNum))
                .wearingGlassesRatio(convertToPercentage(wearingGlassesNum * 1f / validFirstScreeningNum))
                .myopiaNum(myopiaNum).myopiaRatio(convertToPercentage(myopiaNum * 1f / validFirstScreeningNum))
                .focusTargetsNum(warning0Num + warning1Num + warning2Num + warning3Num)
                .warningLevelZeroRatio(convertToPercentage(warning0Num * 1f / validFirstScreeningNum))
                .warningLevelOneRatio(convertToPercentage(warning1Num * 1f / validFirstScreeningNum))
                .warningLevelTwoRatio(convertToPercentage(warning2Num * 1f / validFirstScreeningNum))
                .warningLevelThreeRatio(convertToPercentage(warning3Num * 1f / validFirstScreeningNum))
                .recommendVisitNum(recommendVisitNum)
                .screeningFinishedRatio(convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum))
                .rescreenStat(rescreenStat).build();
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
     * @param currentUser 当前用户
     * @return
     */
    public Map<Integer, List<ContrastTypeYearItemsDTO>> composeContrastTypeFilter(CurrentUser currentUser) {
        Map<Integer, List<ContrastTypeYearItemsDTO>> contrastTypeFilterMap = new HashMap<>(5);
        boolean isPlatformAdmin = currentUser.isPlatformAdminUser();
        if (currentUser.isScreeningUser() || isPlatformAdmin) {
            List<ScreeningPlan> planList = managementScreeningPlanBizService.getScreeningPlanByUser(currentUser);
            contrastTypeFilterMap.put(ContrastTypeEnum.PLAN.code, getYearPlanList(planList));
        }
        if (currentUser.isGovDeptUser() || isPlatformAdmin) {
            List<ScreeningNotice> noticeList = screeningNoticeBizService.getRelatedNoticeByUser(currentUser);
            contrastTypeFilterMap.put(ContrastTypeEnum.NOTIFICATION.code, getYearNotificationList(noticeList));

            List<ScreeningTask> taskList = screeningTaskBizService.getScreeningTaskByUser(currentUser);
            contrastTypeFilterMap.put(ContrastTypeEnum.TASK.code, getYearTaskList(taskList));
        }
        return contrastTypeFilterMap;
    }


    /**
     * 返回历年任务列表
     *
     * @param taskList
     * @return
     */
    private List<ContrastTypeYearItemsDTO> getYearTaskList(List<ScreeningTask> taskList) {
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearTaskMap = new HashMap<>();
        for (ScreeningTask task : taskList) {
            composeYearItemMap(yearTaskMap, task.getId(), task.getTitle(), task.getStartTime(), task.getEndTime());
        }
        return sortYearItemMap(yearTaskMap);
    }

    /**
     * Map排序
     *
     * @param yearItemMap
     * @return
     */
    private List<ContrastTypeYearItemsDTO> sortYearItemMap(Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearItemMap) {
        for (Map.Entry<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> entry : yearItemMap.entrySet()) {
            entry.setValue(entry.getValue().stream().sorted(
                    Comparator.comparing(ContrastTypeYearItemsDTO.YearItemDTO::getStartTime).reversed()).collect(Collectors.toList()));
        }
        return yearItemMap.keySet().stream().sorted(Comparator.reverseOrder())
                .map(x -> new ContrastTypeYearItemsDTO(x, yearItemMap.get(x))).collect(Collectors.toList());
    }

    /**
     * @param yearTaskMap
     * @param id
     * @param title
     * @param startTimeDate
     * @param endTimeDate
     */
    private void composeYearItemMap(Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearTaskMap, int id, String title, Date startTimeDate, Date endTimeDate) {
        if (startTimeDate == null || endTimeDate == null) {
            return;
        }
        Integer startYear = DateUtil.getYear(startTimeDate);
        Integer endYear = DateUtil.getYear(endTimeDate);
        long startTime = startTimeDate.getTime();
        long endTime = endTimeDate.getTime();
        ContrastTypeYearItemsDTO.YearItemDTO yearItemDTO = new ContrastTypeYearItemsDTO.
                YearItemDTO(id, title, startTime, endTime);
        for (int i = startYear; i <= endYear; i++) {
            if (yearTaskMap.containsKey(i)) {
                yearTaskMap.get(i).add(yearItemDTO);
            } else {
                yearTaskMap.put(i, new ArrayList<>(Collections.singletonList(yearItemDTO)));
            }
        }
    }

    /**
     * 返回历年计划
     *
     * @param planList
     * @return
     */
    private List<ContrastTypeYearItemsDTO> getYearPlanList(List<ScreeningPlan> planList) {
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearPlanMap = new HashMap<>();
        for (ScreeningPlan plan : planList) {
            composeYearItemMap(yearPlanMap, plan.getId(), plan.getTitle(), plan.getStartTime(), plan.getEndTime());
        }
        return sortYearItemMap(yearPlanMap);
    }

    /**
     * 返回历年通知列表
     *
     * @param noticeList
     * @return
     */
    private List<ContrastTypeYearItemsDTO> getYearNotificationList(List<ScreeningNotice> noticeList) {
        Map<Integer, List<ContrastTypeYearItemsDTO.YearItemDTO>> yearNoticeMap = new HashMap<>();
        for (ScreeningNotice notice : noticeList) {
            composeYearItemMap(yearNoticeMap, notice.getId(), notice.getTitle(), notice.getStartTime(), notice.getEndTime());
        }
        return sortYearItemMap(yearNoticeMap);
    }

    /**
     * 返回数据对比的筛查项
     *
     * @param contrastType 对比类型
     * @param params       对比参数
     * @param currentUser  当前用户
     * @return
     * @throws IOException
     */
    public DataContrastFilterResultDTO getDataContrastFilter(
            Integer contrastType, DataContrastFilterParamsDTO.Params params, CurrentUser currentUser) throws IOException {
        Integer contrastId = params.getContrastId();
        if (contrastId == null) {
            return null;
        }
        ContrastTypeEnum contrastTypeEnum = ContrastTypeEnum.get(contrastType);
        if (contrastTypeEnum == null) {
            return null;
        }

        StatConclusionQueryDTO query;
        switch (contrastTypeEnum) {
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

        Integer districtId = params.getDistrictId();
        Integer schoolAge = params.getSchoolAge();
        Integer schoolId = params.getSchoolId();
        String schoolGradeCode = params.getSchoolGradeCode();
        String schoolClass = params.getSchoolClass();

        List<Integer> validDistrictIds = this.getValidDistrictIds(districtId, currentUser);
        Integer planScreeningStudentNum = getPlanScreeningStudentNum(contrastId, contrastTypeEnum, validDistrictIds
                , schoolAge, schoolId, schoolGradeCode, schoolClass);
        query.setDistrictIds(validDistrictIds)
                .setSchoolAge(schoolAge)
                .setSchoolId(schoolId);
        if (schoolId != null) {
            if (schoolClass != null) {
                query.setSchoolClassName(schoolClass);
            }
            if (schoolGradeCode != null) {
                query.setSchoolGradeCode(schoolGradeCode);
            }
        }
        List<StatConclusion> statConclusionList = statConclusionService.listByQuery(query);
        return new DataContrastFilterResultDTO(
                getDataContrastFilter(statConclusionList, schoolId, schoolGradeCode, currentUser),
                composeScreeningDataContrast(statConclusionList, planScreeningStudentNum));
    }

    /**
     * 获取对比统计过滤参数
     *
     * @param statConclusionList 统计结论列表
     * @param schoolId           学校id
     * @param schoolGradeCode    年级code
     * @param currentUser        当前用户
     * @return
     * @throws IOException
     */
    public DataContrastFilterDTO getDataContrastFilter(
            List<StatConclusion> statConclusionList, Integer schoolId, String schoolGradeCode, CurrentUser currentUser)
            throws IOException {
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
     * 导出统计对比数据
     *
     * @param dataContrastFilterParams 数据对比过滤参数
     * @param currentUser
     * @throws IOException
     * @throws UtilException
     */
    public void exportStatContrast(DataContrastFilterParamsDTO dataContrastFilterParams, CurrentUser currentUser)
            throws IOException, UtilException {
        Integer contrastType = dataContrastFilterParams.getContrastType();
        List<DataContrastFilterParamsDTO.Params> paramsList = dataContrastFilterParams.getParams();
        List<ScreeningDataContrastDTO> exportList = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); i++) {
            DataContrastFilterParamsDTO.Params params = paramsList.get(i);
            Integer contrastId = params.getContrastId();
            if (contrastId == null) {
                exportList.add(composeScreeningDataContrastDTO("对比项" + (i + 1),
                        composeScreeningDataContrast(Collections.emptyList(), 0)));
                continue;
            }
            ScreeningDataContrast screeningDataContrast = getScreeningDataContrast(contrastType, params, currentUser);
            exportList.add(composeScreeningDataContrastDTO("对比项" + (i + 1), screeningDataContrast));
        }
        excelFacade.exportStatContrast(currentUser.getId(), exportList,
                exportStatContrastTemplate.getInputStream());
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
     *
     * @param districtId
     * @param noticeId
     * @param screeningNotice
     * @return
     * @throws IOException
     */
    public ScreeningVisionStatisticVO getScreeningVisionStatisticVO(
            Integer districtId, Integer noticeId, ScreeningNotice screeningNotice, CurrentUser currentUser) throws IOException {
        //查找合计数据（当前层级 + 下级）
        List<DistrictVisionStatistic> districtVisionStatistics =
                districtVisionStatisticService.getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(
                        noticeId, districtId, currentUser, true);
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
                        noticeId, districtId, currentUser, false);
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
     *
     * @param districtId
     * @param noticeId
     * @param screeningNotice
     * @param currentUser
     * @return
     * @throws IOException
     */
    public DistrictScreeningMonitorStatisticVO getDistrictScreeningMonitorStatisticVO(
            Integer districtId, Integer noticeId, ScreeningNotice screeningNotice, CurrentUser currentUser)
            throws IOException {
        //根据层级获取数据(当前层级，下级层级，汇总数据）
        List<DistrictMonitorStatistic> districtMonitorStatistics =
                districtMonitorStatisticService.getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(
                        noticeId, districtId, currentUser, true);
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
                        noticeId, districtId, currentUser, false);
        DistrictMonitorStatistic currentDistrictMonitorStatistic = null;
        if (CollectionUtils.isNotEmpty(currentDistrictMonitorStatistics)) {
            currentDistrictMonitorStatistic = currentDistrictMonitorStatistics.stream().findFirst().orElse(null);
        }
        //获取数据
        return DistrictScreeningMonitorStatisticVO.getInstance(districtMonitorStatistics,
                districtId, currentRangeName, screeningNotice, districtIdNameMap, currentDistrictMonitorStatistic);
    }

    /**
     * 获取复测报告信息
     *
     * @param planId
     * @param schoolId
     * @param qualityControllerName
     * @param qualityControllerCommander
     * @return
     */
    public List<RescreenReportVO> getRescreenStatInfo(
            Integer planId, Integer schoolId, String qualityControllerName, String qualityControllerCommander) {
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

    /**
     * @param screeningTime
     * @return
     */
    @Transactional
    public int rescreenStat(Date screeningTime) {
        List<StatRescreen> statRescreens = new ArrayList<>();
        // 获取昨日有进行复测的计划及学校信息
        List<ScreenPlanSchoolDTO> rescreenInfo = statConclusionService.getRescreenPlanSchoolByTime(screeningTime);
        // 按计划 + 学校统计复测数据
        rescreenInfo.forEach(rescreen -> {
            List<StatConclusion> rescreenInfoByTime = getRescreenInfo(screeningTime, rescreen.getPlanId(), rescreen.getSchoolId());
            if (CollectionUtils.isNotEmpty(rescreenInfoByTime)) {
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

    /**
     * @param screeningTime
     * @param planId
     * @param schoolId
     * @return
     */
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
