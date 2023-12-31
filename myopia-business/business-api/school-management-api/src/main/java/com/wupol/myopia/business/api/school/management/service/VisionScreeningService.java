package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.export.service.SysUtilService;
import com.wupol.myopia.business.aggregation.screening.constant.SchoolConstant;
import com.wupol.myopia.business.aggregation.screening.domain.builder.SchoolScreeningBizBuilder;
import com.wupol.myopia.business.aggregation.screening.domain.builder.SchoolScreeningPlanBuilder;
import com.wupol.myopia.business.aggregation.screening.domain.dto.SchoolScreeningPlanDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolStatisticVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.ScreeningStudentListVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.StudentScreeningDetailVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolBizService;
import com.wupol.myopia.business.aggregation.stat.facade.StatFacade;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.api.school.management.domain.builder.SchoolStatisticBuilder;
import com.wupol.myopia.business.api.school.management.domain.dto.AddScreeningStudentDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.ScreeningEndTimeDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.StudentListDTO;
import com.wupol.myopia.business.api.school.management.domain.vo.SchoolStatistic;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningStudentVO;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.facade.SchoolBizFacade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningBizTypeEnum;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.io.IOException;
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
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolBizFacade schoolBizFacade;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private StatFacade statFacade;
    @Resource
    private SchoolStudentBizService schoolStudentBizService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Resource
    private ExcelFacade excelFacade;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private DistrictService districtService;
    @Resource
    private SysUtilService sysUtilService;
    @Resource
    private SchoolClassService schoolClassService;
    @Autowired
    private ScreeningPlanSchoolBizService screeningPlanSchoolBizService;



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
            boolean isContains = SchoolConstant.OUR_SCHOOL.contains(screeningPlanListDTO.getScreeningOrgName());
            if (CollUtil.isEmpty(organizationList) && Objects.equals(Boolean.FALSE,isContains)){
                // 可以直接返回空
                return new Page<>();
            }
            Set<Integer> orgIds = organizationList.stream().map(ScreeningOrganization::getId).collect(Collectors.toSet());
            if (Objects.equals(Boolean.TRUE,isContains)){
                orgIds.add(schoolId);
            }
            screeningPlanListDTO.setScreeningOrgIds(Lists.newArrayList(orgIds));
        }

        screeningPlanListDTO.setSchoolId(schoolId);
        screeningPlanListDTO.setScreeningType(ScreeningTypeEnum.VISION.getType());
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
        List<SchoolStatisticVO> schoolStatisticList = getSchoolStatistic(planIds, schoolId);
        Map<Integer, SchoolStatisticVO> schoolStatisticMap = schoolStatisticList.stream().collect(Collectors.toMap(SchoolStatisticVO::getScreeningPlanId, Function.identity(), (o, n) -> o));

        // 筛查机构
        Map<Integer, ScreeningOrganization> orgMap = getScreeningOrganizationMap(organizationList, schoolPlanList);

        List<SchoolGrade> schoolGradeList = schoolGradeService.getBySchoolId(schoolId);
        boolean hasScreeningResults = CollUtil.isNotEmpty(schoolGradeList);

        Map<Integer, Integer> visionScreeningResultMap = getVisionScreeningResultMap(planIds);

        // 获取学校告知书
        School school = schoolService.getBySchoolId(schoolId);
        TwoTuple<NotificationConfig, String> notificationInfo = getNotificationInfo(school);
        schoolPlanList.forEach(schoolPlan -> {
            SchoolScreeningBizBuilder.setScreeningPlanInfo(schoolPlan, planMap ,visionScreeningResultMap);
            SchoolScreeningBizBuilder.setStatisticInfo(schoolPlan,schoolStatisticMap);
            SchoolScreeningBizBuilder.setOrgInfo(schoolPlan,orgMap);
            SchoolScreeningBizBuilder.setNotificationInfo(schoolPlan,notificationInfo);
            schoolPlan.setHasScreeningResults(hasScreeningResults);
            schoolPlan.setIsCanLink(Objects.equals(schoolPlan.getScreeningBizType(), ScreeningBizTypeEnum.INDEPENDENT.getType())
                    && Objects.equals(schoolPlan.getScreeningType(), ScreeningTypeEnum.VISION.getType())
                    && Objects.equals(schoolPlan.getSrcScreeningNoticeId(), 0));
        });
        responseDTO.setRecords(schoolPlanList);
        return responseDTO;
    }

    /**
     * 获取筛查机构
     *
     * @param organizationList
     * @param schoolPlanList
     */
    private Map<Integer, ScreeningOrganization> getScreeningOrganizationMap(List<ScreeningOrganization> organizationList, List<ScreeningListResponseDTO> schoolPlanList) {
        Map<Integer, ScreeningOrganization> orgMap = organizationList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
        if (CollUtil.isEmpty(organizationList)){
            Set<Integer> orgIds = schoolPlanList.stream()
                    .filter(screeningListResponseDTO -> Objects.equals(screeningListResponseDTO.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType()))
                    .map(ScreeningListResponseDTO::getScreeningOrgId).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(orgIds)){
                List<ScreeningOrganization> screeningOrganizationList = screeningOrganizationService.getByIds(orgIds);
                Map<Integer, ScreeningOrganization> collect = screeningOrganizationList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
                orgMap.putAll(collect);
            }
        }
        return orgMap;
    }

    /**
     * 获取筛查结果数
     * @param planIds 筛查计划ID集合
     */
    public Map<Integer, Integer> getVisionScreeningResultMap(List<Integer> planIds) {
        return visionScreeningResultService.getCountByPlanIds(planIds);
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
        Map<Integer, Integer> schoolStudentMap = schoolStudentService.getByStudentIdsAndSchoolId(studentIds, schoolId).stream()
                .collect(Collectors.toMap(SchoolStudent::getStudentId, SchoolStudent::getId));

        trackList.forEach(track -> SchoolScreeningBizBuilder.setStudentTrackWarningInfo(reportMap, schoolStudentMap, track));
        return responseDTO;
    }



    /**
     * 保存筛查计划（创建/编辑）
     * @param schoolScreeningPlanDTO 创建/编辑筛查计划对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveScreeningPlan(SchoolScreeningPlanDTO schoolScreeningPlanDTO, CurrentUser currentUser) {
        //创建和编辑标志
        Boolean isAdd = Objects.isNull(schoolScreeningPlanDTO.getId());
        School school = schoolService.getById(currentUser.getOrgId());
        validParam(schoolScreeningPlanDTO,currentUser,isAdd,school);

        //筛查计划
        ScreeningPlan screeningPlan = null;
        if (Objects.equals(isAdd,Boolean.FALSE)){
            screeningPlan = screeningPlanService.getById(schoolScreeningPlanDTO.getId());
        }
        screeningPlan = SchoolScreeningPlanBuilder.buildScreeningPlan(schoolScreeningPlanDTO, currentUser,school.getDistrictId(),screeningPlan);

        //筛查计划学校
        ScreeningPlanSchool screeningPlanSchool = getScreeningPlanSchool(schoolScreeningPlanDTO, school);
        //创建计划
        screeningPlanService.savePlanInfo(screeningPlan, screeningPlanSchool);
        //更新筛查通知状态为已读
        if (Objects.equals(isAdd,Boolean.TRUE) && !Objects.equals(screeningPlan.getScreeningTaskId(),CommonConst.DEFAULT_ID)){
            List<ScreeningNotice> screeningNoticeList = screeningNoticeService.getByScreeningTaskId(schoolScreeningPlanDTO.getScreeningTaskId(), Lists.newArrayList(ScreeningNotice.TYPE_SCHOOL));
            if (CollUtil.isEmpty(screeningNoticeList)) {
                throw new BusinessException("找不到对应任务通知");
            }
            ScreeningNotice screeningNotice = screeningNoticeList.get(0);
            screeningNoticeDeptOrgService.statusReadAndCreate(screeningNotice.getId(), currentUser.getOrgId(), screeningPlan.getId(), currentUser.getId());
        }
    }

    /**
     * 校验参数
     * @param schoolScreeningPlanDTO
     * @param currentUser
     */
    private void validParam(SchoolScreeningPlanDTO schoolScreeningPlanDTO, CurrentUser currentUser, Boolean isAdd, School school) {
        // 校验用户机构，政府部门，无法新增计划
        if (currentUser.isGovDeptUser()) {
            throw new ValidationException("无权限");
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(DateFormatUtil.parseDate(schoolScreeningPlanDTO.getStartTime(), SchoolConstant.START_TIME, DatePattern.NORM_DATETIME_PATTERN))) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }

        boolean checkIsCreated = screeningPlanService.checkIsCreated(schoolScreeningPlanDTO.getScreeningTaskId(), currentUser.getOrgId(), ScreeningOrgTypeEnum.SCHOOL.getType());
        if (Objects.equals(checkIsCreated,Boolean.TRUE) && Objects.equals(isAdd,Boolean.TRUE)){
            throw new BusinessException("筛查计划已创建");
        }
        screeningPlanSchoolBizService.checkYearAndTime(school.getDistrictId(), schoolScreeningPlanDTO.getYear(), schoolScreeningPlanDTO.getTime());

    }

    /**
     * 获取筛查计划学校
     * @param schoolScreeningPlanDTO 筛查计划参数
     * @param school 学校信息
     */
    private ScreeningPlanSchool getScreeningPlanSchool(SchoolScreeningPlanDTO schoolScreeningPlanDTO, School school){
        //筛查计划学校
        ScreeningPlanSchool screeningPlanSchoolDb=null;
        if (Objects.nonNull(schoolScreeningPlanDTO.getId())){
            screeningPlanSchoolDb = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(schoolScreeningPlanDTO.getId(),school.getId());
        }
        return SchoolScreeningPlanBuilder.buildScreeningPlanSchool(screeningPlanSchoolDb,school, schoolScreeningPlanDTO.getGradeIds());
    }

    /**
     * 获取筛查计划学校学生
     *
     * @param screeningPlanId 筛查计划ID
     * @param gradeIds        年级ID集合
     * @param school          学校信息
     */
    private List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudentInfo(Integer screeningPlanId, List<Integer> gradeIds, School school){
        List<SchoolStudent> schoolStudentList = schoolStudentService.listBySchoolIdAndGradeIds(school.getId(), gradeIds);
        return getScreeningPlanSchoolStudent(screeningPlanId,schoolStudentList,school);
    }

    /**
     * 获取筛查计划学校学生
     *
     * @param screeningPlanId   筛查计划ID
     * @param schoolStudentList 学校学生集合
     * @param school            学校信息
     */
    private List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudent(Integer screeningPlanId,List<SchoolStudent> schoolStudentList , School school){
        Set<Integer> gradeIds = schoolStudentList.stream().map(SchoolStudent::getGradeId).collect(Collectors.toSet());
        TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClassMap = schoolBizFacade.getSchoolGradeAndClass(Lists.newArrayList(gradeIds));
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList=null;
        // 刚创建计划的时候ID会为空
        if (Objects.nonNull(screeningPlanId)){
            screeningPlanSchoolStudentDbList = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(screeningPlanId, school.getId());
        }
        return ScreeningBizBuilder.getScreeningPlanSchoolStudentList(screeningPlanId, schoolStudentList, school, schoolGradeAndClassMap.getFirst(), screeningPlanSchoolStudentDbList);
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
    @Transactional(rollbackFor = Exception.class)
    public void releaseScreeningPlan(Integer screeningPlanId, CurrentUser currentUser) {
        //校验是否存在和权限
        ScreeningPlan screeningPlan = validateExistAndAuthorize(screeningPlanId,currentUser);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlan.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        //校验同一个学校相同时间段内只能创建1个筛查计划
        if (Objects.equals(screeningPlanService.checkTimeLegal(screeningPlan),Boolean.TRUE)){
            throw new BusinessException("学校该时间段已存在筛查计划");
        }
        //校验同一个学校下，筛查标题唯一性，要进行校验，标题不能相同。
        if (Objects.equals(screeningPlanService.checkTitleExist(screeningPlan),Boolean.TRUE)){
            throw new BusinessException("学校已存在相同标题筛查计划");
        }
        //新增计划学生
        School school = schoolService.getById(currentUser.getOrgId());
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId,school.getId());
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = getScreeningPlanSchoolStudentInfo(screeningPlanId, screeningGradeIds, school);
        screeningPlanSchoolStudentService.addScreeningStudent(planSchoolStudentList, screeningPlanId, screeningPlan.getSrcScreeningNoticeId(),screeningPlan.getScreeningTaskId());
        //更新计划状态为发布
        screeningPlan.setStudentNumbers(planSchoolStudentList.size());
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
            throw new BusinessException("该筛查机构无权限");
        }
        return screeningPlan;
    }

    /**
     * 分页获取筛查学生列表
     * @param studentListDTO 学生查询条件对象
     */
    public ScreeningStudentVO studentList(StudentListDTO studentListDTO) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = Wrappers.lambdaQuery(ScreeningPlanSchoolStudent.class)
                .eq(ScreeningPlanSchoolStudent::getSchoolId, studentListDTO.getSchoolId())
                .eq(ScreeningPlanSchoolStudent::getScreeningPlanId,studentListDTO.getScreeningPlanId())
                .like(StrUtil.isNotBlank(studentListDTO.getName()), ScreeningPlanSchoolStudent::getStudentName, studentListDTO.getName())
                .like(StrUtil.isNotBlank(studentListDTO.getSno()), ScreeningPlanSchoolStudent::getStudentNo, studentListDTO.getSno())
                .eq(Objects.nonNull(studentListDTO.getGradeId()), ScreeningPlanSchoolStudent::getGradeId, studentListDTO.getGradeId())
                .eq(Objects.nonNull(studentListDTO.getClassId()), ScreeningPlanSchoolStudent::getClassId, studentListDTO.getClassId());
        Page page = studentListDTO.toPage();
        IPage<ScreeningPlanSchoolStudent> schoolStudentPage = screeningPlanSchoolStudentService.page(page, queryWrapper);
        IPage<ScreeningStudentListVO> studentListVoPage = processScreeningStudentList(schoolStudentPage, studentListDTO.getSchoolId());

        ScreeningStudentVO screeningStudentVO = new ScreeningStudentVO();
        List<GradeInfoVO> gradeInfoList = schoolStudentBizService.getGradeInfo(studentListDTO.getScreeningPlanId(), CurrentUserUtil.getCurrentUser().getOrgId());
        screeningStudentVO.setHasScreeningStudent(gradeInfoList.stream().mapToInt(GradeInfoVO::getUnSyncStudentNum).sum() > 0);
        screeningStudentVO.setPageData(studentListVoPage);
        return screeningStudentVO;
    }

    /**
     * 处理筛查学生信息
     * @param schoolStudentPage 筛查学生分页对象
     */
    private IPage<ScreeningStudentListVO> processScreeningStudentList(IPage<ScreeningPlanSchoolStudent> schoolStudentPage, Integer schoolId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = schoolStudentPage.getRecords();
        IPage<ScreeningStudentListVO> screeningStudentListVoPage = new Page<>(schoolStudentPage.getCurrent(),schoolStudentPage.getSize());
        if (CollUtil.isEmpty(screeningPlanSchoolStudentList)){
            return screeningStudentListVoPage;
        }

        Set<Integer> gradeIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());
        TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClass = schoolBizFacade.getSchoolGradeAndClass(Lists.newArrayList(gradeIds));

        Set<Integer> studentIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getStudentId).collect(Collectors.toSet());
        List<SchoolStudent> schoolStudentList = schoolStudentService.getByStudentIdsAndSchoolId(Lists.newArrayList(studentIds), schoolId);
        Map<Integer, Integer> schoolStudentIdMap = schoolStudentList.stream().collect(Collectors.toMap(SchoolStudent::getStudentId, SchoolStudent::getId));

        Set<Integer> planSchoolStudentIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> resultList  = visionScreeningResultService.getByPlanStudentIds(Lists.newArrayList(planSchoolStudentIds));
        Map<Integer,VisionScreeningResult> visionScreeningResultMap = resultList.stream().filter(visionScreeningResult -> Boolean.FALSE.equals(visionScreeningResult.getIsDoubleScreen())).collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        List<ScreeningStudentListVO> screeningStudentListVOList = screeningPlanSchoolStudentList.stream()
                .map(screeningPlanSchoolStudent ->SchoolScreeningBizBuilder.getScreeningStudentListVO(schoolGradeAndClass, visionScreeningResultMap, screeningPlanSchoolStudent,schoolStudentIdMap)).collect(Collectors.toList());
        BeanUtil.copyProperties(schoolStudentPage,screeningStudentListVoPage);
        screeningStudentListVoPage.setRecords(screeningStudentListVOList);
        return screeningStudentListVoPage;
    }


    /**
     * 新增筛查学生
     * @param addScreeningStudentDTO 新增筛查学校对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void addScreeningStudent(AddScreeningStudentDTO addScreeningStudentDTO) {
        School school = schoolService.getById(addScreeningStudentDTO.getSchoolId());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = getScreeningPlanSchoolStudentInfo(addScreeningStudentDTO.getScreeningPlanId(), addScreeningStudentDTO.getGradeIds(), school);
        // 保存已勾选年级信息
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(addScreeningStudentDTO.getScreeningPlanId(), school.getId());
        changeScreeningGradeIds(addScreeningStudentDTO, screeningPlanSchool);
        screeningPlanSchoolService.saveOrUpdate(screeningPlanSchool);
        // 新增学生
        ScreeningPlan screeningPlan = screeningPlanService.getById(addScreeningStudentDTO.getScreeningPlanId());
        screeningPlanSchoolStudentService.addScreeningStudent(planSchoolStudentList,screeningPlan.getId(),screeningPlan.getSrcScreeningNoticeId(),screeningPlan.getScreeningTaskId());
    }

    /**
     * 修改筛查学校年级ID集合
     * @param addScreeningStudentDTO
     * @param screeningPlanSchool
     */
    private void changeScreeningGradeIds(AddScreeningStudentDTO addScreeningStudentDTO, ScreeningPlanSchool screeningPlanSchool) {
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        screeningGradeIds.addAll(addScreeningStudentDTO.getGradeIds());
        screeningGradeIds = screeningGradeIds.stream().distinct().collect(Collectors.toList());
        screeningPlanSchool.setScreeningGradeIds(CollUtil.join(screeningGradeIds, StrUtil.COMMA));
    }

    /**
     * 学生筛查详情
     * @param screeningPlanId 筛查计划ID
     * @param screeningPlanStudentId 筛查计划学生ID
     */
    public StudentScreeningDetailVO studentScreeningDetail(Integer screeningPlanId, Integer screeningPlanStudentId) {
        VisionScreeningResultDTO studentScreeningResultDetail = visionScreeningResultService.getStudentScreeningResultDetail(screeningPlanId, screeningPlanStudentId);
        return SchoolScreeningPlanBuilder.getStudentScreeningDetailVO(studentScreeningResultDetail);
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

    /**
     * 更新筛查结束时间
     * @param screeningEndTimeDTO 筛查结束时间参数
     */
    public void updateScreeningEndTime(ScreeningEndTimeDTO screeningEndTimeDTO) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningEndTimeDTO.getScreeningPlanId());
        Assert.isTrue(screeningPlan.getUpdateScreeningEndTimeStatus() == ScreeningPlan.NOT_CHANGED, "该计划已经增加过时间");

        ScreeningPlan plan = new ScreeningPlan()
                .setId(screeningEndTimeDTO.getScreeningPlanId())
                .setEndTime(DateFormatUtil.parseDate(screeningEndTimeDTO.getEndTime(),SchoolConstant.END_TIME,DatePattern.NORM_DATETIME_PATTERN))
                .setUpdateScreeningEndTimeStatus(ScreeningPlan.MODIFIED);
        screeningPlanService.updateById(plan);
    }

    /**
     * 获取学校结果统计分析
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     * @param type 学校类型
     */
    public SchoolStatistic getSchoolStatistic(Integer screeningPlanId, Integer schoolId, Integer type) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        TwoTuple<List<VisionScreeningResultStatistic>, List<CommonDiseaseScreeningResultStatistic>> realTimeSchoolStatisticList = statFacade.getRealTimeSchoolStatistics(screeningPlanId, schoolId);
        if (Objects.equals(screeningPlan.getScreeningType(),ScreeningTypeEnum.VISION.getType())){
            List<VisionScreeningResultStatistic> visionScreeningResultStatisticList = realTimeSchoolStatisticList.getFirst();
            return SchoolStatisticBuilder.buildVisionScreeningSchoolStatisticVO(visionScreeningResultStatisticList,type);
        }else {
            List<CommonDiseaseScreeningResultStatistic> commonDiseaseScreeningResultStatisticList = realTimeSchoolStatisticList.getSecond();
            return SchoolStatisticBuilder.buildCommonDiseaseScreeningResultStatisticVO(commonDiseaseScreeningResultStatisticList,type);
        }
    }

    /**
     * 获取学校统计信息
     * @param screeningPlanIds
     * @param schoolId
     */
    public List<SchoolStatisticVO> getSchoolStatistic(List<Integer> screeningPlanIds, Integer schoolId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByPlanIdsAndSchoolId(screeningPlanIds, schoolId,Boolean.FALSE);
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdsAndSchoolId(screeningPlanIds, schoolId,Boolean.FALSE);
        Map<Integer, List<ScreeningPlanSchoolStudent>> screeningPlanSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getScreeningPlanId));
        Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap = visionScreeningResultList.stream().collect(Collectors.groupingBy(VisionScreeningResult::getPlanId));
        return screeningPlanIds.stream().map(screeningPlanId->SchoolScreeningBizBuilder.buildSchoolStatistic(screeningPlanId,screeningPlanSchoolStudentMap,visionScreeningResultMap)).collect(Collectors.toList());
    }

    /**
     * 导出筛查数据
     * @param planId
     */
    public void getScreeningPlanExportData(Integer planId,CurrentUser currentUser) throws IOException, UtilException {

        // TODO：复用ExportPlanStudentDataExcelService导出逻辑
        Integer schoolId = currentUser.getOrgId();

        // 获取文件需显示的名称的学校前缀
        String exportFileNamePrefix = checkNotNullAndGetName(schoolService.getById(schoolId));
        List<StatConclusionExportDTO> statConclusionExportDTOs = statConclusionService.getExportVoByScreeningPlanIdAndSchoolId(planId, schoolId,null);
        if (CollectionUtils.isEmpty(statConclusionExportDTOs)) {
            throw new BusinessException("暂无筛查数据，无法导出");
        }

        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(statConclusionExportDTOs, StatConclusionExportDTO::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(statConclusionExportDTOs, StatConclusionExportDTO::getClassId);

        statConclusionExportDTOs.forEach(vo ->
                vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress()))
                        .setGradeName(gradeMap.getOrDefault(vo.getGradeId(), new SchoolGrade()).getName())
                        .setClassName(classMap.getOrDefault(vo.getClassId(), new SchoolClass()).getName()));
        String key = String.format(RedisConstant.FILE_EXPORT_PLAN_DATA, planId, 0, schoolId, currentUser.getId());
        checkIsExport(key);
        // 导出限制
        sysUtilService.isNoPlatformRepeatExport(String.format(RedisConstant.FILE_EXCEL_SCHOOL_PLAN, planId, schoolId, currentUser.getId()), key,null);
        // 获取文件需显示的名称
        excelFacade.generateVisionScreeningResult(currentUser.getId(), statConclusionExportDTOs, true, exportFileNamePrefix, key);
    }

    /**
     * 判空并获取名称
     *
     * @param object 类型
     * @return 名称
     */
    private <T extends HasName> String checkNotNullAndGetName(T object) {
        if (Objects.isNull(object)) {
            throw new BusinessException(String.format("未找到该%s", "学校"));
        }
        return object.getName();
    }

    /**
     * 是否正在导出
     *
     * @param key Key
     */
    private void checkIsExport(String key) {
        Object o = redisUtil.get(key);
        if (Objects.nonNull(o)) {
            throw new BusinessException("正在导出中，请勿重复导出");
        }
        //time: 60 * 60 * 24
        redisUtil.set(key, 1, 86400L);
    }



}

