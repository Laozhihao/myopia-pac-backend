package com.wupol.myopia.business.aggregation.export.pdf.qrcode;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.QrcodeUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/02/16/11:05
 * @Description: 导出告知书和二维码
 */
@Service(ExportReportServiceNameConstant.EXPORT_QRCODE_SCREENING_SERVICE)
@Log4j2
public class ExportScreeningQrCodeService extends BaseExportPdfFileService {
    @Value("${report.html.url-host}")
    public String htmlUrlHost;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private SchoolClassService schoolClassService;
    @Autowired
    private Html2PdfService html2PdfService;

    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        List<ScreeningStudentDTO> screeningStudentDTOs = getStudentData(exportCondition);
        generateExportScreeningQrCodePdfFile(screeningStudentDTOs,exportCondition,fileSavePath,fileName,exportCondition.getType());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Integer planId = exportCondition.getPlanId();
        Assert.notNull(planId, "筛查计划ID不能为空");

        Integer schoolId = exportCondition.getSchoolId();
        Assert.notNull(schoolId, "学校ID不能为空");
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        String schoolName = schoolService.getNameById(exportCondition.getSchoolId());

        String gradeName = "";
        SchoolGrade schoolGrade = schoolGradeService.getById(exportCondition.getGradeId());
        if (schoolGrade!=null&&schoolGrade.getName()!=null){
            gradeName = schoolGrade.getName();
        }
        String className = "";
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());
        if (schoolClass!=null&&schoolClass.getName()!=null){
            className = schoolClass.getName();
        }

        Integer type = exportCondition.getType();
        if (type.equals(CommonConst.EXPORT_NOTICE)){
            return String.format(PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME, schoolName,gradeName,className);
        }else if (type.equals(CommonConst.EXPORT_VS666)){
            return String.format(PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME, schoolName,gradeName,className);
        }else if (type.equals(CommonConst.EXPORT_SCREENING_QRCODE)){
            return String.format(PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME, schoolName,gradeName,className);
        }
        return String.format(PDFFileNameConstant.REPORT_SCREENING_QR_CODE_FILE_NAME, schoolName,gradeName,className);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_QRCODE_SCREENING,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getGradeId(),
                exportCondition.getClassId(),
                exportCondition.getPlanStudentIds()
        );
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String fileName = getFileName(exportCondition);
        return syncExportScreeningQrCodePdfFile(exportCondition,fileName,exportCondition.getType());
    }

    /**
     * 获取学数据
     * @param exportCondition
     * @return
     */
    public List<ScreeningStudentDTO> getStudentData(ExportCondition exportCondition){
        List<Integer> pladnStudentIds =null;
        if (StringUtil.isNotEmpty(exportCondition.getPlanStudentIds())&&!"null".equals(exportCondition.getPlanStudentIds())){
            pladnStudentIds = Arrays.stream(exportCondition.getPlanStudentIds().split(",")).map(Integer::valueOf).collect(Collectors.toList());
        }
        // 2. 处理参数
        List<ScreeningStudentDTO> students = screeningPlanSchoolStudentService.selectBySchoolGradeAndClass(
                exportCondition.getPlanId(), exportCondition.getSchoolId(),
                exportCondition.getGradeId(), exportCondition.getClassId(),pladnStudentIds);
        QrConfig config = new QrConfig().setHeight(130).setWidth(130).setBackColor(Color.white).setMargin(1);
        students.forEach(student -> {
            student.setGenderDesc(GenderEnum.getName(student.getGender()));
            String content = QrcodeUtil.getQrCodeContent(student.getPlanId(), student.getPlanStudentId(),
                    student.getAge(), student.getGender(), student.getParentPhone(),
                    student.getIdCard(), exportCondition.getType());
            student.setQrCodeUrl(QrCodeUtil.generateAsBase64(content, config, "jpg"));
        });

        return students;
    }

    /**
     * 导出学生二维码
     * @param exportCondition
     * @param fileSavePath
     * @param fileName
     */
    public void generateExportScreeningQrCodePdfFile(List<ScreeningStudentDTO> students,ExportCondition exportCondition, String fileSavePath,
                                                  String fileName,Integer type){

        Map<Integer, List<ScreeningStudentDTO>> gradeGroup = students.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getGradeId));
        gradeGroup.forEach((gradeId,gradeStudents)->{
            Map<Integer, List<ScreeningStudentDTO>> classGroup = gradeStudents.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getClassId));
            classGroup.forEach((classId,classStudents)-> downloadFile(exportCondition, fileSavePath, fileName, type, gradeId, classId, classStudents));
        });
    }

    /**
     * 下载文件
     * @param exportCondition
     * @param fileSavePath
     * @param fileName
     * @param type
     * @param gradeId
     * @param classId
     * @param classStudents
     */
    private void downloadFile(ExportCondition exportCondition, String fileSavePath, String fileName, Integer type, Integer gradeId, Integer classId, List<ScreeningStudentDTO> classStudents) {
        ScreeningStudentDTO screeningStudentDTO  = classStudents.get(0);

        String studentQrCodePdfHtmlUrl = getUrl(exportCondition, type, gradeId, classId);

        TwoTuple<String, String> dirAndClassName = getDirAndClassName(exportCondition, fileSavePath, fileName, screeningStudentDTO);

        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(studentQrCodePdfHtmlUrl, dirAndClassName.getSecond());
        log.info("响应参数:{}", JSON.toJSONString(pdfResponseDTO));
        try {
            log.info("文件件保存路径:{}",dirAndClassName.getFirst());
            FileUtils.downloadFile(pdfResponseDTO.getUrl(), Paths.get(dirAndClassName.getFirst(),dirAndClassName.getSecond()).toString());
        } catch (Exception e) {
            log.error("下载筛查二维码PDF异常", e);
        }
    }


    /**
     * 获取目录及文件名称
     * @param exportCondition
     * @param fileSavePath
     * @param fileName
     * @param screeningStudentDTO
     */
    private TwoTuple<String,String> getDirAndClassName(ExportCondition exportCondition, String fileSavePath,String fileName,ScreeningStudentDTO screeningStudentDTO){
        String dir = null;
        String className = null;
        if (exportCondition.getType().equals(CommonConst.EXPORT_NOTICE)) {
            dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME);
            className = String.format(PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME, "", "", screeningStudentDTO.getClassName()) + ".pdf";
        } else if (exportCondition.getType().equals(CommonConst.EXPORT_QRCODE)) {
            dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_SCREENING_QR_CODE_FILE_NAME);
            className = String.format(PDFFileNameConstant.REPORT_SCREENING_QR_CODE_FILE_NAME, "", "", screeningStudentDTO.getClassName()) + ".pdf";
        } else if (exportCondition.getType().equals(CommonConst.EXPORT_VS666)) {
            dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME);
            className = String.format(PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME, "", "", screeningStudentDTO.getClassName()) + ".pdf";
        } else if (exportCondition.getType().equals(CommonConst.EXPORT_SCREENING_QRCODE)) {
            dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME);
            className = String.format(PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME, "", "", screeningStudentDTO.getClassName()) + ".pdf";
        }
        return TwoTuple.of(dir,className);
    }

    /**
     * 文件路径
     * @param fileSavePath 保存路径
     * @param fileName 文件名称
     * @param screeningStudentDTO 学生文件信息
     * @param reportNoticeQrCodeFileName PDF报告文件名
     * @return 文件路径
     */
    private String getDirPath(String fileSavePath, String fileName, ScreeningStudentDTO screeningStudentDTO, String reportNoticeQrCodeFileName) {
        String temp = String.format(reportNoticeQrCodeFileName, "", screeningStudentDTO.getGradeName(), "");
        return Paths.get(fileSavePath, fileName, temp).toString();
    }

    /**
     * 同步导出筛查报告
     * @param exportCondition 传入参数
     * @param fileName 文件名称
     * @param type 文件类型
     * @return
     */
    public String syncExportScreeningQrCodePdfFile(ExportCondition exportCondition, String fileName,Integer type) {

        String studentQrCodePdfHtmlUrl = getUrl(exportCondition,type,exportCondition.getGradeId(),exportCondition.getClassId());

        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(studentQrCodePdfHtmlUrl, fileName+".pdf");
        log.info("response:{}", JSON.toJSONString(pdfResponseDTO));
        return pdfResponseDTO.getUrl();
    }


    /**
     * 获取访问地址
     * @param exportCondition
     * @param type
     * @param gradeId
     * @param classId
     */
    private String getUrl(ExportCondition exportCondition, Integer type, Integer gradeId, Integer classId) {
        return String.format(HtmlPageUrlConstant.STUDENT_QRCODE_HTML_URL, htmlUrlHost,
                exportCondition.getPlanId(), exportCondition.getSchoolId(),
                Objects.nonNull(gradeId) ? gradeId : StringUtils.EMPTY,
                Objects.nonNull(classId) ? classId : StringUtils.EMPTY,
                Objects.nonNull(exportCondition.getPlanStudentIds()) ? exportCondition.getPlanStudentIds() : StringUtils.EMPTY,
                type,Objects.nonNull(exportCondition.getIsSchoolClient())?exportCondition.getIsSchoolClient():StringUtils.EMPTY);
    }
}
