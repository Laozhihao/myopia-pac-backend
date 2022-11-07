package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.PlanSchoolGradeVO;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ExportPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.service.TemplateDistrictService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 档案卡PDF生成器
 *
 * @Author HaoHao
 * @Date 2022/5/25
 **/
@Service
public class ArchivePdfGenerator {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private TemplateDistrictService templateDistrictService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private Html2PdfService html2PdfService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 【行政区域】生成档案卡PDF文件
     *
     * @param saveDirectory   保存目录
     * @param exportCondition 条件
     **/
    public void generateDistrictArchivesPdfFile(String saveDirectory, ExportCondition exportCondition) {
        List<Integer> childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(exportCondition.getDistrictId());
        List<ExportPlanSchool> exportPlanSchoolGradeClassList = statConclusionService.getPlanSchoolGradeClassHasData(exportCondition.getNotificationId(), childDistrictIds);
        exportPlanSchoolGradeClassList.forEach(planSchool -> {
            ExportCondition condition = new ExportCondition();
            condition.setPlanId(planSchool.getScreeningPlanId())
                    .setSchoolId(planSchool.getSchoolId())
                    .setType(exportCondition.getType())
                    .setScreeningType(exportCondition.getScreeningType());
            generateSchoolOrGradeArchivesPdfFile(saveDirectory, condition);
        });

    }

    /**
     * 【学校/年级】生成档案卡PDF文件
     *
     * @param saveDirectory   保存目录
     * @param exportCondition 条件
     **/
    public void generateSchoolOrGradeArchivesPdfFile(String saveDirectory, ExportCondition exportCondition) {
        Integer planId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();

        // 必要参数校验
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);

        // 获取档案卡模板ID
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Integer templateId = templateDistrictService.getArchivesByDistrictId(districtService.getProvinceId(plan.getDistrictId()), TemplateConstants.getTemplateBizTypeByScreeningType(plan.getScreeningType()));

        // 生成PDF（转换为年级维度）
        School school = schoolService.getById(schoolId);
        List<PlanSchoolGradeVO> gradeAndClass = getGradeAndClass(planId, schoolId, gradeId);
        for (PlanSchoolGradeVO grade : gradeAndClass) {
            grade.getClasses().forEach(schoolClass -> generateClassArchivesPdfFile(saveDirectory, planId, templateId, school.getId(), school.getName(), grade.getId(),
                    grade.getGradeName(), schoolClass.getId(), schoolClass.getName(), exportCondition.getType(), exportCondition.getScreeningType()));
        }
    }

    /**
     * 【班级】生成档案卡PDF文件
     *
     * @param saveDirectory 保存的路径
     * @param planId        计划Id
     * @param templateId    模板Id
     * @param schoolId      学校id
     * @param schoolName    学校名称
     * @param gradeId       年级Id
     * @param gradeName     年级名称
     * @param classId       班级Id
     * @param className     班级名称
     */
    private void generateClassArchivesPdfFile(String saveDirectory, Integer planId, Integer templateId, Integer schoolId, String schoolName, Integer gradeId, String gradeName,
                                              Integer classId, String className, Integer type, Integer screeningType) {
        String fileName = String.format(PDFFileNameConstant.CLASS_ARCHIVES_PDF_FILE_NAME, schoolName, gradeName, className, ScreeningTypeEnum.isVisionScreeningType(screeningType) ? PDFFileNameConstant.VISION_ARCHIVE : PDFFileNameConstant.COMMON_DISEASE_ARCHIVE);
        String fileSavePath = Paths.get(saveDirectory, schoolName, gradeName, className, fileName).toString();
        generateClassArchivesPdfFile(planId, templateId, schoolId, gradeId, classId, null, type, fileSavePath, fileName, screeningType);
    }

    /**
     * 【班级/多个学生】生成档案卡PDF文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     */
    public void generateClassOrStudentsArchivesPdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        String planStudentIds = exportCondition.getPlanStudentIds();

        // 获取筛查机构的模板
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Integer templateId = templateDistrictService.getArchivesByDistrictId(districtService.getProvinceId(plan.getDistrictId()), TemplateConstants.getTemplateBizTypeByScreeningType(plan.getScreeningType()));

        // 生成PDF
        generateClassArchivesPdfFile(planId, templateId, schoolId, gradeId, classId, planStudentIds, exportCondition.getType(), fileSavePath, fileName, exportCondition.getScreeningType());
    }

    /**
     * 【班级】生成档案卡PDF文件
     *
     * @param planId
     * @param templateId
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param planStudentIds
     * @param type
     * @param fileSavePath
     * @param fileName
     * @param screeningType
     * @return void
     **/
    private void generateClassArchivesPdfFile(Integer planId, Integer templateId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIds,
                                              Integer type, String fileSavePath, String fileName, Integer screeningType) {
        // 根据不同筛查类型走不一样的生成方式 TODO：合并url
        if (ScreeningTypeEnum.VISION.getType().equals(screeningType)) {
            generateVisionArchivesPDF(planId, classId, planStudentIds, schoolId, templateId, fileSavePath, gradeId, fileName);
        } else {
            generateCommonDiseaseArchivesPDF(planId, classId, planStudentIds, type, templateId, fileSavePath, fileName);
        }
    }

    private void generateVisionArchivesPDF(Integer planId, Integer classId, String planStudentIds, Integer schoolId, Integer templateId, String fileSavePath, Integer gradeId, String fileName) {
        String studentPdfHtmlUrl = String.format(HtmlPageUrlConstant.CLASS_ARCHIVES_HTML_URL, htmlUrlHost, planId, schoolId, templateId, gradeId, Objects.nonNull(classId) ? classId : StringUtils.EMPTY, StringUtils.isNotBlank(planStudentIds) ? planStudentIds : StringUtils.EMPTY);
        String pdfUrl = html2PdfService.convertHtmlToPdf(studentPdfHtmlUrl, fileName);
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成常见病档案卡PDF文件异常", e);
        }
    }

    /**
     * 生成常见病PDF
     *
     * @param planId
     * @param classId
     * @param planStudentIds
     * @param type
     * @param templateId
     * @param fileSavePath
     * @param fileName
     * @return void
     **/
    private void generateCommonDiseaseArchivesPDF(Integer planId, Integer classId, String planStudentIds, Integer type, Integer templateId, String fileSavePath, String fileName) {
        String archiveHtmlUrl = String.format(HtmlPageUrlConstant.STUDENT_ARCHIVE_HTML_URL, htmlUrlHost, templateId, planId, classId, StringUtils.isNotBlank(planStudentIds) ? planStudentIds : StringUtils.EMPTY, type);
        String pdfUrl = html2PdfService.convertHtmlToPdf(archiveHtmlUrl, fileName);
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成常见病档案卡PDF文件异常", e);
        }
    }

    /**
     * 获取年级班级信息
     *
     * @param screeningPlanId
     * @param schoolId
     * @param gradeId
     * @return java.util.List<com.wupol.myopia.business.aggregation.export.pdf.domain.PlanSchoolGradeVO>
     **/
    public List<PlanSchoolGradeVO> getGradeAndClass(Integer screeningPlanId, Integer schoolId, Integer gradeId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId, gradeId);
        setGradeAndClass(gradeClasses);
        Map<Integer, String> gradeMap = gradeClasses.stream().collect(Collectors.toMap(GradeClassesDTO::getGradeId, GradeClassesDTO::getGradeName, (o, n) -> n));
        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(x -> {
            PlanSchoolGradeVO vo = new PlanSchoolGradeVO();
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(x);
            // 查询并设置年级名称
            vo.setId(x);
            vo.setGradeName(gradeMap.get(x));
            // 查询并设置班级名称
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setId(dto.getClassId());
                schoolClass.setName(dto.getClassName());
                return schoolClass;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 设置班级、年级名称
     *
     * @param gradeClasses gradeClasses
     */
    private void setGradeAndClass(List<GradeClassesDTO> gradeClasses) {
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeClasses, GradeClassesDTO::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(gradeClasses, GradeClassesDTO::getClassId);
        gradeClasses.forEach(s -> {
            s.setGradeName(gradeMap.getOrDefault(s.getGradeId(), new SchoolGrade()).getName());
            s.setClassName(classMap.getOrDefault(s.getClassId(), new SchoolClass()).getName());
        });
    }

    public PDFRequestDTO getUrl(String saveDirectory, ExportCondition exportCondition) {
        Integer planId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();

        // 必要参数校验
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);

        // 获取档案卡模板ID
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Integer templateId = templateDistrictService.getArchivesByDistrictId(districtService.getProvinceId(plan.getDistrictId()), TemplateConstants.getTemplateBizTypeByScreeningType(plan.getScreeningType()));

        // 生成PDF（转换为年级维度）
        School school = schoolService.getById(schoolId);
        List<PlanSchoolGradeVO> gradeAndClass = getGradeAndClass(planId, schoolId, gradeId);
        List<PDFRequestDTO.Item> result = new ArrayList<>();
        for (PlanSchoolGradeVO grade : gradeAndClass) {
            grade.getClasses().forEach(schoolClass -> result.add(generateClassArchivesPdfFile2(saveDirectory, planId, templateId, school.getId(), school.getName(), grade.getId(),
                    grade.getGradeName(), schoolClass.getId(), schoolClass.getName(), exportCondition.getType(), exportCondition.getScreeningType())));
        }
        PDFRequestDTO requestDTO = new PDFRequestDTO();
        requestDTO.setItems(result).setZipFileName(saveDirectory);
        return requestDTO;
    }
    private PDFRequestDTO.Item generateClassArchivesPdfFile2(String saveDirectory, Integer planId, Integer templateId, Integer schoolId, String schoolName, Integer gradeId, String gradeName,
                                              Integer classId, String className, Integer type, Integer screeningType) {
        String fileName = String.format(PDFFileNameConstant.CLASS_ARCHIVES_PDF_FILE_NAME, schoolName, gradeName, className, ScreeningTypeEnum.isVisionScreeningType(screeningType) ? PDFFileNameConstant.VISION_ARCHIVE : PDFFileNameConstant.COMMON_DISEASE_ARCHIVE);
        String fileSavePath = Paths.get(saveDirectory, schoolName, gradeName, className, fileName).toString();
        return generateClassArchivesPdfFile2(planId, templateId, schoolId, gradeId, classId, null, type, fileSavePath, fileName, screeningType);
    }

    private PDFRequestDTO.Item generateClassArchivesPdfFile2(Integer planId, Integer templateId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIds,
                                              Integer type, String fileSavePath, String fileName, Integer screeningType) {
        if (ScreeningTypeEnum.VISION.getType().equals(screeningType)) {
            String studentPdfHtmlUrl = String.format(HtmlPageUrlConstant.CLASS_ARCHIVES_HTML_URL, htmlUrlHost, planId, schoolId, templateId, gradeId, Objects.nonNull(classId) ? classId : StringUtils.EMPTY, StringUtils.isNotBlank(planStudentIds) ? planStudentIds : StringUtils.EMPTY);
            return new PDFRequestDTO.Item().setUrl(studentPdfHtmlUrl).setFileName(fileSavePath);
        } else {
            String archiveHtmlUrl = String.format(HtmlPageUrlConstant.STUDENT_ARCHIVE_HTML_URL, htmlUrlHost, templateId, planId, classId, StringUtils.isNotBlank(planStudentIds) ? planStudentIds : StringUtils.EMPTY, type);
            return new PDFRequestDTO.Item().setUrl(archiveHtmlUrl).setFileName(fileSavePath);
        }
    }


}
