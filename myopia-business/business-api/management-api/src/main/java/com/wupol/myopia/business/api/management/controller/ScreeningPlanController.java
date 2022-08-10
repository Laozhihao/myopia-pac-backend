package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.domain.UploadScreeningStudentVO;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.domain.dto.ScreeningQrCodeDTO;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolBizService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.api.management.domain.dto.MockStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.dto.PlanStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.dto.ReviewInformExportDataDTO;
import com.wupol.myopia.business.api.management.service.ManagementScreeningPlanBizService;
import com.wupol.myopia.business.api.management.service.ReviewInformService;
import com.wupol.myopia.business.api.management.service.QuestionnaireLoginService;
import com.wupol.myopia.business.api.management.service.ScreeningPlanSchoolStudentBizService;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.service.SchoolAdminService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 筛查计划相关接口
 *
 * @author Alix
 * date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningPlan")
@Slf4j
@Validated
public class ScreeningPlanController {

    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private ScreeningPlanSchoolBizService screeningPlanSchoolBizService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private PlanStudentExcelImportService planStudentExcelImportService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private SchoolAdminService schoolAdminService;
    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;
    @Autowired
    private ScreeningPlanSchoolStudentBizService screeningPlanSchoolStudentBizService;
    @Autowired
    private ExportStrategy exportStrategy;
    @Autowired
    private ScreeningPlanStudentBizService screeningPlanStudentBizService;
    @Autowired
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;
    @Autowired
    private ScreeningExportService screeningExportService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ReviewInformService reviewInformService;
    @Autowired
    private QuestionnaireLoginService questionnaireLoginService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;

    /**
     * 新增
     *
     * @param screeningPlanDTO 新增参数
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构，政府部门，无法新增计划
        if (user.isGovDeptUser()) {
            throw new ValidationException("无权限");
        }
        // 平台管理员，筛查机构ID必传
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningPlanDTO.getScreeningOrgId(), "筛查机构ID不能为空");
        }
        // 若为筛查人员或医生，只能发布自己机构的计划
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            screeningPlanDTO.setScreeningOrgId(user.getScreeningOrgId());
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlanDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        // 有传screeningTaskId时，需判断是否已创建且筛查任务是否有该筛查机构
        if (Objects.nonNull(screeningPlanDTO.getScreeningTaskId())) {
            if (screeningPlanService.checkIsCreated(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId())) {
                throw new ValidationException("筛查计划已创建");
            }
            ScreeningTaskOrg screeningTaskOrg = screeningTaskOrgService.getOne(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId());
            if (Objects.isNull(screeningTaskOrg)) {
                throw new ValidationException("筛查任务查无该机构");
            }
            ScreeningTask screeningTask = screeningTaskService.getById(screeningPlanDTO.getScreeningTaskId());
            screeningPlanDTO.setSrcScreeningNoticeId(screeningTask.getScreeningNoticeId()).setDistrictId(screeningTask.getDistrictId()).setGovDeptId(screeningTask.getGovDeptId());
        } else {
            // 用户自己新建的筛查计划需设置districtId
            ScreeningOrganization organization = screeningOrganizationService.getById(user.getScreeningOrgId());
            screeningPlanDTO.setDistrictId(organization.getDistrictId());
        }
        screeningPlanDTO.setCreateUserId(user.getId());
        screeningPlanService.saveOrUpdateWithSchools(user.getId(), screeningPlanDTO, true);
    }

    /**
     * 查看筛查计划
     *
     * @param id ID
     * @return Object
     */
    @GetMapping("{id}")
    public Object getInfo(@PathVariable Integer id) {
        return screeningPlanService.getById(id);
    }

    /**
     * 更新筛查计划
     *
     * @param screeningPlanDTO 更新参数
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        ScreeningPlan screeningPlan = screeningExportService.validateExistAndAuthorize(screeningPlanDTO.getId(), CommonConst.STATUS_RELEASE);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlanDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        screeningPlanDTO.setScreeningOrgId(screeningPlan.getScreeningOrgId());
        screeningPlanService.saveOrUpdateWithSchools(CurrentUserUtil.getCurrentUser().getId(), screeningPlanDTO, false);
    }

    /**
     * 分页查询计划列表
     *
     * @param query 查询参数
     * @param page  分页数据
     * @return IPage<ScreeningPlanVo>
     */
    @GetMapping("page")
    public IPage<ScreeningPlanPageDTO> queryInfo(PageRequest page, ScreeningPlanQueryDTO query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isGovDeptUser()) {
            throw new ValidationException("无权限");
        }
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            query.setScreeningOrgId(user.getScreeningOrgId());
        }
        query.setNeedFilterAbolishPlan(!user.isPlatformAdminUser());
        return managementScreeningPlanBizService.getPage(query, page);
    }

    /**
     * 获取计划学校（计划中没有导入学生显示）
     *
     * @param screeningPlanId 计划ID
     * @return List<ScreeningPlanSchoolVo>
     */
    @GetMapping("schools/{screeningPlanId}")
    public List<ScreeningPlanSchoolDTO> querySchoolsInfo(@PathVariable Integer screeningPlanId, String schoolName) {
        // 任务状态判断
        screeningExportService.validateExist(screeningPlanId);
        return screeningPlanSchoolBizService.getSchoolVoListsByPlanId(screeningPlanId, schoolName);
    }

    /**
     * 获取计划学校（计划中没有导入学生不显示）
     *
     * @param screeningPlanId 计划ID
     * @return List<ScreeningPlanSchoolVo>
     */
    @GetMapping("schools/haveStudents/{screeningPlanId}")
    public List<ScreeningPlanSchoolDTO> querySchoolsInfoWithPlan(@PathVariable Integer screeningPlanId, String schoolName) {
        // 任务状态判断
        screeningExportService.validateExist(screeningPlanId);
        return screeningPlanSchoolBizService.querySchoolsInfoInPlanHaveStudent(screeningPlanId, schoolName);
    }

    /**
     * 获取指定计划下学校信息
     *
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    @GetMapping("schools/{screeningPlanId}/{schoolId}")
    public ScreeningPlanSchool getPlanSchool(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        return screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVO> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        // 任务状态判断
        screeningExportService.validateExist(screeningPlanId);
        return screeningPlanSchoolStudentFacadeService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 获取计划学校-年级-班级 下的学生
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolId        学校ID
     * @param gradeId         年级ID
     * @param classId         班级ID
     * @return
     */
    @GetMapping("students/{screeningPlanId}/{schoolId}/{gradeId}/{classId}")
    public List<ScreeningPlanSchoolStudent> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId,
                                                            @PathVariable Integer gradeId, @PathVariable Integer classId) {

        return screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId, gradeId, classId);
    }

    /**
     * 获取计划学校的年级情况（有计划）
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/haveResult/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVO> getGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId, Boolean isKindergarten) {
        return screeningPlanSchoolStudentFacadeService.getByPlanIdAndSchoolIdAndId(screeningPlanId, schoolId, isKindergarten);
    }

    /**
     * 新增筛查学校
     *
     * @param screeningPlanId      筛查计划ID
     * @param screeningPlanSchools 新增的学校列表
     */
    @PostMapping("schools/{screeningPlanId}")
    public void addSchoolsInfo(@PathVariable Integer screeningPlanId, @RequestBody @Valid List<ScreeningPlanSchool> screeningPlanSchools) {
        if (CollectionUtils.isEmpty(screeningPlanSchools)) {
            return;
        }
        // 任务状态判断：已发布才能新增
        ScreeningPlan screeningPlan = screeningExportService.validateExistWithReleaseStatusAndReturn(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
        validateSchoolLegal(screeningPlan, screeningPlanSchools);
        screeningPlanSchoolService.saveOrUpdateBatchByPlanId(screeningPlanId, screeningPlan.getScreeningOrgId(), screeningPlanSchools);
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id 筛查计划ID
     */
    @DeleteMapping("{id}")
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        screeningExportService.validateExistWithReleaseStatusAndReturn(id, CommonConst.STATUS_RELEASE);
        screeningPlanService.removeWithSchools(CurrentUserUtil.getCurrentUser(), id);
    }

    /**
     * 发布
     *
     * @param id 筛查计划ID
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 已发布，直接返回
        ScreeningPlan screeningPlan = screeningExportService.validateExistAndAuthorize(id, CommonConst.STATUS_RELEASE);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlan.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        // 没有学校，直接报错
        List<ScreeningPlanSchool> schoolListsByPlanId = screeningPlanSchoolService.getSchoolListsByPlanId(id);
        if (CollectionUtils.isEmpty(schoolListsByPlanId)) {
            throw new ValidationException("无筛查的学校");
        }
        validateSchoolLegal(screeningPlan, schoolListsByPlanId);
        // 查询学校的userId
        List<SchoolAdmin> schoolAdmins = schoolAdminService.getBySchoolIds(schoolListsByPlanId.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList()));

        if (!CollectionUtils.isEmpty(schoolAdmins)) {
            // 为消息中心创建通知
            List<Integer> toUserIds = schoolAdmins.stream().map(SchoolAdmin::getUserId).collect(Collectors.toList());
            noticeService.batchCreateNotice(user.getId(), id, toUserIds, CommonConst.NOTICE_SCREENING_PLAN, screeningPlan.getTitle(), screeningPlan.getTitle(), screeningPlan.getStartTime(), screeningPlan.getEndTime());
        }
        screeningPlanService.release(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 校验学校是否可新增：如果该机构相同时间段内已有该学校，不能新增
     *
     * @param screeningPlan       筛查计划
     * @param schoolListsByPlanId 筛查计划中的学校
     */
    private void validateSchoolLegal(ScreeningPlan screeningPlan, List<ScreeningPlanSchool> schoolListsByPlanId) {
        // 学校是否可新增：如果该机构相同时间段内已有该学校，不能新增
        LocalDate startTime = com.wupol.framework.core.util.DateUtil.fromDate(screeningPlan.getStartTime());
        LocalDate endTime = com.wupol.framework.core.util.DateUtil.fromDate(screeningPlan.getEndTime());
        List<Integer> havePlanSchoolIds = screeningPlanSchoolService.getHavePlanSchoolIds(null, screeningPlan.getId(), screeningPlan.getScreeningOrgId(), startTime, endTime,screeningPlan.getScreeningType());
        if (schoolListsByPlanId.stream().anyMatch(screeningPlanSchool -> havePlanSchoolIds.contains(screeningPlanSchool.getSchoolId()))) {
            throw new ValidationException("该筛查机构相同时间段内计划已存在学校");
        }
    }

    /**
     * 分页查询筛查学生信息
     *
     * @param query 查询参数
     * @param page  分页数据
     * @return IPage<StudentDTO>
     */
    @GetMapping("students/page")
    public IPage<ScreeningStudentDTO> queryStudentInfos(PageRequest page, ScreeningStudentQueryDTO query) {
        screeningExportService.validateExistWithReleaseStatusAndReturn(query.getScreeningPlanId(), null);
        return screeningPlanSchoolStudentFacadeService.getPage(query, page);
    }

    /**
     * 查询筛查学生信息
     *
     * @param requestDTO 查询参数
     * @return IPage<StudentDTO>
     */
    @GetMapping("students/list")
    public List<ScreeningPlanSchoolStudent> queryPlanStudentList(PlanStudentRequestDTO requestDTO) {
        return screeningPlanSchoolStudentBizService.getListByRequest(requestDTO);
    }

    /**
     * 导入筛查计划的学生数据
     *
     * @param file            学生文件
     * @param screeningPlanId 筛查计划ID
     * @param schoolId        学校ID
     * @throws IOException IO异常
     */
    @PostMapping("/upload/{screeningPlanId}/{schoolId}")
    public UploadScreeningStudentVO uploadScreeningStudents(MultipartFile file, @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //1. 发布成功后才能导入
        screeningExportService.validateExistAndAuthorize(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
        //2. 校验计划学校是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (Objects.isNull(planSchool)) {
            throw new ValidationException("该筛查学校不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        return planStudentExcelImportService.importScreeningSchoolStudents(currentUser.getId(), file, screeningPlan, schoolId);
    }

    /**
     * 导出筛查计划的学生二维码信息
     * <p>
     * //TODO 当前这个二维码导出，在其他地方有使用，考虑是否删除
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @param type            1-二维码 2-VS666 3-学生编码二维码
     * @return pdf的URL
     */
    @GetMapping("/export/QRCode")
    public Map<String, String> downloadQRCodeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo, Integer type) {
        return screeningExportService.getQrCodeFile(schoolClassInfo, type);
    }


    /**
     * 导出筛查计划的学生告知书
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @return PDF的URL
     */
    @GetMapping("/export/notice")
    public Map<String, String> downloadNoticeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo) {
        return screeningExportService.getNoticeFile(schoolClassInfo, null);
    }


    /**
     * 创建虚拟学生
     *
     * @param requestDTO      请求入惨
     * @param screeningPlanId 计划Id
     * @param schoolId        学生Id
     */
    @PostMapping("/mock/student/{screeningPlanId}/{schoolId}")
    public void mockStudent(@RequestBody MockStudentRequestDTO requestDTO, @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        screeningPlanSchoolStudentBizService.initMockStudent(requestDTO, screeningPlanId, schoolId, currentUser);
    }

    /**
     * @param screeningPlanId
     * @param schoolId
     * @throws IOException
     */
    @GetMapping("/export/planStudent/{screeningPlanId}/{schoolId}")
    public void exportPlanStudent(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId, Integer gradeId) throws IOException {

        Assert.isTrue(Objects.nonNull(screeningPlanId), "计划Id不能为空");
        Assert.isTrue(Objects.nonNull(schoolId), "学校Id不能为空");

        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition().setApplyExportFileUserId(user.getId()).setSchoolId(schoolId).setPlanId(screeningPlanId).setGradeId(gradeId), ExportExcelServiceNameConstant.PLAN_STUDENT_SERVICE);
    }

    @PostMapping("/update/planStudent")
    public void updatePlanStudent(@RequestBody UpdatePlanStudentRequestDTO requestDTO) {
        screeningPlanStudentBizService.updatePlanStudent(requestDTO);
    }

    /**
     * 增加筛查时间
     * @param screeningPlanDTO
     */
    @PostMapping("/increased/screeningTime")
    public void updateScreeningEndTime(@RequestBody ScreeningPlanDTO screeningPlanDTO) {
        Assert.notNull(screeningPlanDTO.getId(), "计划Id不能为空");
        Assert.notNull(screeningPlanDTO.getEndTime(), "结束时间不能为空");
        if (!CurrentUserUtil.getCurrentUser().isPlatformAdminUser()){
            ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanDTO.getId());
            Assert.isTrue(screeningPlan.getUpdateScreeningEndTimeStatus() == ScreeningPlan.NOT_CHANGED, "该计划已经增加过时间");
        }
        ScreeningPlan plan = new ScreeningPlan().setId(screeningPlanDTO.getId()).setEndTime(screeningPlanDTO.getEndTime()).setUpdateScreeningEndTimeStatus(ScreeningPlan.MODIFIED);
        screeningPlanService.updateById(plan);
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     * @param isSchoolClient   是否学校端
     * @return List<ScreeningStudentDTO>
     */
    @GetMapping("screeningNoticeResult")
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(@NotNull(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr, @NotNull(message = "查询类型不能为空") Boolean isSchoolClient, String planStudentName) {
        return screeningPlanStudentBizService.getScreeningNoticeResultStudent(planId, schoolId, gradeId, classId, orgId, planStudentIdStr, isSchoolClient, planStudentName);
    }

    /**
     * 异步导出学生报告
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     */
    @GetMapping("screeningNoticeResult/asyncGeneratorPDF")
    public void asyncGeneratorPDF(@NotNull(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningPlanStudentBizService.asyncGeneratorPDF(planId, schoolId, gradeId, classId, orgId, planStudentIdStr, false, user.getId());
    }

    /**
     * 同步导出学生报告
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     */
    @GetMapping("screeningNoticeResult/syncGeneratorPDF")
    public PdfResponseDTO syncGeneratorPDF(@NotNull(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningPlanStudentBizService.syncGeneratorPDF(planId, schoolId, gradeId, classId, orgId, planStudentIdStr, false, user.getId());
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param planStudentIdStr 筛查学生Ids
     * @param planStudentName  筛查学生名称
     * @return List<ScreeningStudentDTO>
     */
    @GetMapping("screeningNoticeResult/list")
    public List<ScreeningStudentDTO> getScreeningNoticeResultLists(@NotNull(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIdStr, String planStudentName) {
        return screeningPlanStudentBizService.getScreeningStudentDTOS(planId, schoolId, gradeId, classId, planStudentIdStr, planStudentName);
    }

    /**
     * 获取计划学校
     *
     * @param screeningPlanId 计划ID
     * @return List<ScreeningPlanSchoolVo>
     */
    @GetMapping("schools/haveResult/{screeningPlanId}")
    public List<ScreeningPlanSchoolDTO> getHaveResultSchool(@PathVariable Integer screeningPlanId, String schoolName) {
        // 任务状态判断
        return screeningPlanSchoolBizService.getHaveResultSchool(screeningPlanId, schoolName);
    }

    /**
     * @Description: 学生筛查信息
     * @Param: [计划ID, 学生ID]
     * @return: java.lang.Object
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/12
     */
    @GetMapping("/getStudentEyeByStudentId")
    public ApiResult<VisionScreeningResultDTO> getStudentEyeByStudentId(@RequestParam Integer planId,@RequestParam Integer planStudentId) {
        return ApiResult.success(visionScreeningResultService.getStudentScreeningResultDetail(planId, planStudentId));
    }

    /**
     * 删除学生
     *
     * @param planStudentId 筛查学生Id
     */
    @DeleteMapping("/deleted/planStudent/{planStudentId}")
    public void deletedPlanStudentById(@PathVariable @NotNull(message = "筛查学生Id不能为空") Integer planStudentId) {
        screeningPlanStudentBizService.deletedPlanStudentById(planStudentId);
    }

    /**
     * 获取筛查计划的学生二维码数据
     *
     * @param screeningPlanId
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param planStudentIds
     * @param type
     * @return
     */
    @GetMapping("/student/QRCode")
    public Object studentQRCodeFile(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId,
                                    @NotNull(message = "学校ID不能为空") Integer schoolId, Integer gradeId, Integer classId, String planStudentIds,
                                    Integer type) {
        List<Integer> studentIds = null;
        if (StringUtil.isNotEmpty(planStudentIds) && !"null".equals(planStudentIds)) {
            studentIds = Arrays.stream(planStudentIds.split(StrUtil.COMMA)).map(Integer::valueOf).collect(Collectors.toList());
        }
        return screeningExportService.studentQRCodeFile(screeningPlanId, schoolId, gradeId, classId, studentIds, type);
    }

    /**
     * 告知书数据
     *
     * @param screeningPlanId
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param planStudentIds
     * @return
     */
    @GetMapping("/student/notice")
    public ScreeningQrCodeDTO studentNoticeData(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId,
                                                @NotNull(message = "学校ID不能为空") Integer schoolId, Integer gradeId,
                                                Integer classId, String planStudentIds,
                                                boolean isSchoolClient) {
        List<Integer> studentIds = null;
        if (StringUtil.isNotEmpty(planStudentIds) && !"null".equals(planStudentIds)) {
            studentIds = Arrays.stream(planStudentIds.split(StrUtil.COMMA)).map(Integer::valueOf).collect(Collectors.toList());
        }
        return screeningExportService.getNoticeData(screeningPlanId, schoolId, gradeId, classId, studentIds, isSchoolClient);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/all/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVO> getAllGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        // 任务状态判断
        screeningExportService.validateExist(screeningPlanId);
        return screeningPlanSchoolStudentFacadeService.getGradeByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 获取学校
     *
     * @param planId 筛查计划
     * @param orgId  机构Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    @GetMapping("/review/getSchool/{planId}/{orgId}")
    public List<ScreeningPlanSchoolStudent> reviewGetSchools(@PathVariable("planId") Integer planId,
                                                             @PathVariable("orgId") Integer orgId,
                                                             @RequestParam(required = false) String schoolName) {
        return reviewInformService.getReviewSchools(planId, orgId, schoolName);
    }


    /**
     * 获取复查年级班级
     *
     * @param planId   计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @return List<SchoolGradeVO>
     */
    @GetMapping("/review/getGrades/{planId}/{orgId}/{schoolId}")
    public List<SchoolGradeVO> reviewGetGrade(@PathVariable("planId") Integer planId,
                                              @PathVariable("orgId") Integer orgId,
                                              @PathVariable("schoolId") Integer schoolId) {
        return reviewInformService.getReviewGrade(planId, orgId, schoolId);
    }

    /**
     * 获取复查告知书数据
     *
     * @param planId   筛查计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return List<ReviewInformExportDataDTO>
     */
    @GetMapping("/review/getExportData")
    public List<ReviewInformExportDataDTO> getExportData(Integer planId, Integer orgId, Integer schoolId,
                                                         Integer gradeId, Integer classId) {
        return reviewInformService.getExportData(planId, orgId, schoolId, gradeId, classId);

    }

    /**
     * 导出复查通知书
     *
     * @param planId   筛查计划Id
     * @param orgId    机构Id
     * @param type     类型
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return ApiResult<String>
     */
    @GetMapping("/review/export")
    public ApiResult<String> reviewExport(@NotNull(message = "计划Id不能为空") Integer planId,
                                          @NotNull(message = "机构Id不能为空") Integer orgId,
                                          @NotNull(message = "typeId不能为空") Integer type,
                                          @NotNull(message = "学校Id不能为空") Integer schoolId, Integer gradeId, Integer classId) {

        // 如果是班级纬度的，同步导出
        if (ExportTypeConst.CLASS.equals(type)) {
            return ApiResult.success(reviewInformService.syncExportReview(planId, orgId, schoolId, gradeId, classId));
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        reviewInformService.asyncExportReview(planId, orgId, schoolId, gradeId, classId, type, currentUser.getId());
        return ApiResult.success();
    }

    /**
     * 获取学生信息
     * @param credentialNo
     * @return
     */
    @GetMapping("/student")
    public ApiResult getStudentByCredentialNo(@RequestParam("credentialNo") String credentialNo, @RequestParam("studentName") String studentName) {
        return this.questionnaireLoginService.getStudentByCredentialNo(credentialNo,studentName);
    }

    /**
     * 获取学校信息
     *
     * @param schoolNo
     * @return
     */
    @GetMapping("/school")
    public ApiResult getSchoolBySchoolNo(@RequestParam("schoolNo") String schoolNo, @RequestParam("password") String password) {
        return this.questionnaireLoginService.getSchoolBySchoolNo(schoolNo,password);
    }


    /**
     * 校验政府是否能够登录问卷系统
     *
     * @param orgId
     * @return
     */
    @GetMapping("/government")
    public ApiResult checkGovernmentLogin(@RequestParam("orgId") Integer orgId) {
        return this.questionnaireLoginService.checkGovernmentLogin(orgId);
    }

    /**
     * 作废计划
     *
     * @param planId 筛查计划ID
     * @return void
     **/
    @PutMapping("/abolish/{planId}")
    public void abolishScreeningPlan(@PathVariable("planId") Integer planId) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        Assert.isTrue(Objects.nonNull(screeningPlan) && !CommonConst.STATUS_ABOLISH.equals(screeningPlan.getReleaseStatus()), "无效筛查计划");
        // 1.身份校验，仅平台管理员可以作废计划
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Assert.isTrue(currentUser.isPlatformAdminUser(), "无操作权限，请联系平台管理员");
        // 2.更新计划状态为作废状态
        screeningPlanService.updateById(new ScreeningPlan().setId(planId).setReleaseStatus(CommonConst.STATUS_ABOLISH));
        // 3.筛查通知状态变更未创建
        if (Objects.nonNull(screeningPlan.getSrcScreeningNoticeId())) {
            screeningNoticeDeptOrgService.updateById(new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId()).setAcceptOrgId(screeningPlan.getScreeningOrgId()).setOperationStatus(CommonConst.STATUS_NOTICE_READ), currentUser.getId());
        }
    }

    /**
     * 删除计划学校
     *
     * @param planId    计划ID
     * @param schoolId  学校ID
     * @return void
     **/
    @DeleteMapping("/school/{planId}/{schoolId}")
    public void deletePlanSchool(@PathVariable("planId") Integer planId, @PathVariable("schoolId") Integer schoolId) {
        int count = visionScreeningResultService.count(new VisionScreeningResult().setPlanId(planId).setSchoolId(schoolId));
        Assert.isTrue(count > 0, "该学校已有筛查数据，不可删除！");
        screeningPlanSchoolService.remove(new ScreeningPlanSchool().setScreeningPlanId(planId).setSchoolId(schoolId));
        screeningPlanSchoolStudentService.remove(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
    }
}
