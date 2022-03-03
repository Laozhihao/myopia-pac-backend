package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.api.management.domain.dto.MockStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.dto.PlanStudentRequestDTO;
import com.wupol.myopia.business.api.management.service.ManagementScreeningPlanBizService;
import com.wupol.myopia.business.api.management.service.ScreeningPlanSchoolStudentBizService;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
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
    /**
     * 新增
     *
     * @param screeningPlanDTO 新增参数
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isGovDeptUser() || user.isPlatformAdminUser()) {
            // 政府部门，无法新增计划
            throw new ValidationException("无权限");
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
            // 用户自己新建的筛查计划需设置districtIdmanagement/screeningNotice
            ScreeningOrganization organization = screeningOrganizationService.getById(user.getScreeningOrgId());
            screeningPlanDTO.setDistrictId(organization.getDistrictId());
        }
        screeningPlanDTO.setCreateUserId(user.getId());
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, true);
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
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningPlanDTO.setScreeningOrgId(screeningPlan.getScreeningOrgId());
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, false);
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
        return managementScreeningPlanBizService.getPage(query, page);
    }

    /**
     * 获取计划学校
     *
     * @param screeningPlanId 计划ID
     * @return List<ScreeningPlanSchoolVo>
     */
    @GetMapping("schools/{screeningPlanId}")
    public List<ScreeningPlanSchoolDTO> querySchoolsInfo(@PathVariable Integer screeningPlanId, String schoolName) {
        // 任务状态判断
        screeningExportService.validateExist(screeningPlanId);
        return screeningPlanSchoolService.getSchoolVoListsByPlanId(screeningPlanId, schoolName);
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
     * @param screeningPlanId 筛查计划ID
     * @param schoolId  学校ID
     * @param gradeId 年级ID
     * @param classId 班级ID
     * @return
     */
    @GetMapping("students/{screeningPlanId}/{schoolId}/{gradeId}/{classId}")
    public List<ScreeningPlanSchoolStudent> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId,
                                               @PathVariable Integer gradeId,@PathVariable Integer classId) {

        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId,
                gradeId, classId);
        return screeningPlanSchoolStudents;
    }

    /**
     * 获取计划学校的年级情况（有计划）
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/haveResult/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVO> getGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        return screeningPlanSchoolStudentFacadeService.getByPlanIdAndSchoolIdAndId(screeningPlanId, schoolId);
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
        List<Integer> havePlanSchoolIds = screeningPlanSchoolService.getHavePlanSchoolIds(null, screeningPlan.getId(), screeningPlan.getScreeningOrgId(), startTime, endTime);
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
    public void uploadScreeningStudents(MultipartFile file, @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //1. 发布成功后才能导入
        screeningExportService.validateExistAndAuthorize(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
        //2. 校验计划学校是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (Objects.isNull(planSchool)) {
            throw new ValidationException("该筛查学校不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        planStudentExcelImportService.importScreeningSchoolStudents(currentUser.getId(), file, screeningPlan, schoolId);
    }

    /**
     * 导出筛查计划的学生二维码信息
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
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(@NotNull(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr, @NotBlank(message = "查询类型不能为空") Boolean isSchoolClient, String planStudentName) {
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
    public void asyncGeneratorPDF(@NotNull(message = "计划Id不能为空")Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
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
    public PdfResponseDTO syncGeneratorPDF(@NotBlank(message = "计划Id不能为空")Integer planId, Integer schoolId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
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
    public List<ScreeningStudentDTO> getScreeningNoticeResultLists(@NotBlank(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIdStr, String planStudentName) {
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
        return screeningPlanSchoolService.getHaveResultSchool(screeningPlanId, schoolName);
    }

    /**
     * @Description: 学生筛查信息
     * @Param: [筛查计划ID, 筛查机构ID, 学校ID, 年级ID, 班级ID]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2021/12/29
     */
    @GetMapping("/plan/export/studentInfo")
    public ApiResult getScreeningPlanExportDoAndSync(Integer screeningPlanId, @RequestParam(defaultValue = "0") Integer screeningOrgId,
                                                  @RequestParam Integer schoolId,
                                                  @RequestParam(required = false) Integer gradeId,
                                                  @RequestParam(required = false) Integer classId) throws IOException {

        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(screeningPlanId)
                .setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setGradeId(gradeId)
                .setClassId(classId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());

        if (Objects.isNull(classId)){
            exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_VISION_SCREENING_RESULT_EXCEL_SERVICE);
            return ApiResult.success();
        }
        String path = exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.EXPORT_VISION_SCREENING_RESULT_EXCEL_SERVICE);
        return ApiResult.success(path);
    }

    /**
    * @Description: 学生筛查信息
    * @Param: [计划ID, 学生ID]
    * @return: java.lang.Object
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/12
    */
    @GetMapping("/getStudentEyeByStudentId")
    public ApiResult getStudentEyeByStudentId(@RequestParam Integer planId,@RequestParam Integer studentId) {
        List<Integer> studentIds = Collections.singletonList(studentId);
        List<VisionScreeningResult> visionScreeningResults =  visionScreeningResultService.getByStudentIds(planId,studentIds);
        if (visionScreeningResults.isEmpty()){
            return ApiResult.success();
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResults.get(0);

        return ApiResult.success(visionScreeningResult);

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
        List<Integer> studentIds =null;
        if (StringUtil.isNotEmpty(planStudentIds)&&!planStudentIds.equals("null")){
            studentIds = Arrays.stream(planStudentIds.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        }
        return screeningExportService.studentQRCodeFile(screeningPlanId, schoolId,gradeId,classId,studentIds,type);
    }

    /**
     * 告知书数据
     * @param screeningPlanId
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param planStudentIds
     * @return
     */
    @GetMapping("/student/notice")
    public Map<String, Object> studentNoticeData(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId,
                                                 @NotNull(message = "学校ID不能为空") Integer schoolId, Integer gradeId,
                                                 Integer classId, String planStudentIds,
                                                 boolean isSchoolClient) {
        List<Integer> studentIds =null;
        if (StringUtil.isNotEmpty(planStudentIds)&&!planStudentIds.equals("null")){
            studentIds = Arrays.stream(planStudentIds.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        }
        return screeningExportService.getNoticeData(screeningPlanId, schoolId,gradeId,classId,studentIds,isSchoolClient);
    }
}
