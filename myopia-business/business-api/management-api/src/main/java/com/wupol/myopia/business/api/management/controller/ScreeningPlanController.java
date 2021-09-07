package com.wupol.myopia.business.api.management.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.framework.utils.FreemarkerUtil;
import com.wupol.framework.utils.PdfUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.constant.QrCodeConstant;
import com.wupol.myopia.business.api.management.domain.dto.MockStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.vo.SchoolGradeVO;
import com.wupol.myopia.business.api.management.service.ManagementScreeningPlanBizService;
import com.wupol.myopia.business.api.management.service.ScreeningPlanSchoolStudentBizService;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolAdminService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.PDFTemplateConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
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
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private SchoolAdminService schoolAdminService;
    @Autowired
    private ManagementScreeningPlanBizService managementScreeningPlanBizService;
    @Autowired
    private ScreeningPlanSchoolStudentBizService screeningPlanSchoolStudentBizService;

    @Value("${server.host}")
    private String hostUrl;

    /**
     * 默认文件Id
     */
    private final static Integer DEFAULT_FILE_ID = -1;

    /**
     * 默认图片路径
     */
    private final static String DEFAULT_IMAGE_PATH = "/image/wechat_mp_qrcode.png";

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
        if (user.isScreeningUser()) {
            // 筛查机构人员，需校验是否同机构
            Assert.isTrue(user.getOrgId().equals(screeningPlanDTO.getScreeningOrgId()), "无该筛查机构权限");
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
            ScreeningOrganization organization = screeningOrganizationService.getById(user.getOrgId());
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
        ScreeningPlan screeningPlan = validateExistAndAuthorize(screeningPlanDTO.getId(), CommonConst.STATUS_RELEASE);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningPlanDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningPlanDTO.setScreeningOrgId(screeningPlan.getScreeningOrgId());
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, false);
    }

    /**
     * 校验计划是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningPlanId 筛查计划ID
     * @param releaseStatus   发布状态
     */
    private ScreeningPlan validateExistAndAuthorize(Integer screeningPlanId, Integer releaseStatus) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isGovDeptUser()) {
            // 政府部门，无法新增修改计划
            throw new ValidationException("无权限");
        }
        ScreeningPlan screeningPlan = validateExistWithReleaseStatusAndReturn(screeningPlanId, releaseStatus);
        if (user.isScreeningUser()) {
            // 筛查机构人员，需校验是否同机构
            Assert.isTrue(user.getOrgId().equals(screeningPlan.getScreeningOrgId()), "无该筛查机构权限");
        }
        return screeningPlan;
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     * 返回该筛查计划
     *
     * @param id            筛查计划ID
     * @param releaseStatus 发布状态
     * @return 筛查计划
     */
    private ScreeningPlan validateExistWithReleaseStatusAndReturn(Integer id, Integer releaseStatus) {
        ScreeningPlan screeningPlan = validateExist(id);
        Integer taskStatus = screeningPlan.getReleaseStatus();
        if (Objects.nonNull(releaseStatus) && releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该计划%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
        return screeningPlan;
    }

    /**
     * 校验筛查计划是否存在
     *
     * @param id 筛查计划ID
     * @return 筛查计划
     */
    private ScreeningPlan validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(id);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("查无该计划");
        }
        return screeningPlan;
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
        if (user.isScreeningUser()) {
            query.setScreeningOrgId(user.getOrgId());
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
    public List<ScreeningPlanSchoolDTO> querySchoolsInfo(@PathVariable Integer screeningPlanId) {
        // 任务状态判断
        validateExist(screeningPlanId);
        return screeningPlanSchoolService.getSchoolVoListsByPlanId(screeningPlanId);
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
        validateExist(screeningPlanId);
        return screeningPlanSchoolStudentBizService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
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
        ScreeningPlan screeningPlan = validateExistWithReleaseStatusAndReturn(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
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
        validateExistWithReleaseStatusAndReturn(id, CommonConst.STATUS_RELEASE);
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
        ScreeningPlan screeningPlan = validateExistAndAuthorize(id, CommonConst.STATUS_RELEASE);
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
            noticeService.batchCreateScreeningNotice(user.getId(), id, toUserIds, CommonConst.NOTICE_SCREENING_PLAN, screeningPlan.getTitle(), screeningPlan.getTitle(), screeningPlan.getStartTime(), screeningPlan.getEndTime());
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
        validateExistWithReleaseStatusAndReturn(query.getScreeningPlanId(), null);
        return screeningPlanSchoolStudentBizService.getPage(query, page);
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
        validateExistAndAuthorize(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
        //2. 校验计划学校是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (Objects.isNull(planSchool)) {
            throw new ValidationException("该筛查学校不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        excelFacade.importScreeningSchoolStudents(currentUser.getId(), file, screeningPlan, schoolId);
    }

    /**
     * 导出筛查计划的学生二维码信息
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @param type            1-二维码 2-VS666
     * @return pdf的URL
     */
    @GetMapping("/export/QRCode")
    public Map<String, String> downloadQRCodeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo, Integer type) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-二维码", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));
            List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getScreeningPlanId(), schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
            students.forEach(student -> {
                student.setGenderDesc(GenderEnum.getName(student.getGender()));
                String content;
                if (Objects.isNull(type) || type.equals(CommonConst.EXPORT_QRCODE)) {
                    content = String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, student.getId());
                } else {
                    content = setVs666QrCodeRule(student);
                }
                student.setQrCodeUrl(QrCodeUtil.generateAsBase64(content, config, "jpg"));
            });
            // 3. 处理pdf报告参数
            Map<String, Object> models = new HashMap<>(16);
            models.put("students", students);
            models.put("classDisplay", classDisplay);
            // 4. 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
            File file = PdfUtil.generatePdfFromContent(FreemarkerUtil.generateHtmlString(PDFTemplateConst.QRCODE_TEMPLATE_PATH, models), fileName);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.getPdfUrl(file.getName(), file));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException("生成PDF文件失败", e);
        }
    }

    /**
     * 获取VS666格式所需要的二维码
     *
     * @param student 学生信息
     * @return 二维码
     */
    private String setVs666QrCodeRule(ScreeningStudentDTO student) {
        return String.format(QrCodeConstant.VS666_QR_CODE_CONTENT_FORMAT_RULE,
                String.format(QrCodeConstant.GENERATE_VS666_ID, student.getPlanId(), student.getPlanStudentId()),
                student.getName(),
                GenderEnum.getEnGenderDesc(student.getGender()),
                student.getAge(),
                StringUtils.getDefaultIfBlank(student.getParentPhone(), "null"),
                StringUtils.getDefaultIfBlank(student.getSchoolName(), "null"),
                StringUtils.isEmpty(student.getGradeName()) ? "null" : student.getGradeName() + student.getClassName(),
                StringUtils.getDefaultIfBlank(student.getIdCard(), "null"));
    }

    /**
     * 导出筛查计划的学生告知书
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @return PDF的URL
     */
    @GetMapping("/export/notice")
    public Map<String, String> downloadNoticeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            School school = schoolService.getById(schoolClassInfo.getSchoolId());
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-告知书", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));
            ScreeningPlan plan = screeningPlanService.getById(schoolClassInfo.getScreeningPlanId());
            ScreeningOrgResponseDTO screeningOrganization = screeningOrganizationService.getScreeningOrgDetails(plan.getScreeningOrgId());
            List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getScreeningPlanId(), schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
            students.forEach(student -> {
                student.setQrCodeUrl(QrCodeUtil.generateAsBase64(String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, student.getId()), config, "jpg"));
                student.setGenderDesc(GenderEnum.getName(student.getGender()));
            });
            Map<String, Object> models = new HashMap<>(16);
            models.put("screeningOrgConfigs", screeningOrganization.getNotificationConfig());
            models.put("students", students);
            models.put("classDisplay", classDisplay);
            models.put("schoolName", school.getName());
            if (Objects.nonNull(screeningOrganization.getNotificationConfig())
                    && Objects.nonNull(screeningOrganization.getNotificationConfig().getQrCodeFileId())
                    && !screeningOrganization.getNotificationConfig().getQrCodeFileId().equals(DEFAULT_FILE_ID)
            ) {
                models.put("qrCodeFile", resourceFileService.getResourcePath(screeningOrganization.getNotificationConfig().getQrCodeFileId()));
            } else {
                models.put("qrCodeFile", DEFAULT_IMAGE_PATH);
            }
            // 3. 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
            File file = PdfUtil.generatePdfFromContent(FreemarkerUtil.generateHtmlString(PDFTemplateConst.NOTICE_TEMPLATE_PATH, models), hostUrl, fileName);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.getPdfUrl(file.getName(), file));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException("生成PDF文件失败", e);
        }
    }

    @PostMapping("/mock/student/{screeningPlanId}/{schoolId}")
    public void mockStudent(@RequestBody MockStudentRequestDTO requestDTO,
                            @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        screeningPlanSchoolStudentBizService.initMockStudent(requestDTO, screeningPlanId, schoolId);
    }
}
