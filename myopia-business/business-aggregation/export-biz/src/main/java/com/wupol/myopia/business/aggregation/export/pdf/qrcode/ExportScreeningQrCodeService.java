package com.wupol.myopia.business.aggregation.export.pdf.qrcode;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QrCodeConstant;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.QrcodeUtil;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.awt.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/02/16/11:05
 * @Description: 导出告知书和二维码
 */
@Service("screeningQrCodeService")
@Slf4j
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

    private String gradeNameTmp;

    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        List<ScreeningStudentDTO> screeningStudentDTOs = getStudentData(exportCondition);
        generateExportScreeningQrCodePdfFile(screeningStudentDTOs,exportCondition,fileSavePath,fileName,exportCondition.getType(),gradeNameTmp);
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
            gradeNameTmp = gradeName;
        }
        String className = "";
        SchoolClass schoolClass = schoolClassService.getById(exportCondition.getClassId());
        if (schoolClass!=null&&schoolClass.getName()!=null){
            className = schoolClass.getName();
        }

        if (exportCondition.getType().equals(CommonConst.EXPORT_NOTICE)){
            return String.format(PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME, schoolName,gradeName,className);
        }else if (exportCondition.getType().equals(CommonConst.EXPORT_VS666)){
            return String.format(PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME, schoolName,gradeName,className);
        }else if (exportCondition.getType().equals(CommonConst.EXPORT_SCREENING_QRCODE)){
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
            String content;
            if (CommonConst.EXPORT_SCREENING_QRCODE.equals(exportCondition.getType())) {
                content = String.format(QrCodeConstant.SCREENING_CODE_QR_CONTENT_FORMAT_RULE, student.getPlanStudentId());
            } else if (CommonConst.EXPORT_VS666.equals(exportCondition.getType())) {
                content = QrcodeUtil.setVs666QrCodeRule(student.getPlanId(), student.getPlanStudentId(),
                        student.getName(),student.getAge(),student.getGender(),student.getParentPhone(),student.getSchoolName(),
                        student.getGradeName(),student.getClassName(),student.getIdCard());
            } else {
                content = String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, student.getPlanStudentId());
            }
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
                                                  String fileName,Integer type,String gradeNameTmp){

        Map<Integer, List<ScreeningStudentDTO>> gradeGroup = students.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getGradeId));
        for (Integer gradeId:gradeGroup.keySet()){
            List<ScreeningStudentDTO> gradeStudents = gradeGroup.get(gradeId);
            Map<Integer, List<ScreeningStudentDTO>> classGroup = gradeStudents.stream().collect(Collectors.groupingBy(ScreeningStudentDTO::getClassId));
            for (Integer classId:classGroup.keySet()){
                List<ScreeningStudentDTO> classStudents  = classGroup.get(classId);
                ScreeningStudentDTO screeningStudentDTO  = classStudents.get(0);

                String studentQrCodePdfHtmlUrl = String.format(HtmlPageUrlConstant.STUDENT_QRCODE_HTML_URL,htmlUrlHost,
                        exportCondition.getPlanId(), exportCondition.getSchoolId(),gradeId,classId,
                        Objects.nonNull(exportCondition.getPlanStudentIds()) ? exportCondition.getPlanStudentIds() : StringUtils.EMPTY,
                        type);

                String dir = null;
                String className = null;
                if(StringUtils.isNotBlank(gradeNameTmp)){
                    dir = Paths.get(fileSavePath,fileName).toString();
                    className = String.format(PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME, "",gradeNameTmp,screeningStudentDTO.getClassName())+".pdf";
                }else {
                    if (exportCondition.getType().equals(CommonConst.EXPORT_NOTICE)){
                        dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME);
                        className = String.format(PDFFileNameConstant.REPORT_NOTICE_QR_CODE_FILE_NAME, "","",screeningStudentDTO.getClassName())+".pdf";
                    }else if (exportCondition.getType().equals(CommonConst.EXPORT_QRCODE)){
                        dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_SCREENING_QR_CODE_FILE_NAME);
                        className = String.format(PDFFileNameConstant.REPORT_SCREENING_QR_CODE_FILE_NAME, "","",screeningStudentDTO.getClassName())+".pdf";
                    }else if (exportCondition.getType().equals(CommonConst.EXPORT_VS666)){
                        dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME);
                        className = String.format(PDFFileNameConstant.REPORT_VS666_QR_CODE_FILE_NAME, "","",screeningStudentDTO.getClassName())+".pdf";
                    }else if (exportCondition.getType().equals(CommonConst.EXPORT_SCREENING_QRCODE)){
                        dir = getDirPath(fileSavePath, fileName, screeningStudentDTO, PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME);
                        className = String.format(PDFFileNameConstant.REPORT_FICTITIOUS_QR_CODE_FILE_NAME, "","",screeningStudentDTO.getClassName())+".pdf";
                    }
                }
                String uuid = UUID.randomUUID().toString();
                PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(studentQrCodePdfHtmlUrl, className, uuid);
                log.info("响应参数:{}", JSONObject.toJSONString(pdfResponseDTO));
                try {
                    log.info("文件件保存路径:{}",dir);
                    FileUtils.downloadFile(pdfResponseDTO.getUrl(), Paths.get(dir,className).toString());
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }
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

        String studentQrCodePdfHtmlUrl = String.format(HtmlPageUrlConstant.STUDENT_QRCODE_HTML_URL,htmlUrlHost,
                exportCondition.getPlanId(), exportCondition.getSchoolId(),
                Objects.nonNull( exportCondition.getGradeId()) ? exportCondition.getGradeId() : StringUtils.EMPTY,
                Objects.nonNull( exportCondition.getClassId()) ? exportCondition.getClassId() : StringUtils.EMPTY,
                Objects.nonNull(exportCondition.getPlanStudentIds()) ? exportCondition.getPlanStudentIds() : StringUtils.EMPTY,
                type);
        String uuid = UUID.randomUUID().toString();
        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(studentQrCodePdfHtmlUrl, fileName+".pdf", uuid);
        log.info("response:{}", JSONObject.toJSONString(pdfResponseDTO));
        return pdfResponseDTO.getUrl();
    }
}
