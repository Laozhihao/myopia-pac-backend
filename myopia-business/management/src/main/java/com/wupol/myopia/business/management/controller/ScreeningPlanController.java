package com.wupol.myopia.business.management.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.utils.FreemarkerUtil;
import com.wupol.framework.utils.PdfUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.PDFTemplateConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.*;
import com.wupol.myopia.business.management.util.S3Utils;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.List;

/**
 * 筛查计划相关接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningPlan")
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

    /**
     * 新增
     *
     * @param screeningPlanDTO 新增参数
     * @return Object
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
            screeningPlanDTO.setDistrictId(screeningTask.getDistrictId()).setGovDeptId(screeningTask.getGovDeptId());
        } else {
            // 用户自己新建的筛查计划需设置districtId
            ScreeningOrganization organization = screeningOrganizationService.getById(user.getOrgId());
            screeningPlanDTO.setDistrictId(organization.getDistrictId());
        }
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
     * @return Object
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) throws AccessDeniedException {
        validateExistAndAuthorize(screeningPlanDTO.getId(), CommonConst.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, false);
    }

    /**
     * 校验计划是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningPlanId
     * @param releaseStatus
     */
    private void validateExistAndAuthorize(Integer screeningPlanId, Integer releaseStatus) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isGovDeptUser() || user.isPlatformAdminUser()) {
            // 政府部门，无法新增计划
            throw new ValidationException("无权限");
        }
        ScreeningPlan screeningPlan = validateExistWithReleaseStatusAndReturn(screeningPlanId, releaseStatus);
        if (user.isScreeningUser()) {
            // 筛查机构人员，需校验是否同机构
            Assert.isTrue(user.getOrgId().equals(screeningPlan.getScreeningOrgId()), "无该筛查机构权限");
        }
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     * 返回该筛查计划
     *
     * @param id
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
     * @param id
     * @return
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
     * @return Object
     */
    @GetMapping("page")
    public IPage queryInfo(PageRequest page, ScreeningPlanQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isScreeningUser()) {
            query.setScreeningOrgId(user.getOrgId());
        }
        if (user.isGovDeptUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return screeningPlanService.getPage(query, page);
    }

    /**
     * 获取计划学校
     *
     * @param screeningPlanId 计划ID
     * @return Object
     */
    @GetMapping("schools/{screeningPlanId}")
    public List<ScreeningPlanSchoolVo> querySchoolsInfo(@PathVariable Integer screeningPlanId) {
        // 任务状态判断
        validateExist(screeningPlanId);
        return screeningPlanSchoolService.getSchoolVoListsByPlanId(screeningPlanId);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return Object
     */
    @GetMapping("grades/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVo> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        // 任务状态判断
        validateExist(screeningPlanId);
        return screeningPlanSchoolStudentService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 新增筛查学校
     *
     * @param screeningPlanSchool 新增参数
     * @return Object
     */
    @PostMapping("schools")
    public void addSchoolsInfo(@RequestBody @Valid ScreeningPlanSchool screeningPlanSchool) {
        // 任务状态判断：已发布才能新增
        validateExistWithReleaseStatusAndReturn(screeningPlanSchool.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
        // 是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOne(screeningPlanSchool.getScreeningPlanId(), screeningPlanSchool.getSchoolId());
        if (Objects.nonNull(planSchool)) {
            screeningPlanSchool.setId(planSchool.getId());
        }
        screeningPlanSchoolService.saveOrUpdate(screeningPlanSchool);
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id ID
     * @return void
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
     * @param id ID
     * @return void
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) throws AccessDeniedException {
        // 已发布，直接返回
        validateExistWithReleaseStatusAndReturn(id, CommonConst.STATUS_RELEASE);
        // 没有学校，直接报错
        if (CollectionUtils.isEmpty(screeningPlanSchoolService.getSchoolListsByPlanId(id))) {
            throw new ValidationException("无筛查的学校");
        }
        screeningPlanService.release(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 分页查询筛查学生信息
     *
     * @param query 查询参数
     * @param page  分页数据
     * @return Object
     */
    @GetMapping("students/page")
    public IPage queryStudentInfos(PageRequest page, StudentQuery query) {
        validateExistWithReleaseStatusAndReturn(query.getScreeningPlanId(), null);
        return screeningPlanSchoolStudentService.getPage(query, page);
    }

    /**
     * 导入筛查计划的学生数据
     *
     * @param file
     * @param screeningPlanId
     * @param schoolId
     * @throws IOException
     */
    @PostMapping("/upload/{screeningPlanId}/{schoolId}")
    public void uploadScreeningStudents(MultipartFile file, @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        // TODO
//        //1. 发布成功后才能导入
//        validateExistWithReleaseStatusAndReturn(screeningPlanId, CommonConst.STATUS_NOT_RELEASE);
//        //2. 校验计划学校是否已存在
//        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOne(screeningPlanId, schoolId);
//        if (Objects.isNull(planSchool)) {
//            throw new ValidationException("该筛查学校不存在");
//        }
        excelFacade.importScreeningSchoolStudents(currentUser.getId(), file, screeningPlanId, schoolId);
    }

    /**
     * 导出筛查计划的学生二维码信息
     *
     * @param schoolClassInfo
     * @throws IOException
     */
    @GetMapping("/export/QRCode")
    public Object downloadQRCodeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-二维码", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));
            List<StudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white);
            students.forEach(student -> student.setGenderDesc(GenderEnum.getName(student.getGender())).setQrCodeUrl(QrCodeUtil.generateAsBase64(student.getSno(), config, "jpg")));
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
     * 导出筛查计划的学生告知书
     *
     * @param schoolClassInfo
     * @throws IOException
     */
    @GetMapping("/export/notice")
    public Object downloadNoticeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            School school = schoolService.getById(schoolClassInfo.getSchoolId());
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-告知书", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(schoolClassInfo.getScreeningPlanId());
            List<StudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white);
            students.forEach(student -> student.setQrCodeUrl(QrCodeUtil.generateAsBase64(student.getSno(), config, "jpg")));
            Map<String, Object> models = new HashMap<>(16);
            models.put("screeningOrgConfigs", screeningOrganization.getNotificationConfig());
            models.put("students", students);
            models.put("classDisplay", classDisplay);
            models.put("schoolName", school.getName());
            models.put("qrCodeFile", resourceFileService.getResourcePath(screeningOrganization.getNotificationConfig().getQrCodeFileId()));
            // 3. 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
            File file = PdfUtil.generatePdfFromContent(FreemarkerUtil.generateHtmlString(PDFTemplateConst.NOTICE_TEMPLATE_PATH, models), fileName);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.getPdfUrl(file.getName(), file));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException("生成PDF文件失败", e);
        }
    }
}
