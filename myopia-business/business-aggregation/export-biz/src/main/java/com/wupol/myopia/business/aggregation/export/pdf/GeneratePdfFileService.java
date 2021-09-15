package com.wupol.myopia.business.aggregation.export.pdf;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.PlanSchoolGradeVO;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.util.HtmlToPdfUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.TemplateDistrictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 生成PDF报告
 *
 * @Author HaoHao
 * @Date 2021/3/26
 **/
@Service
@Slf4j
public class GeneratePdfFileService {

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
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private TemplateDistrictService templateDistrictService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 生成筛查报告PDF文件 - 行政区域
     *
     * @param saveDirectory 文件保存目录
     * @param fileName      文件名
     * @param noticeId      筛查通知ID
     * @param districtId    行政区域ID
     * @return void
     **/
    public void generateDistrictScreeningReportPdfFile(String saveDirectory, String fileName, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.hasLength(fileName, "文件名为空");
        Assert.notNull(noticeId, BizMsgConstant.NOTICE_ID_IS_EMPTY);
        Assert.notNull(districtId, "行政区域ID为空");
        String htmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_REPORT_HTML_URL, htmlUrlHost, noticeId, districtId);
        boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(saveDirectory, fileName + ".pdf").toString());
        Assert.isTrue(isSuccessful, "【生成行政区域报告PDF文件异常】" + fileName);
    }

    /**
     * 根据筛查通知ID，生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     **/
    public void generateSchoolScreeningReportPdfFileByNoticeId(String saveDirectory, Integer noticeId, Integer districtId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(noticeId, BizMsgConstant.NOTICE_ID_IS_EMPTY);
        List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        List<Integer> schoolIdList = statConclusionService.getSchoolIdsByScreeningNoticeIdAndDistrictIds(noticeId, districtIds);
        generateSchoolScreeningReportPdfFileBatch(saveDirectory, noticeId, null, schoolIdList);
    }

    /**
     * 生成筛查报告PDF文件 - 筛查机构
     *
     * @param saveDirectory 保存目录
     * @param planId        筛查计划ID
     **/
    public void generateScreeningOrgScreeningReportPdfFile(String saveDirectory, Integer planId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(planId, BizMsgConstant.PLAN_ID_IS_EMPTY);
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByPlanId(planId);
        generateSchoolScreeningReportPdfFileBatch(saveDirectory, null, planId, schoolIdList);
    }

    /**
     * 批量生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     * @param planId        筛查计划ID
     * @param schoolIdList  学校ID集合
     **/
    private void generateSchoolScreeningReportPdfFileBatch(String saveDirectory, Integer noticeId, Integer planId, List<Integer> schoolIdList) {
        Assert.notEmpty(schoolIdList, "学校ID集为空");
        schoolIdList.forEach(schoolId -> generateSchoolScreeningReportPdfFile(saveDirectory, noticeId, planId, schoolId));
    }

    /**
     * 生成筛查计划总报告
     *
     * @param saveDirectory
     * @param planId
     */
    public void generateScreeningPlanReportPdfFile(String saveDirectory, Integer planId) {
        log.info("生成筛查计划总报告, planId={}", planId);
        ScreeningPlan plan = screeningPlanService.getById(planId);
        Assert.notNull(plan, "该计划不存在");
        String reportFileName = String.format(PDFFileNameConstant.PLAN_REPORT_PDF_FILE_NAME, DateUtil.getYear(plan.getStartTime()));
        String schoolPdfHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_HTML_URL_WITH_PLAN_ID, htmlUrlHost, planId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, reportFileName + ".pdf").toString()), "【生成计划报告PDF文件异常】：" + plan.getTitle());
    }

    /**
     * 生成筛查报告PDF文件 - 学校
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId      筛查通知ID
     * @param planId        筛查计划ID
     * @param schoolId      学校ID
     **/
    public void generateSchoolScreeningReportPdfFile(String saveDirectory, Integer noticeId, Integer planId, Integer schoolId) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.isTrue(Objects.nonNull(noticeId) || Objects.nonNull(planId), "筛查通知ID和筛查计划ID都为空");
        Assert.notNull(schoolId, "学校ID不能为空");
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(PDFFileNameConstant.REPORT_PDF_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(Objects.isNull(noticeId) ? HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID : HtmlPageUrlConstant.SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID, htmlUrlHost, Objects.isNull(noticeId) ? planId : noticeId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成报告PDF文件异常】：" + school.getName());
    }

    /**
     * 生成档案卡PDF文件 - 筛查机构
     *
     * @param saveDirectory 保存目录
     * @param exportCondition        条件
     **/
    public void generateScreeningOrgArchivesPdfFile(String saveDirectory, ExportCondition exportCondition) {
        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(exportCondition.getPlanId(), BizMsgConstant.PLAN_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getSchoolId(), BizMsgConstant.SCHOOL_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getGradeId(), BizMsgConstant.GRADE_ID_IS_EMPTY);
        Assert.notNull(exportCondition.getClassId(), BizMsgConstant.CLASS_ID_IS_EMPTY);
        generateSchoolArchivesPdfFile(saveDirectory, exportCondition);
    }

    /**
     * 生成档案卡PDF文件 - 学校
     *  @param saveDirectory 保存目录
     * @param exportCondition
     **/
    public void generateSchoolArchivesPdfFile(String saveDirectory, ExportCondition exportCondition) {
        Integer planId = exportCondition.getPlanId();
        Integer schoolId = exportCondition.getSchoolId();
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        String planStudentIds = exportCondition.getPlanStudentIds();

        Assert.hasLength(saveDirectory, BizMsgConstant.SAVE_DIRECTORY_EMPTY);
        Assert.notNull(planId, BizMsgConstant.PLAN_ID_IS_EMPTY);
        Assert.notNull(schoolId, "学校ID不能为空");
        School school = schoolService.getById(schoolId);

        // 获取筛查机构的模板
        ScreeningOrganization org = screeningOrganizationService.getById(screeningPlanService.getById(planId).getScreeningOrgId());
        Integer templateId = templateDistrictService.getByDistrictId(districtService.getProvinceId(org.getDistrictId()));

        // 特殊处理
        if (ObjectsUtil.allNotNull(gradeId, classId)) {
            SchoolGrade schoolGrade = schoolGradeService.getById(gradeId);
            SchoolClass schoolClass = schoolClassService.getById(classId);
            String schoolPdfHtmlUrl = String.format(HtmlPageUrlConstant.SCHOOL_ARCHIVES_HTML_URL, htmlUrlHost, planId, schoolId, templateId, gradeId, classId, planStudentIds);
            String schoolReportFileName = String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME_GRADE_CLASS, school.getName(), schoolGrade.getName(), schoolClass.getName());
            String dir = saveDirectory + "/" + school.getName() + "/" + schoolGrade.getName() + "/" + schoolClass.getName();
            Assert.isTrue(HtmlToPdfUtil.convertArchives(schoolPdfHtmlUrl, Paths.get(dir, schoolReportFileName + ".pdf").toString()), "【生成学校档案卡PDF文件异常】：" + school.getName());
        } else {
            // 获取年纪班级信息
            List<PlanSchoolGradeVO> gradeAndClass = getGradeAndClass(planId, schoolId);

            for (PlanSchoolGradeVO gradeVO : gradeAndClass) {
                gradeVO.getClasses().forEach(schoolClass -> {
                    String schoolPdfHtmlUrl = String.format(HtmlPageUrlConstant.SCHOOL_ARCHIVES_HTML_URL, htmlUrlHost, planId, schoolId, templateId, gradeVO.getId(), schoolClass.getId(), "");
                    String schoolReportFileName = String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME_GRADE_CLASS, school.getName(), gradeVO.getGradeName(), schoolClass.getName());
                    String dir = saveDirectory + "/" + school.getName() + "/" + gradeVO.getGradeName() + "/" + schoolClass.getName();
                    Assert.isTrue(HtmlToPdfUtil.convertArchives(schoolPdfHtmlUrl, Paths.get(dir, schoolReportFileName + ".pdf").toString()), "【生成学校档案卡PDF文件异常】：" + school.getName());
                });
            }
        }
    }

    public List<PlanSchoolGradeVO> getGradeAndClass(Integer screeningPlanId, Integer schoolId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        Map<Integer, String> gradeMap = gradeClasses.stream().collect(Collectors.toMap(GradeClassesDTO::getGradeId, GradeClassesDTO::getGradeName, (o, n) -> n));
        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(gradeId -> {
            PlanSchoolGradeVO vo = new PlanSchoolGradeVO();
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            // 查询并设置年级名称
            vo.setId(gradeId);
            vo.setGradeName(gradeMap.get(gradeId));
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
}
