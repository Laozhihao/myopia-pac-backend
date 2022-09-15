package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.api.school.management.domain.builder.ScreeningPlanBuilder;
import com.wupol.myopia.business.api.school.management.domain.dto.AddScreeningStudentDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.StudentListDTO;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningStudentListVO;
import com.wupol.myopia.business.api.school.management.domain.vo.StudentScreeningDetailVO;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanListDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端-视力筛查
 *
 * @author Simple4H
 */
@Service
public class VisionScreeningService {

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;
    @Resource
    private SchoolFacade schoolFacade;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 获取视力筛查列表
     *
     * @param screeningPlanListDTO 筛查计划列表查询对象
     * @param schoolId    学校Id
     *
     * @return IPage<ScreeningListResponseDTO>
     */
    public IPage<ScreeningListResponseDTO> getList(ScreeningPlanListDTO screeningPlanListDTO, Integer schoolId) {

        List<ScreeningOrganization> organizationList=Lists.newArrayList();
        //筛查机构名称条件查询
        if (StrUtil.isNotBlank(screeningPlanListDTO.getScreeningOrgName())){
            organizationList = screeningOrganizationService.getByNameLike(screeningPlanListDTO.getScreeningOrgName(), Boolean.FALSE);
            if (CollUtil.isEmpty(organizationList)){
                // 可以直接返回空
                return new Page<>();
            }
            Set<Integer> orgIds = organizationList.stream().map(ScreeningOrganization::getId).collect(Collectors.toSet());
            screeningPlanListDTO.setScreeningOrgIds(Lists.newArrayList(orgIds));
        }

        screeningPlanListDTO.setSchoolId(schoolId);
        IPage<ScreeningListResponseDTO> responseDTO = screeningPlanSchoolService.listByCondition(screeningPlanListDTO);

        return screeningListResponseResult(responseDTO,organizationList,schoolId);
    }

    /**
     * 筛查列表结果处理
     * @param responseDTO 筛查列表结果
     * @param organizationList 机构信息集合
     * @param schoolId 学校ID
     */
    private IPage<ScreeningListResponseDTO> screeningListResponseResult(IPage<ScreeningListResponseDTO> responseDTO,List<ScreeningOrganization> organizationList,Integer schoolId){
        List<ScreeningListResponseDTO> schoolPlanList = responseDTO.getRecords();
        if (CollUtil.isEmpty(schoolPlanList)){
            return responseDTO;
        }

        // 获取筛查计划
        List<Integer> planIds = schoolPlanList.stream().map(ScreeningListResponseDTO::getPlanId).collect(Collectors.toList());
        List<ScreeningPlan> screeningPlans = screeningPlanService.listByIds(planIds);
        Map<Integer, ScreeningPlan> planMap = screeningPlans.stream()
                .filter(s->Objects.equals(s.getScreeningType(), ScreeningTypeEnum.VISION.getType()))
                .collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));

        // 获取统计信息
        List<SchoolVisionStatistic> statisticList = schoolVisionStatisticService.getByPlanIdsAndSchoolId(planIds, schoolId);
        Map<Integer, SchoolVisionStatistic> schoolStatisticMap = statisticList.stream().collect(Collectors.toMap(SchoolVisionStatistic::getScreeningPlanId, Function.identity(), (o, n) -> o));

        // 筛查机构
        Map<Integer, ScreeningOrganization> orgMap = organizationList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));

        // 获取学校告知书
        School school = schoolService.getBySchoolId(schoolId);
        TwoTuple<NotificationConfig, String> notificationInfo = getNotificationInfo(school);
        schoolPlanList.forEach(schoolPlan -> {
            setScreeningPlanInfo(schoolPlan, planMap);
            setStatisticInfo(schoolPlan,schoolStatisticMap);
            setOrgInfo(schoolPlan,orgMap);
            setNotificationInfo(schoolPlan,notificationInfo);
        });

        responseDTO.setRecords(schoolPlanList);
        return responseDTO;
    }

    /**
     * 设置告知书配置
     * @param responseDTO 返回对象
     * @param notificationInfo 告知书配置
     */
    private void setNotificationInfo(ScreeningListResponseDTO responseDTO,TwoTuple<NotificationConfig, String> notificationInfo) {
        if (Objects.isNull(notificationInfo)){
            return;
        }
        // 设置告知书配置
        responseDTO.setNotificationConfig(notificationInfo.getFirst());
        responseDTO.setQrCodeFileUrl(notificationInfo.getSecond());
    }

    /**
     * 获取学校告知书配置信息
     * @param school 学校对象
     */
    private TwoTuple<NotificationConfig,String> getNotificationInfo(School school) {
        TwoTuple<NotificationConfig, String> twoTuple = TwoTuple.of(null,null);
        // 设置告知书配置
        NotificationConfig notificationConfig = school.getNotificationConfig();
        if (Objects.nonNull(notificationConfig)) {
            twoTuple.setFirst(notificationConfig);
            // 设置图片
            Integer qrCodeFileId = notificationConfig.getQrCodeFileId();
            if (Objects.nonNull(qrCodeFileId)) {
                twoTuple.setSecond(resourceFileService.getResourcePath(qrCodeFileId));
            }
        } else {
            notificationConfig = new NotificationConfig();
            notificationConfig.setTitle("学生视力筛查告家长书");
            notificationConfig.setCall("亲爱的家长");
            notificationConfig.setGreetings("您好！");
            notificationConfig.setSubTitle(school.getName());
            twoTuple.setFirst(notificationConfig);
        }
        return twoTuple;
    }


    /**
     * 设置机构信息
     * @param responseDTO 返回对象
     * @param orgMap 机构信息集合
     */
    private void setOrgInfo(ScreeningListResponseDTO responseDTO,Map<Integer, ScreeningOrganization> orgMap) {
        ScreeningOrganization screeningOrganization = orgMap.get(responseDTO.getScreeningOrgId());
        if (Objects.isNull(screeningOrganization)){
            return;
        }
        responseDTO.setScreeningOrgName(screeningOrganization.getName());
        responseDTO.setQrCodeConfig(screeningOrganization.getQrCodeConfig());
    }

    /**
     * 设置统计信息
     * @param responseDTO 返回对象
     * @param schoolStatisticMap 统计信息集合
     */
    private void setStatisticInfo(ScreeningListResponseDTO responseDTO, Map<Integer, SchoolVisionStatistic> schoolStatisticMap) {
        SchoolVisionStatistic schoolVisionStatistic = schoolStatisticMap.get(responseDTO.getPlanId());
        if (Objects.nonNull(schoolVisionStatistic)) {
            responseDTO.setSchoolStatisticId(schoolVisionStatistic.getId());
            responseDTO.setPlanScreeningNumbers(schoolVisionStatistic.getPlanScreeningNumbers());
            responseDTO.setRealScreeningNumbers(schoolVisionStatistic.getRealScreeningNumbers());
        } else {
            responseDTO.setRealScreeningNumbers(0);
        }
    }

    /**
     * 设置筛查计划信息
     * @param responseDTO 返回对象
     * @param planMap 筛查计划集合
     */
    private void setScreeningPlanInfo(ScreeningListResponseDTO responseDTO, Map<Integer, ScreeningPlan> planMap) {
        ScreeningPlan screeningPlan = planMap.get(responseDTO.getPlanId());
        if (Objects.isNull(screeningPlan)) {
            return;
        }
        responseDTO.setTitle(screeningPlan.getTitle());
        responseDTO.setStartTime(screeningPlan.getStartTime());
        responseDTO.setEndTime(screeningPlan.getEndTime());
        responseDTO.setReleaseStatus(screeningPlan.getReleaseStatus());
        responseDTO.setScreeningStatus(ScreeningOrganizationService.getScreeningStatus(screeningPlan.getStartTime(), screeningPlan.getEndTime(), screeningPlan.getReleaseStatus()));
        responseDTO.setReleaseTime(screeningPlan.getReleaseTime());
        responseDTO.setContent(screeningPlan.getContent());
    }

    /**
     * 获取学生跟踪预警列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @param schoolId    学校Id
     *
     * @return IPage<StudentTrackWarningResponseDTO>
     */
    public IPage<StudentTrackWarningResponseDTO> getTrackList(PageRequest pageRequest, StudentTrackWarningRequestDTO requestDTO, Integer schoolId) {
        IPage<StudentTrackWarningResponseDTO> responseDTO = statConclusionService.getTrackList(pageRequest, requestDTO, schoolId);
        List<StudentTrackWarningResponseDTO> trackList = responseDTO.getRecords();
        if (CollectionUtils.isEmpty(trackList)) {
            return responseDTO;
        }
        List<Integer> studentIds = trackList.stream().map(StudentTrackWarningResponseDTO::getStudentId).collect(Collectors.toList());
        List<Integer> reportIds = trackList.stream().map(StudentTrackWarningResponseDTO::getReportId).collect(Collectors.toList());

        Map<Integer, MedicalReport> reportMap = medicalReportService.getByIds(reportIds).stream()
                .collect(Collectors.toMap(MedicalReport::getId, Function.identity()));

        // 学校端学生
        Map<Integer, Integer> schoolStudentMap = schoolStudentService.getByStudentIds(studentIds, schoolId).stream()
                .collect(Collectors.toMap(SchoolStudent::getStudentId, SchoolStudent::getId));

        trackList.forEach(track -> {
            track.setSchoolStudentId(schoolStudentMap.get(track.getStudentId()));
            track.setIsBindMp(track.getIsBindMp());
            if (Objects.nonNull(track.getReportId())) {
                MedicalReport report = reportMap.get(track.getReportId());
                track.setIsReview(true);
                track.setVisitResult(report.getMedicalContent());
                track.setGlassesSuggest(report.getGlassesSituation());
            }
        });
        return responseDTO;
    }

    /**
     * 保存筛查计划（创建/编辑）
     * @param screeningPlanDTO 创建/编辑筛查计划对象
     */
    public void saveScreeningPlan(ScreeningPlanDTO screeningPlanDTO, CurrentUser currentUser) {
        // 校验用户机构，政府部门，无法新增计划
        if (currentUser.isGovDeptUser()) {
            throw new ValidationException("无权限");
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlanDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        //筛查计划
        School school = schoolService.getById(currentUser.getOrgId());
        ScreeningPlan screeningPlan = ScreeningPlanBuilder.buildScreeningPlan(screeningPlanDTO, currentUser,school.getDistrictId());

        //筛查计划学校
        ScreeningPlanSchool screeningPlanSchool = getScreeningPlanSchool(screeningPlanDTO, school);

        //筛查学生
        TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> twoTuple = getScreeningPlanSchoolStudentInfo(screeningPlanDTO,school);

        screeningPlan.setStudentNumbers(twoTuple.getFirst().size());

        screeningPlanService.savePlanInfo(screeningPlan,screeningPlanSchool,twoTuple);
    }

    /**
     * 获取筛查计划学校
     * @param screeningPlanDTO 筛查计划参数
     * @param school 学校信息
     */
    private ScreeningPlanSchool getScreeningPlanSchool(ScreeningPlanDTO screeningPlanDTO,School school){
        //筛查计划学校
        ScreeningPlanSchool screeningPlanSchoolDb=null;
        if (Objects.nonNull(screeningPlanDTO.getId())){
            screeningPlanSchoolDb = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanDTO.getId(),school.getId());
        }
        return ScreeningPlanBuilder.buildScreeningPlanSchool(screeningPlanSchoolDb,school);
    }

    /**
     * 获取筛查计划学校学生
     * @param screeningPlanDTO 筛查计划参数
     * @param school 学校信息
     */
    private TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> getScreeningPlanSchoolStudentInfo(ScreeningPlanDTO screeningPlanDTO, School school){
        List<Integer> gradeIds = screeningPlanDTO.getGradeIds();
        List<SchoolStudent> schoolStudentList = schoolStudentService.listBySchoolIdAndGradeIds(school.getId(), gradeIds);
        TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClassMap = schoolFacade.getSchoolGradeAndClass(gradeIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList=null;
        if (Objects.nonNull(screeningPlanDTO.getId())){
            screeningPlanSchoolStudentDbList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanDTO.getId(),Boolean.FALSE);
        }
        return ScreeningPlanBuilder.getScreeningPlanSchoolStudentList(schoolStudentList, school, schoolGradeAndClassMap.getFirst(), schoolGradeAndClassMap.getSecond(), screeningPlanSchoolStudentDbList);
    }


    /**
     * 删除筛查计划
     * @param screeningPlanId 筛查计划ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteScreeningPlan(Integer screeningPlanId) {
        screeningPlanSchoolStudentService.remove(new ScreeningPlanSchoolStudent().setScreeningPlanId(screeningPlanId));
        screeningPlanSchoolService.remove(new ScreeningPlanSchool().setScreeningPlanId(screeningPlanId));
        screeningPlanService.remove(new ScreeningPlan().setId(screeningPlanId));
    }

    /**
     * 发布筛查计划
     * @param screeningPlanId 筛查计划ID
     */
    public void releaseScreeningPlan(Integer screeningPlanId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //校验是否存在和权限
        ScreeningPlan screeningPlan = validateExistAndAuthorize(screeningPlanId,currentUser);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlan.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        screeningPlanService.release(screeningPlan, currentUser);
    }

    /**
     * 校验是否存在和权限
     * @param screeningPlanId 筛查计划
     * @param user 当前用户
     */
    private ScreeningPlan validateExistAndAuthorize(Integer screeningPlanId,CurrentUser user) {
        // 校验用户机构
        if (user.isGovDeptUser()) {
            // 政府部门，无法新增修改计划
            throw new ValidationException("无权限");
        }
        ScreeningPlan screeningPlan = validateExist(screeningPlanId);
        if (!(Objects.equals(screeningPlan.getScreeningOrgType(), ScreeningOrgTypeEnum.SCHOOL.getType()) && Objects.equals(user.getOrgId(),screeningPlan.getScreeningOrgId()))){
            throw new BusinessException("无该筛查机构权限");
        }
        return screeningPlan;
    }

    /**
     * 分页获取筛查学生列表
     * @param studentListDTO 学生查询条件对象
     */
    public IPage<ScreeningStudentListVO> studentList(StudentListDTO studentListDTO) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = Wrappers.lambdaQuery(ScreeningPlanSchoolStudent.class)
                .eq(ScreeningPlanSchoolStudent::getSchoolId, studentListDTO.getSchoolId())
                .like(StrUtil.isNotBlank(studentListDTO.getName()), ScreeningPlanSchoolStudent::getStudentName, studentListDTO.getName())
                .like(StrUtil.isNotBlank(studentListDTO.getSno()), ScreeningPlanSchoolStudent::getStudentNo, studentListDTO.getSno())
                .eq(Objects.nonNull(studentListDTO.getGradeId()), ScreeningPlanSchoolStudent::getGradeId, studentListDTO.getGradeId())
                .eq(Objects.nonNull(studentListDTO.getClassId()), ScreeningPlanSchoolStudent::getClassId, studentListDTO.getClassId());
        Page page = studentListDTO.toPage();
        IPage<SchoolStudent> schoolStudentPage = screeningPlanSchoolStudentService.page(page, queryWrapper);
        List<SchoolStudent> records = schoolStudentPage.getRecords();
        return null;
    }

    /**
     * 新增筛查学生
     * @param addScreeningStudentDTO 新增筛查学校对象
     */
    public void addScreeningStudent(AddScreeningStudentDTO addScreeningStudentDTO) {

    }

    /**
     * 学生筛查详情
     * @param screeningPlanId 筛查计划ID
     * @param screeningPlanStudentId 筛查计划学生ID
     */
    public StudentScreeningDetailVO studentScreeningDetail(Integer screeningPlanId, Integer screeningPlanStudentId) {

        return null;
    }

    /**
     * 校验存在和是否发布
     * @param screeningPlanId 筛查计划ID
     */
    public void validateExistWithReleaseStatus(Integer screeningPlanId) {
        ScreeningPlan screeningPlan = validateExist(screeningPlanId);
        Integer taskStatus = screeningPlan.getReleaseStatus();
        if (Objects.equals(CommonConst.STATUS_RELEASE,taskStatus)) {
            throw new BusinessException("该筛查计划已发布");
        }
    }

    /**
     * 校验筛查计划是否存在
     * @param screeningPlanId 筛查计划ID
     */
    private ScreeningPlan validateExist(Integer screeningPlanId) {
        if (Objects.isNull(screeningPlanId)){
            throw new BusinessException("筛查计划ID不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("筛查计划不存在");
        }
        return screeningPlan;

    }
}

