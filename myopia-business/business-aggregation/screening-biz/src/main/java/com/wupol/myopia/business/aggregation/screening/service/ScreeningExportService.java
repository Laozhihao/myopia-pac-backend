package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.lowagie.text.DocumentException;
import com.vistel.Interface.exception.UtilException;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.utils.FreemarkerUtil;
import com.wupol.framework.utils.PdfUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.AppQueryQrCodeParams;
import com.wupol.myopia.business.aggregation.screening.domain.dto.ScreeningQrCodeDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.QrCodeInfo;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QrCodeConstant;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.util.QrcodeUtil;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.PDFTemplateConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查导出相关
 *
 * @author Simple4H
 */
@Service
public class ScreeningExportService {

    /**
     * 默认文件Id
     */
    private static final Integer DEFAULT_FILE_ID = -1;

    /**
     * 默认图片路径
     */
    private static final String DEFAULT_IMAGE_PATH = "/image/wechat_mp_qrcode.png";

    @Value("${server.host}")
    private String hostUrl;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private S3Utils s3Utils;

    /**
     * 导出筛查计划的学生告知书
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @param schoolId        学校Id
     * @return PDF的URL
     */
    public Map<String, String> getNoticeFile(ScreeningPlanSchoolStudent schoolClassInfo, Integer schoolId) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            School school = schoolService.getBySchoolId(schoolClassInfo.getSchoolId());
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-告知书", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));
            ScreeningPlan plan = screeningPlanService.getById(schoolClassInfo.getScreeningPlanId());

            List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getScreeningPlanId(), schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
            students.forEach(student -> {
                student.setQrCodeUrl(QrCodeUtil.generateAsBase64(String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, student.getPlanStudentId()), config, "jpg"));
                student.setGenderDesc(GenderEnum.getName(student.getGender()));
            });
            NotificationConfig notificationConfig;
            // 如果学校Id不为空，说明是学校端进行的导出，使用学校自己的告知书配置
            if (Objects.nonNull(schoolId)) {
                notificationConfig = school.getNotificationConfig();
            } else {
                ScreeningOrgResponseDTO screeningOrganization = screeningOrganizationService.getScreeningOrgDetails(plan.getScreeningOrgId());
                notificationConfig = screeningOrganization.getNotificationConfig();
            }
            Map<String, Object> models = getQrCodeInfoMap(school, classDisplay, students, notificationConfig);
            // 3. 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
            return uploadQrCodePdf(fileName, models);
        } catch (Exception e) {
            throw new BusinessException("生成PDF文件失败", e);
        }
    }

    /**
     * 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
     * @param fileName 文件名称
     * @param models 二维码信息
     * @return
     * @throws DocumentException
     * @throws IOException
     * @throws TemplateException
     * @throws UtilException
     */
    private Map<String, String> uploadQrCodePdf(String fileName, Map<String, Object> models) throws DocumentException, IOException, TemplateException, UtilException {
        File file = PdfUtil.generatePdfFromContent(FreemarkerUtil.generateHtmlString(PDFTemplateConst.NOTICE_TEMPLATE_PATH, models), hostUrl, fileName);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("url", s3Utils.getPdfUrl(file.getName(), file));
        return resultMap;
    }

    /**
     * 我二维码返回参数
     * @param school 学校
     * @param classDisplay 班级
     * @param students 学生列表
     * @param notificationConfig 通知配置
     * @return 二维码返回参数
     */
    private Map<String, Object> getQrCodeInfoMap(School school, String classDisplay, List<ScreeningStudentDTO> students, NotificationConfig notificationConfig) {
        Map<String, Object> models = new HashMap<>();
        models.put("screeningOrgConfigs", notificationConfig);
        models.put("students", students);
        models.put("classDisplay", classDisplay);
        models.put("schoolName", school.getName());
        if (Objects.nonNull(notificationConfig)
                && Objects.nonNull(notificationConfig.getQrCodeFileId())
                && !notificationConfig.getQrCodeFileId().equals(DEFAULT_FILE_ID)
        ) {
            models.put("qrCodeFile", resourceFileService.getResourcePath(notificationConfig.getQrCodeFileId()));
        } else {
            models.put("qrCodeFile", DEFAULT_IMAGE_PATH);
        }
        return models;
    }

    /**
     *
     * @param screeningPlanId 计划ID
     * @param schoolId 学校ID
     * @param gradeId 年级ID
     * @param classId 班级ID
     * @param studentIds 学生ID集合
     * @param isSchoolClient true:学校端   fasle：管理端
     * @return ScreeningQrCodeDTO
     */
    public ScreeningQrCodeDTO getNoticeData(Integer screeningPlanId, Integer schoolId, Integer gradeId, Integer classId, List<Integer> studentIds, boolean isSchoolClient) {
        // 2. 处理参数
        String gradeName = StringUtils.EMPTY;
        School school = schoolService.getBySchoolId(schoolId);
        if (Objects.nonNull(gradeId)) {
            SchoolGrade schoolGrade = schoolGradeService.getById(gradeId);
            gradeName = schoolGrade.getName();
        }
        String className = StringUtils.EMPTY;
        if (Objects.nonNull(classId)) {
            SchoolClass schoolClass = schoolClassService.getById(classId);
            className = schoolClass.getName();
        }
        String classDisplay = String.format("%s%s", gradeName, className);

        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);
        NotificationConfig notificationConfig;
        if (Objects.equals(plan.getScreeningOrgType(), ScreeningOrgTypeEnum.ORG.getType())) {
            // 如果学校Id不为空，说明是学校端进行的导出，使用学校自己的告知书配置
            if (isSchoolClient) {
                notificationConfig = school.getNotificationConfig();
            } else {
                ScreeningOrgResponseDTO screeningOrganization = screeningOrganizationService.getScreeningOrgDetails(plan.getScreeningOrgId());
                notificationConfig = screeningOrganization.getNotificationConfig();
            }
        } else {
            notificationConfig = school.getNotificationConfig();
        }

        List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.selectBySchoolGradeAndClass(screeningPlanId, schoolId, gradeId, classId, studentIds);
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(students, ScreeningStudentDTO::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(students, ScreeningStudentDTO::getClassId);
        QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
        students.forEach(student -> {
            student.setGradeName(gradeMap.getOrDefault(student.getGradeId(), new SchoolGrade()).getName())
                    .setClassName(classMap.getOrDefault(student.getClassId(), new SchoolClass()).getName());
            student.setSchoolName(school.getName());
            student.setQrCodeUrl(QrCodeUtil.generateAsBase64(QrcodeUtil.setVs666QrCodeRule(screeningPlanId, student.getPlanStudentId(), student.getAge(), student.getGender(), student.getParentPhone(), student.getIdCard()), config, "jpg"));
            student.setGenderDesc(GenderEnum.getName(student.getGender()));
            student.setScreeningOrgConfigs(notificationConfig);
        });
        return getScreeningQrCodeDTO(school, classDisplay, notificationConfig, students);
    }

    /**
     * 筛查二维码返回数据
     * @param school 学校
     * @param classDisplay 班级
     * @param notificationConfig 通知配置
     * @param students 学生集合信息
     * @return
     */
    private ScreeningQrCodeDTO getScreeningQrCodeDTO(School school, String classDisplay, NotificationConfig notificationConfig, List<ScreeningStudentDTO> students) {
        ScreeningQrCodeDTO screeningQrCodeDTO = new ScreeningQrCodeDTO();
        screeningQrCodeDTO.setNotificationConfig(notificationConfig);
        screeningQrCodeDTO.setStudents(students);
        screeningQrCodeDTO.setClassDisplay(classDisplay);
        screeningQrCodeDTO.setSchoolName(school.getName());
        if (Objects.nonNull(notificationConfig)
                && Objects.nonNull(notificationConfig.getQrCodeFileId())
                && !notificationConfig.getQrCodeFileId().equals(DEFAULT_FILE_ID)) {
            screeningQrCodeDTO.setQrCodeFile(resourceFileService.getResourcePath(notificationConfig.getQrCodeFileId()));
        } else {
            screeningQrCodeDTO.setQrCodeFile(StringUtils.EMPTY);
        }
        return screeningQrCodeDTO;
    }

    /**
     * 导出筛查计划的学生二维码信息
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @param type            1-二维码 2-VS666 3-学生编码二维码
     * @return pdf的URL
     */
    public Map<String, String> getQrCodeFile(ScreeningPlanSchoolStudent schoolClassInfo, Integer type) {
        try {
            // 1. 校验
            validateExistAndAuthorize(schoolClassInfo.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
            // 2. 处理参数
            String schoolName = schoolService.getNameById(schoolClassInfo.getSchoolId());
            SchoolClass schoolClass = schoolClassService.getById(schoolClassInfo.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(schoolClassInfo.getGradeId());
            String classDisplay = String.format("%s%s", schoolGrade.getName(), schoolClass.getName());
            String fileName = String.format("%s-%s-二维码", classDisplay, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_TIME_WITHOUT_LINE));

            List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.getByGradeAndClass(schoolClassInfo.getScreeningPlanId(), schoolClassInfo.getGradeId(), schoolClassInfo.getClassId());
            QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
            students.forEach(student -> {
                student.setGenderDesc(GenderEnum.getName(student.getGender()));
                student.setQrCodeUrl(QrCodeUtil.generateAsBase64(QrcodeUtil.getQrCodeContent(
                        student.getPlanId(), student.getPlanStudentId(),
                        student.getAge(),student.getGender(),student.getParentPhone(),
                        student.getIdCard(),
                        type), config, "jpg"));
            });

            // 3. 处理pdf报告参数
            Map<String, Object> models = new HashMap<>(16);
            models.put("students", students);
            models.put("classDisplay", classDisplay);
            models.put("schoolName", schoolName);
            // 4. 生成并上传覆盖pdf。S3上路径：myopia/pdf/{date}/{file}。获取地址1天失效
            File file = PdfUtil.generatePdfFromContent(FreemarkerUtil.generateHtmlString(
                    CommonConst.EXPORT_SCREENING_QRCODE.equals(type) ? PDFTemplateConst.SCREENING_QRCODE_TEMPLATE_PATH :
                            PDFTemplateConst.QRCODE_TEMPLATE_PATH, models), fileName);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.getPdfUrl(file.getName(), file));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException("生成PDF文件失败", e);
        }
    }

    /**
     * 获取学生二维码
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     * @param gradeId 年级ID
     * @param classId 班级ID
     * @param planStudentIds 学生ID
     * @param type 3：导出虚拟二维码 2：EXPORT_VS666 其他：筛查二维码
     * @return
     */
    public List<ScreeningStudentDTO> studentQRCodeFile(Integer screeningPlanId,Integer schoolId, Integer gradeId,Integer classId,List<Integer> planStudentIds,Integer type) {
        // 2. 处理参数
        List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.selectBySchoolGradeAndClass(
                screeningPlanId, schoolId,gradeId, classId,planStudentIds);
        QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(students, ScreeningStudentDTO::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(students, ScreeningStudentDTO::getClassId);
        students.forEach(student -> {
            student.setGenderDesc(GenderEnum.getName(student.getGender()));
            student.setGradeName(gradeMap.getOrDefault(student.getGradeId(), new SchoolGrade()).getName())
                    .setClassName(classMap.getOrDefault(student.getClassId(), new SchoolClass()).getName());
            String content = QrcodeUtil.getQrCodeContent(student.getPlanId(), student.getPlanStudentId(),
                    student.getAge(), student.getGender(), student.getParentPhone(),
                    student.getIdCard(), type);
            //TODO 调整内容就好，上完线在来处理，需要和前段对接
            student.setQrCodeUrl(QrCodeUtil.generateAsBase64(content, config, "jpg"));
        });
        return students;
    }

    /**
     * 导出指定学生的筛查二维码
     *
     * @param params
     * @return
     */
    public List<QrCodeInfo> getQrCodeAndStudentInfo(AppQueryQrCodeParams params, Integer orgId,Integer channel) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(orgId, channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            throw new BusinessException("当前无筛查计划");
        }
        List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.getScreeningNoticeResultStudent(new ArrayList<>(currentPlanIds), params.getSchoolId(), params.getGradeId(), params.getClassId(), null, params.getStudentName(), Boolean.FALSE)
                .stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(students)) {
            return Collections.emptyList();
        }
        String gradeName = Optional.ofNullable(schoolGradeService.getById(params.getGradeId())).orElse(new SchoolGrade()).getName();
        String className = Optional.ofNullable(schoolClassService.getById(params.getClassId())).orElse(new SchoolClass()).getName();
        return students.stream().map(student -> {
            student.setGenderDesc(GenderEnum.getName(student.getGender()));
            int type = params.getType();
            QrCodeInfo info = new QrCodeInfo();
            info.setName(student.getName());
            info.setBirthday(DateFormatUtil.format(student.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE));
            info.setGender(student.getGenderDesc());
            info.setGradeName(gradeName);
            info.setClassName(className);
            info.setQrCodeContent(QrcodeUtil.getQrCodeContent(
                    student.getPlanId(), student.getPlanStudentId(),
                    student.getAge(),student.getGender(),student.getParentPhone(),
                    student.getIdCard(),
                    type));
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * 校验计划是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningPlanId 筛查计划ID
     * @param releaseStatus   发布状态
     */
    public ScreeningPlan validateExistAndAuthorize(Integer screeningPlanId, Integer releaseStatus) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isGovDeptUser()) {
            // 政府部门，无法新增修改计划
            throw new ValidationException("无权限");
        }
        ScreeningPlan screeningPlan = validateExistWithReleaseStatusAndReturn(screeningPlanId, releaseStatus);
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            // 筛查机构人员，需校验是否同机构
            Assert.isTrue(user.getScreeningOrgId().equals(screeningPlan.getScreeningOrgId()), "无该筛查机构权限");
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
    public ScreeningPlan validateExistWithReleaseStatusAndReturn(Integer id, Integer releaseStatus) {
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
    public ScreeningPlan validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(id);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("查无该计划");
        }
        return screeningPlan;
    }
}
