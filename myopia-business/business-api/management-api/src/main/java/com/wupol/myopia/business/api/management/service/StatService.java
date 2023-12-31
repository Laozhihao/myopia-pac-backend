package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.SchoolResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.facade.StatFacade;
import com.wupol.myopia.business.api.management.domain.dto.*;
import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.StatClassLabel;
import com.wupol.myopia.business.core.screening.flow.domain.dos.WarningLevelCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.domain.vo.ReScreeningCardVO;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.ReScreenCardUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictAttentiveObjectsStatisticService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
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
    private StatRescreenService statRescreenService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningTaskBizService screeningTaskBizService;
    @Autowired
    private StatDistrictService statDistrictService;
    @Autowired
    private StatSchoolService statSchoolService;
    @Autowired
    private StatFacade statFacade;

    @Value("classpath:excel/ExportStatContrastTemplate.xlsx")
    private Resource exportStatContrastTemplate;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 预警信息
     *
     * @return
     */
    public WarningInfo getWarningList(CurrentUser currentUser) {
        // 获取统计时间区间
        List<Integer> districtIds = this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(districtIds);
        query.setIsValid(true);
        query.setIsRescreen(false);
        query.setIsCooperative(0);
        StatConclusion lastConclusion = statConclusionService.getLastOne(query);
        if (lastConclusion == null) {
            return WarningInfo.builder().build();
        }
        ZoneId zoneId = ZoneId.of("UTC+8");
        LocalDate endDate = DateUtil.convertToLocalDate(lastConclusion.getCreateTime(), zoneId).plusDays(1);
        LocalDate startDate = endDate.plusYears(-1);
        query.setStartTime(startDate).setEndTime(endDate);
        // 统计
        List<WarningLevelCountDO> warningLevelCountList = statConclusionService.countWarningLevel(query);
        int total = (int)warningLevelCountList.stream().map(WarningLevelCountDO::getCount).count();
        Map<Integer, Integer> warningLevelCountMap = warningLevelCountList.stream().filter(x -> Objects.nonNull(x.getWarningLevel())).collect(Collectors.toMap(WarningLevelCountDO::getWarningLevel, WarningLevelCountDO::getCount));
        // 构建返回值
        int warning0Num = warningLevelCountMap.getOrDefault(WarningLevel.ZERO.code, CommonConst.ZERO);
        int warning1Num = warningLevelCountMap.getOrDefault(WarningLevel.ONE.code, CommonConst.ZERO);
        int warning2Num = warningLevelCountMap.getOrDefault(WarningLevel.TWO.code, CommonConst.ZERO);
        int warning3Num = warningLevelCountMap.getOrDefault(WarningLevel.THREE.code, CommonConst.ZERO);
        int focusTargetsNum = warning0Num + warning1Num + warning2Num + warning3Num;
        ArrayList<WarningLevelInfo> warningLevelInfoArrayList = new ArrayList<>();
        warningLevelInfoArrayList.add(new WarningLevelInfo(0, warning0Num, MathUtil.ratio(warning0Num,total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(1, warning1Num, MathUtil.ratio(warning1Num,total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(2, warning2Num, MathUtil.ratio(warning2Num,total)));
        warningLevelInfoArrayList.add(new WarningLevelInfo(3, warning3Num, MathUtil.ratio(warning3Num,total)));
        return WarningInfo.builder()
                .statTime(startDate.atStartOfDay(zoneId).toInstant().toEpochMilli())
                .endTime(endDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1)
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(MathUtil.ratio(focusTargetsNum,total))
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
    public ScreeningDataContrast getScreeningDataContrast(Integer contrastType, DataContrastFilterParamsDTO.Params params, CurrentUser currentUser) {
        if (currentUser.isGovDeptUser() && Objects.isNull(params.getDistrictId())) {
            params.setDistrictId(districtBizService.getNotPlatformAdminUserDistrict(currentUser).getId());
        }
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

        List<Integer> validDistrictIds = this.getValidDistrictIds(contrastTypeEnum, contrastId, districtId, currentUser);
        query.setDistrictIds(validDistrictIds);
        query.setSchoolAge(schoolAge);
        query.setSchoolId(schoolId);
        query.setSchoolGradeCode(schoolGradeCode);
        query.setSchoolClassName(schoolClass);
        return composeScreeningDataContrast(statConclusionService.listOfReleasePlanByQuery(query),
                getPlanScreeningStudentNum(contrastId, contrastTypeEnum, validDistrictIds, schoolAge, schoolId, schoolGradeCode, schoolClass));
    }

    /**
     * 获取用户有效的区域Id列表
     *
     * @param notificationId 筛查通知ID
     * @param currentUser    当前用户
     * @return
     */
    public List<Integer> getValidDistrictIdsByNotificationId(int notificationId, CurrentUser currentUser) {
        List<ScreeningPlan> screeningPlans = managementScreeningPlanBizService
                .getScreeningPlanByNoticeIdAndUser(notificationId, currentUser);
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
    public ScreeningClassStat getScreeningClassStat(Integer notificationId, CurrentUser currentUser) throws IOException {
        List<Integer> validDistrictIds = new ArrayList<>();
        if (currentUser.isGovDeptUser()) {
            // 获取以当前政府人员所属行政区域为根节点的行政区域树
            districtService.getAllIds(validDistrictIds, districtBizService.getCurrentUserDistrictTree(currentUser));
        } else {
            validDistrictIds = this.getValidDistrictIdsByNotificationId(notificationId, currentUser);
        }
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(validDistrictIds);
        query.setSrcScreeningNoticeId(notificationId);
        List<StatConclusion> statConclusions = statConclusionService.listOfReleasePlanByQuery(query);
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
                validConclusions.stream().filter(sc-> Objects.equals(sc.getIsMyopia(),Boolean.TRUE)).collect(Collectors.toList());

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

        RescreenStat rescreenStat = this.rescreenConclusion(rescreenConclusions);

        TwoTuple<BigDecimal, BigDecimal> tuple = StatUtil.calculateAverageVision(validConclusions);
        int planScreeningNum = getPlanScreeningStudentNum(notificationId, validDistrictIds);
        return ScreeningClassStat.builder().notificationId(notificationId)
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .screeningFinishedRatio(planScreeningNum > 0 ?
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum) : 0)
                .averageVisionLeft(tuple.getFirst())
                .averageVisionRight(tuple.getSecond())
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
     * @param schoolClassName
     * @return
     */
    private Integer getPlanScreeningStudentNum(int contrastId, ContrastTypeEnum contrastTypeEnum, List<Integer> validDistrictIds, Integer schoolAge,
                                               Integer schoolId, String schoolGradeCode, String schoolClassName) {
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getPlanStudentCountByScreeningItemId(contrastId, contrastTypeEnum);
        SchoolGrade schoolGrade = schoolGradeService.getByGradeCodeAndSchoolId(schoolId, schoolGradeCode);
        SchoolClass schoolClass = schoolClassService.getByClassNameAndSchoolId(schoolId, schoolGrade.getId(), schoolClassName);


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
                    .filter(x -> Objects.equals(x.getGradeId(), schoolGrade.getId())).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(schoolClassName)) {
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(x -> Objects.equals(x.getClassId(), schoolClass.getId())).collect(Collectors.toList());
        }
        return planSchoolStudentList.size();
    }

    /**
     * @param districtId  区域ID
     * @param currentUser 当前用户
     * @return
     */
    private List<Integer> getValidDistrictIds(ContrastTypeEnum contrastTypeEnum, Integer contrastId, Integer districtId, CurrentUser currentUser) {
        Set<Integer> userDistrictSet = getDistrictIdsByContrastType(contrastTypeEnum, contrastId, currentUser);
        if (districtId != null && districtId >= 0) {
            List<Integer> selectDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            if (CollectionUtils.isNotEmpty(userDistrictSet)) {
                selectDistrictIds.retainAll(userDistrictSet);
            }
            return selectDistrictIds;
        } else {
            return userDistrictSet.stream().collect(Collectors.toList());
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
                .averageVisionLeft(Optional.ofNullable(contrast.getAverageVisionLeft()).map(BigDecimal::toString).orElse(StrUtil.EMPTY))
                .averageVisionRight(Optional.ofNullable(contrast.getAverageVisionRight()).map(BigDecimal::toString).orElse(StrUtil.EMPTY))
                .lowVisionNum(contrast.getLowVisionNum())
                .lowVisionRatio(nullToRatio(contrast.getLowVisionRatio()))
                .wearingGlassesNum(contrast.getWearingGlassesNum())
                .wearingGlassesRatio(nullToRatio(contrast.getWearingGlassesRatio()))
                .myopiaNum(contrast.getMyopiaNum())
                .myopiaRatio(nullToRatio(contrast.getMyopiaRatio()))
                .focusTargetsNum(contrast.getFocusTargetsNum())
                .focusTargetsRatio(nullToRatio(contrast.getFocusTargetsRatio()))
                .warningLevelZeroNum(contrast.getWarningLevelZeroNum())
                .warningLevelZeroRatio(nullToRatio(contrast.getWarningLevelZeroRatio()))
                .warningLevelOneNum(contrast.getWarningLevelOneNum())
                .warningLevelOneRatio(nullToRatio(contrast.getWarningLevelOneRatio()))
                .warningLevelTwoNum(contrast.getWarningLevelTwoNum())
                .warningLevelTwoRatio(nullToRatio(contrast.getWarningLevelTwoRatio()))
                .warningLevelThreeNum(contrast.getWarningLevelThreeNum())
                .warningLevelThreeRatio(nullToRatio(contrast.getWarningLevelThreeRatio()))
                .recommendVisitNum(contrast.getRecommendVisitNum())
                .recommendVisitRatio(nullToRatio(contrast.getRecommendVisitRatio()))
                .rescreenNum(rs.getRescreenNum())
                .wearingGlassesRescreenNum(rs.getWearingGlassesRescreenNum())
                .wearingGlassesRescreenIndexNum(rs.getWearingGlassesRescreenIndexNum())
                .withoutGlassesRescreenNum(rs.getWithoutGlassesRescreenNum())
                .withoutGlassesRescreenIndexNum(rs.getWithoutGlassesRescreenIndexNum())
                .rescreenItemNum(rs.getRescreenItemNum())
                .incorrectItemNum(rs.getIncorrectItemNum())
                .incorrectRatio(nullToRatio(rs.getIncorrectRatio()))
                .build();
    }

    /**
     * 将null转换为0占比
     *
     * @param ratio
     * @return
     */
    private String nullToRatio(Float ratio) {
        return (ratio == null ? 0f : ratio) + "%";
    }

    /**
     * 获取当前用户所有权限的区域ID
     *
     * @param currentUser 当前用户
     * @return
     */
    private List<Integer> getCurrentUserDistrictIds(CurrentUser currentUser) {
        //TODO: 机构用户是否有权限调用此接口？
        if (currentUser.isScreeningUser() || (currentUser.isHospitalUser() && (Objects.nonNull(currentUser.getScreeningOrgId())))) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(currentUser.getScreeningOrgId());
            District userDistrict = districtService.getById(screeningOrganization.getDistrictId());
            return districtService.getSpecificDistrictTreeAllDistrictIds(userDistrict.getId());
        }
        if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            District userDistrict = districtService.getById(govDept.getDistrictId());
            return districtService.getSpecificDistrictTreeAllDistrictIds(userDistrict.getId());
        }
        return Collections.emptyList();
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
        long errorIndexNum = rescreenConclusions.stream().mapToLong(StatConclusion::getRescreenErrorNum).sum() - rescreenConclusions.stream().mapToLong(StatConclusion::getPhysiqueRescreenErrorNum).sum();
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

    public RescreenStat rescreenConclusion(List<StatConclusion> rescreenConclusions) {

        int totalScreeningNum = rescreenConclusions.size();
        Integer screeningType =null;
        List<Integer> typeList = rescreenConclusions.stream().map(StatConclusion::getScreeningType).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(typeList)){
            screeningType=typeList.get(0);
        }

        long wearingGlassesNum = rescreenConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long withoutGlassesNum = rescreenConclusions.stream().filter(x -> x.getGlassesType() <= 0).count();
        long rescreenItemNum = rescreenConclusions.stream().map(StatConclusion::getRescreenItemNum).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();

        RescreenStat.RescreenStatBuilder builder = RescreenStat.builder();

        if (Objects.equals(screeningType, ScreeningTypeEnum.COMMON_DISEASE.getType())){
            builder.wearingGlassesRescreenIndexNum(8);
            builder.withoutGlassesRescreenIndexNum(6);
            long wearingGlassesIndexNum = wearingGlassesNum * 8;
            long withoutGlassesIndexNum = withoutGlassesNum * 6;
            long errorIndexNum = rescreenConclusions.stream().mapToLong(StatConclusion::getRescreenErrorNum).sum();
            builder.incorrectItemNum(errorIndexNum)
                    .incorrectRatio(convertToPercentage(errorIndexNum * 1f / (wearingGlassesIndexNum + withoutGlassesIndexNum)));
        }else {
            builder.wearingGlassesRescreenIndexNum(6);
            builder.withoutGlassesRescreenIndexNum(4);
            long wearingGlassesIndexNum = wearingGlassesNum * 6;
            long withoutGlassesIndexNum = withoutGlassesNum * 4;
            long errorIndexNum = rescreenConclusions.stream().mapToLong(StatConclusion::getRescreenErrorNum).sum() - rescreenConclusions.stream().mapToLong(StatConclusion::getPhysiqueRescreenErrorNum).sum();
            builder.incorrectItemNum(errorIndexNum)
                    .incorrectRatio(convertToPercentage(errorIndexNum * 1f / (wearingGlassesIndexNum + withoutGlassesIndexNum)));
        }
        return builder
                .rescreenNum(totalScreeningNum)
                .wearingGlassesRescreenNum(wearingGlassesNum)
                .withoutGlassesRescreenNum(withoutGlassesNum)
                .rescreenItemNum(rescreenItemNum)
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
        long lowVisionNum = validConclusions.stream().filter(sc->Objects.equals(sc.getIsLowVision(),Boolean.TRUE)).count();
        long refractiveErrorNum = validConclusions.stream().filter(sc->Objects.equals(sc.getIsRefractiveError(),Boolean.TRUE)).count();
        long wearingGlassesNum = validConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long myopiaNum = validConclusions.stream().filter(sc->Objects.equals(sc.getIsMyopia(),Boolean.TRUE)).count();
        long totalFirstScreeningNum = firstScreeningConclusions.size();
        long validFirstScreeningNum = validConclusions.size();
        long recommendVisitNum = validConclusions.stream().filter(sc->Objects.equals(sc.getIsRecommendVisit(),Boolean.TRUE)).count();
        long warning0Num =
                validConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel())).count();
        long warning1Num =
                validConclusions.stream().filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel())).count();
        long warning2Num =
                validConclusions.stream().filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel())).count();
        long warning3Num =
                validConclusions.stream().filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel())).count();
        long warningSPNum =
                validConclusions.stream().filter(x -> WarningLevel.ZERO_SP.code.equals(x.getWarningLevel())).count();
        long focusTargetsNum = warning0Num + warning1Num + warning2Num + warning3Num;

        List<StatConclusion> rescreenConclusions =
                resultConclusion.stream().filter(x -> x.getIsRescreen() && x.getIsValid()).collect(Collectors.toList());
        TwoTuple<BigDecimal, BigDecimal> tuple = StatUtil.calculateAverageVision(validConclusions);
        RescreenStat rescreenStat = this.rescreenConclusion(rescreenConclusions);
        return ScreeningDataContrast.builder()
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .averageVisionLeft(tuple.getFirst())
                .averageVisionRight(tuple.getSecond())
                .lowVisionNum(lowVisionNum)
                .lowVisionRatio(convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum))
                .refractiveErrorRatio(convertToPercentage(refractiveErrorNum * 1f / validFirstScreeningNum))
                .wearingGlassesNum(wearingGlassesNum)
                .wearingGlassesRatio(convertToPercentage(wearingGlassesNum * 1f / validFirstScreeningNum))
                .myopiaNum(myopiaNum)
                .myopiaRatio(convertToPercentage(myopiaNum * 1f / validFirstScreeningNum))
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsRatio(convertToPercentage(focusTargetsNum * 1f / validFirstScreeningNum))
                .warningLevelZeroNum(warning0Num + warningSPNum)
                .warningLevelZeroRatio(convertToPercentage((warning0Num + warningSPNum) * 1f / validFirstScreeningNum))
                .warningLevelOneNum(warning1Num)
                .warningLevelOneRatio(convertToPercentage(warning1Num * 1f / validFirstScreeningNum))
                .warningLevelTwoNum(warning2Num)
                .warningLevelTwoRatio(convertToPercentage(warning2Num * 1f / validFirstScreeningNum))
                .warningLevelThreeNum(warning3Num)
                .warningLevelThreeRatio(convertToPercentage(warning3Num * 1f / validFirstScreeningNum))
                .recommendVisitNum(recommendVisitNum)
                .recommendVisitRatio(convertToPercentage(recommendVisitNum * 1f / validFirstScreeningNum))
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
     * 获取用户相关的历年通知、任务、计划用户统计对比筛选项
     *
     * @param currentUser 当前用户
     * @return
     */
    public Map<Integer, List<ContrastTypeYearItemsDTO>> composeContrastTypeFilter(CurrentUser currentUser) {
        Map<Integer, List<ContrastTypeYearItemsDTO>> contrastTypeFilterMap = new HashMap<>(5);
        boolean isPlatformAdmin = currentUser.isPlatformAdminUser();
        if (currentUser.isScreeningUser() || currentUser.isHospitalUser() || isPlatformAdmin) {
            List<ScreeningPlan> planList = managementScreeningPlanBizService.getReleaseScreeningPlanByUser(currentUser);
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
     * @return DataContrastFilterResultDTO
     * @throws IOException io异常
     */
    public DataContrastFilterResultDTO getDataContrastFilter(
            Integer contrastType, DataContrastFilterParamsDTO.Params params, CurrentUser currentUser) {
        if (currentUser.isGovDeptUser() && Objects.isNull(params.getDistrictId())) {
            params.setDistrictId(districtBizService.getNotPlatformAdminUserDistrict(currentUser).getId());
        }
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

        List<Integer> validDistrictIds = this.getValidDistrictIds(contrastTypeEnum, contrastId, districtId, currentUser);
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
        List<StatConclusion> statConclusionList = statConclusionService.listOfReleasePlanByQuery(query);
        DataContrastFilterResultDTO dataContrastFilterResultDTO = new DataContrastFilterResultDTO(
                getDataContrastFilter(statConclusionList, schoolId, schoolGradeCode, currentUser),
                composeScreeningDataContrast(statConclusionList, planScreeningStudentNum));

        if (!CollectionUtils.isEmpty(statConclusionList) && Objects.equals(statConclusionList.get(0).getScreeningType(), ScreeningTypeEnum.COMMON_DISEASE.getType())) {
            RescreenStat rescreenStat = dataContrastFilterResultDTO.getResult().getRescreenStat();
            rescreenStat.setWearingGlassesRescreenIndexNum(8);
            rescreenStat.setWithoutGlassesRescreenIndexNum(6);
            dataContrastFilterResultDTO.getResult().setRescreenStat(rescreenStat);
        }
        return dataContrastFilterResultDTO;
    }

    private Set<Integer> getDistrictIdsByContrastType(ContrastTypeEnum contrastTypeEnum, Integer contrastId, CurrentUser currentUser) {
        Set<Integer> districtIdList;
        switch (contrastTypeEnum) {
            case NOTIFICATION:
                districtIdList = getDistrictIdList(managementScreeningPlanBizService.getScreeningPlanByNoticeIdAndUser(contrastId, currentUser));
                break;
            case TASK:
                districtIdList = getDistrictIdList(managementScreeningPlanBizService.getScreeningPlanByTaskIdAndUser(contrastId, currentUser));
                break;
            case PLAN:
                districtIdList = getDistrictIdList(Collections.singletonList(screeningPlanService.getById(contrastId)));
                break;
            default:
                return Collections.emptySet();
        }
        return districtIdList;
    }

    private Set<Integer> getDistrictIdList(List<ScreeningPlan> screeningPlanList) {
        return schoolBizService.getAllSchoolDistrictIdsByScreeningPlanIds(screeningPlanList.stream()
                .map(ScreeningPlan::getId).collect(Collectors.toList()));
    }

    /**
     * 获取对比统计过滤参数
     *
     * @param statConclusionList 统计结论列表
     * @param schoolId           学校id
     * @param schoolGradeCode    年级code
     * @param currentUser        当前用户
     * @return DataContrastFilterDTO
     * @throws IOException io异常
     */
    public DataContrastFilterDTO getDataContrastFilter(
            List<StatConclusion> statConclusionList, Integer schoolId, String schoolGradeCode, CurrentUser currentUser) {
        Set<Integer> districtIds = statConclusionList.stream().map(StatConclusion::getDistrictId)
                .collect(Collectors.toSet());

        List<District> districtList;
        List<Integer> schoolIds;
        List<Integer> schoolAgeList;

        // 政府人员的逻辑需要新的逻辑
        if (currentUser.isGovDeptUser()) {
            // 获取当前用户与districts下行政区域的交集
            districtList = districtBizService.getChildDistrictValidDistrictTree(currentUser, districtIds);
            List<Integer> allDistrictId = new ArrayList<>();
            districtService.getAllIds(allDistrictId, districtList);
            schoolIds = statConclusionList.stream().filter(s -> allDistrictId.contains(s.getDistrictId())).map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
            schoolAgeList = statConclusionList.stream().filter(s -> allDistrictId.contains(s.getDistrictId())).map(StatConclusion::getSchoolAge).distinct().sorted().collect(Collectors.toList());
        } else {
            districtList = districtBizService.getValidDistrictTree(currentUser, districtIds);
            schoolIds = statConclusionList.stream().map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
            schoolAgeList = statConclusionList.stream().map(StatConclusion::getSchoolAge)
                    .distinct().sorted().collect(Collectors.toList());
        }
        List<FilterParamsDTO<Integer, String>> schoolFilterList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<School> schoolList = schoolService.getByIds(schoolIds);
            schoolFilterList = schoolList.stream().map(x -> new FilterParamsDTO<>(x.getId(), x.getName())).collect(Collectors.toList());
        }
        List<FilterParamsDTO<Integer, String>> schoolAgeFilterList = schoolAgeList.stream().map(x -> new FilterParamsDTO<>(x, SchoolAge.get(x).desc)).collect(Collectors.toList());
        return getDataContrastFilterDTO(statConclusionList, schoolId, schoolGradeCode, districtList, schoolFilterList, schoolAgeFilterList);
    }

    /**
     * 封装DataContrastFilterDTO
     *
     * @param statConclusionList  筛查数据结论
     * @param schoolId            学校Id
     * @param schoolGradeCode     年级编码
     * @param districtList        行政区域List
     * @param schoolFilterList    学校过滤参数
     * @param schoolAgeFilterList 学龄段过滤参数
     * @return DataContrastFilterDTO
     */
    private DataContrastFilterDTO getDataContrastFilterDTO(List<StatConclusion> statConclusionList, Integer schoolId,
                                                           String schoolGradeCode, List<District> districtList,
                                                           List<FilterParamsDTO<Integer, String>> schoolFilterList,
                                                           List<FilterParamsDTO<Integer, String>> schoolAgeFilterList) {
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
            StringBuilder title = new StringBuilder();
            title.append(i == 0 ? "被对比项：" : "对比项" + i + "：");
            composeFilterTitle(title, contrastType, params);
            if (contrastId == null) {
                exportList.add(composeScreeningDataContrastDTO(title.toString(),
                        composeScreeningDataContrast(Collections.emptyList(), 0)));
                continue;
            }
            ScreeningDataContrast screeningDataContrast = getScreeningDataContrast(contrastType, params, currentUser);
            exportList.add(composeScreeningDataContrastDTO(title.toString(), screeningDataContrast));
        }
        excelFacade.exportStatContrast(currentUser.getId(), exportList,
                exportStatContrastTemplate.getInputStream());
    }

    private void composeFilterTitle(StringBuilder builder, Integer contrastType,
                                    DataContrastFilterParamsDTO.Params filterParams) {
        Integer contrastId = filterParams.getContrastId();
        switch (ContrastTypeEnum.get(contrastType)) {
            case NOTIFICATION:
                ScreeningNotice notice = screeningNoticeService.getById(contrastId);
                builder.append(composeContrastParams(notice.getTitle(), notice.getStartTime(), notice.getEndTime()));
                break;
            case TASK:
                ScreeningTask task = screeningTaskService.getById(contrastId);
                builder.append(composeContrastParams(task.getTitle(), task.getStartTime(), task.getEndTime()));
                break;
            case PLAN:
                ScreeningPlan plan = screeningPlanService.getById(contrastId);
                builder.append(composeContrastParams(plan.getTitle(), plan.getStartTime(), plan.getEndTime()));
                break;
            default:
                return;
        }

        Integer districtId = filterParams.getDistrictId();
        if (districtId != null) {
            District district = districtService.getById(districtId);
            if (district != null) {
                builder.append("，").append(district.getName());
            }
        }

        Integer schoolAgeCode = filterParams.getSchoolAge();
        if (schoolAgeCode != null) {
            SchoolAge schoolAge = SchoolAge.get(schoolAgeCode);
            if (schoolAge != null) {
                builder.append("，").append(schoolAge.desc);
            }
        }

        Integer schoolId = filterParams.getSchoolId();
        if (schoolId != null) {
            School school = schoolService.getById(schoolId);
            if (school != null) {
                builder.append("，").append(school.getName());
            }
        }

        String schoolGradeCode = filterParams.getSchoolGradeCode();
        if (StringUtils.isNotEmpty(schoolGradeCode)) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
            if (gradeCodeEnum != null) {
                builder.append("，").append(gradeCodeEnum.getName());
            }
        }

        String schoolClass = filterParams.getSchoolClass();
        if (StringUtils.isNotEmpty(schoolClass)) {
            builder.append("，").append(schoolClass);
        }

    }

    private String composeContrastParams(String title, Date startTime, Date endTime) {
        String startDate = DateFormatUtil.format(startTime, DateFormatUtil.FORMAT_ONLY_DATE);
        String endDate = DateFormatUtil.format(endTime, DateFormatUtil.FORMAT_ONLY_DATE);
        return String.format("%s，%s 至 %s", title, startDate, endDate);
    }

    /**
     * 按区域-获取幼儿园结果统计
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public KindergartenResultVO getKindergartenResult(Integer districtId, Integer noticeId) {
        return statDistrictService.getKindergartenResult(districtId,noticeId);

    }

    /**
     * 按区域-获取小学及以上结果统计
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public PrimarySchoolAndAboveResultVO getPrimarySchoolAndAboveResult(Integer districtId, Integer noticeId) {
        return statDistrictService.getPrimarySchoolAndAboveResult(districtId,noticeId);

    }

    /**
     * 按区域-获取合计详情
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public ScreeningResultStatisticDetailVO getScreeningResultTotalDetail(Integer districtId, Integer noticeId) {
        return statDistrictService.getScreeningResultTotalDetail(districtId,noticeId);

    }

    /**
     * 按学校-获取幼儿园结果统计
     *
     * @param districtId 区域ID
     * @param noticeId 通知ID
     * @param planId 计划ID
     */
    public SchoolKindergartenResultVO getSchoolKindergartenResult(Integer districtId, Integer noticeId,Integer planId) {
        return statSchoolService.getSchoolKindergartenResult(districtId,noticeId,planId);

    }

    /**
     * 按学校-获取小学及以上结果统计
     *
     * @param districtId 区域ID
     * @param noticeId 通知ID
     * @param planId 计划ID
     */
    public SchoolPrimarySchoolAndAboveResultVO getSchoolPrimarySchoolAndAboveResult(Integer districtId, Integer noticeId,Integer planId) {
        return statSchoolService.getSchoolPrimarySchoolAndAboveResult(districtId,noticeId,planId);

    }

    /**
     * 按学校-查看详情
     * @param statisticDetailBO 统计详情业务流转实体
     */
    public SchoolResultDetailVO getSchoolStatisticDetail(StatisticDetailBO statisticDetailBO) {
        return statFacade.getSchoolStatisticDetail(statisticDetailBO);
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
    public FocusObjectsStatisticVO getFocusObjectsStatisticVO(Integer districtId, List<District> districts, Set<Integer> districtIds) {
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
        DistrictAttentiveObjectsStatistic currentDistrictAttentiveObjectsStatistic = null;
        if (CollectionUtils.isNotEmpty(currentAttentiveObjectsStatistics)) {
            currentDistrictAttentiveObjectsStatistic = currentAttentiveObjectsStatistics.stream().findFirst().orElse(null);
        }
        //获取数据
        return FocusObjectsStatisticVO.getInstance(districtAttentiveObjectsStatistics, districtId,
                currentRangeName, districtIdNameMap,currentDistrictAttentiveObjectsStatistic);
    }



    /**
     * 获取复测报告信息
     *
     * @param planId
     * @param schoolId
     * @param qualityControllerName
     * @param qualityControllerCommander
     * @param screeningData
     * @return
     */
    public List<RescreenReportVO> getRescreenStatInfo(Integer planId, Integer schoolId,
                                                      String qualityControllerName, String qualityControllerCommander,
                                                      Long screeningData) {
        List<RescreenReportVO> rrvos = new ArrayList<>();

        List<StatRescreen> rescreens = statRescreenService.getByPlanAndSchool(planId, schoolId, Objects.nonNull(screeningData) ? new Date(screeningData) : null);
        if (CollectionUtils.isEmpty(rescreens)) {
            return rrvos;
        }

        // 获取筛查数据
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByPlanIdAndSchoolId(planId, schoolId);
        // 通过日期分组 yyyy-MM-dd
        Map<String, List<VisionScreeningResult>> groupDate = resultList.stream().collect(Collectors.groupingBy(s -> DateUtil.formatDate(s.getCreateTime())));

        String orgName = screeningOrganizationService.getNameById(rescreens.get(0).getScreeningOrgId());
        String schoolName = schoolService.getNameById(schoolId);
        rescreens.forEach(rescreen -> {
            RescreenReportVO rrvo = new RescreenReportVO();
            BeanUtils.copyProperties(rescreen, rrvo);
            rrvo.setQualityControllerName(qualityControllerName)
                    .setQualityControllerCommander(qualityControllerCommander)
                    .setOrgName(orgName)
                    .setSchoolName(schoolName)
                    .setCardList(generateReScreenResultCard(groupDate, rrvo.getScreeningTime(), qualityControllerName));
            rrvos.add(rrvo);
        });
        return rrvos;
    }

    /**
     * @param screeningTime
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
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
                        .setScreeningType(conclusion.getScreeningType())
                        .setSrcScreeningNoticeId(conclusion.getSrcScreeningNoticeId())
                        .setTaskId(conclusion.getTaskId())
                        .setPlanId(conclusion.getPlanId())
                        .setSchoolId(conclusion.getSchoolId())
                        .setScreeningTime(screeningTime);
                RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenInfoByTime);
                BeanUtils.copyProperties(rescreenStat, statRescreen);
                if (ScreeningTypeEnum.COMMON_DISEASE.getType().equals(conclusion.getScreeningType())) {
                    composePhysiqueReScreenConclusion(statRescreen, rescreenInfoByTime);
                }
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
        LocalDate endDate = startDate.plusDays(1L);
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setStartTime(startDate)
                .setEndTime(endDate)
                .setIsRescreen(true)
                .setIsValid(true)
                .setPlanId(planId)
                .setSchoolId(schoolId);
        return statConclusionService.listByQuery(query);
    }

    /**
     * 生成常见病复查信息
     *
     * @param statRescreen    复查
     * @param statConclusions 结论
     */
    private void composePhysiqueReScreenConclusion(StatRescreen statRescreen, List<StatConclusion> statConclusions) {
        int total = statConclusions.size();
        statRescreen.setPhysiqueRescreenNum((long) total);
        // 体格复查指数只有两个：身高和体重
        statRescreen.setPhysiqueIndexNum(2L);
        statRescreen.setPhysiqueRescreenItemNum(total * 2L);
        statRescreen.setPhysiqueIncorrectItemNum(statConclusions.stream().mapToLong(StatConclusion::getPhysiqueRescreenErrorNum).sum());
        statRescreen.setPhysiqueIncorrectRatio(convertToPercentage(statRescreen.getPhysiqueIncorrectItemNum() * 1f/ statRescreen.getPhysiqueRescreenItemNum()));
    }

    /**
     * 组装筛查复测卡
     *
     * @param groupDate     分组时间
     * @param screeningTime 筛查时间
     * @return 检测卡
     */
    private List<ReScreeningCardVO> generateReScreenResultCard(Map<String, List<VisionScreeningResult>> groupDate, Date screeningTime, String qualityControllerName) {
        // 获取日期当天的数据
        List<VisionScreeningResult> resultList = groupDate.get(DateUtil.formatDate(screeningTime));
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        List<ReScreeningCardVO> reScreeningCardVO = new ArrayList<>();

        // 获取复筛数据
        List<VisionScreeningResult> reScreenResults = resultList.stream().filter(VisionScreeningResult::getIsDoubleScreen).collect(Collectors.toList());

        // 获取初筛数据
        List<Integer> planStudentIdList = reScreenResults.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        List<VisionScreeningResult> firstResult = visionScreeningResultService.getFirstByPlanStudentIds(planStudentIdList);

        Map<Integer, VisionScreeningResult> screeningResultMap = firstResult.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        // 获取计划学生的commonDiseasesCode
        List<Integer> planStudentIds = firstResult.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        Map<Integer, String> commonDiseaseMap = screeningPlanSchoolStudentService.getByIds(planStudentIds).stream()
                .filter(s -> org.apache.commons.lang3.StringUtils.isNotBlank(s.getCommonDiseaseId()))
                .collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, ScreeningPlanSchoolStudent::getCommonDiseaseId));

        reScreenResults.forEach(reScreenResult -> {
            VisionScreeningResult first = screeningResultMap.get(reScreenResult.getScreeningPlanSchoolStudentId());
            reScreeningCardVO.add(ReScreenCardUtil.reScreenResultCard(first, reScreenResult, qualityControllerName, commonDiseaseMap.getOrDefault(first.getScreeningPlanSchoolStudentId(), org.apache.commons.lang3.StringUtils.EMPTY)));
        });
        return reScreeningCardVO;
    }

}
