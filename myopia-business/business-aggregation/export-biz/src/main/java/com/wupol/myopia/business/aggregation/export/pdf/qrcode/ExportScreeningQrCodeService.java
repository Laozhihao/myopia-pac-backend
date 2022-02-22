package com.wupol.myopia.business.aggregation.export.pdf.qrcode;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.constant.QrCodeConstant;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/02/16/11:05
 * @Description: 导出筛查二维码
 */
@Service("screeningQrCodeService")
public class ExportScreeningQrCodeService extends BaseExportPdfFileService {
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private ScreeningExportService screeningExportService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;
    @Autowired
    private GeneratePdfFileService generateReportPdfService;

    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        List<ScreeningStudentDTO> screeningStudentDTOS = getStudentData(exportCondition);
        generateReportPdfService.generateExportScreenQrcodePdfFile(screeningStudentDTOS,exportCondition,fileSavePath,fileName,exportCondition.getType());
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
        return String.format(PDFFileNameConstant.REPORT_RQCODE_FILE_NAME, schoolName,gradeName,className);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_QRCODE_SCREENING,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getGradeId(),
                exportCondition.getClassId()
        );
    }


    @Override
    public String syncExport(ExportCondition exportCondition) {
        String fileSavePath = getFileSaveParentPath();
        String fileName = getFileName(exportCondition);
        return generateReportPdfService.syncExportScreenQrcodePdfFile(exportCondition,fileSavePath,fileName);
    }


    public List<ScreeningStudentDTO> getStudentData(ExportCondition exportCondition){
        List<Integer> pladnStudentIds =null;
        if (StringUtil.isNotEmpty(exportCondition.getPlanStudentIds())&&!exportCondition.getPlanStudentIds().equals("null")){
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
                content = screeningExportService.setVs666QrCodeRule(student);
            } else {
                content = String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, student.getPlanStudentId());
            }
            student.setQrCodeUrl(QrCodeUtil.generateAsBase64(content, config, "jpg"));
        });

        return students;
    }

}
