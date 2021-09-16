package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出筛查机构的档案卡（压缩包中包含多个学校的PDF文件）
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("screeningOrgArchivesService")
public class ExportScreeningOrgArchivesService extends BaseExportPdfFileService {

    @Autowired
    private GeneratePdfFileService generateReportPdfService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generateReportPdfService.generateScreeningOrgArchivesPdfFile(fileSavePath, exportCondition);
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        Integer gradeId = exportCondition.getGradeId();
        Integer classId = exportCondition.getClassId();
        School school = schoolService.getById(exportCondition.getSchoolId());
        if (ObjectsUtil.allNotNull(gradeId, classId)) {
            return String.format(PDFFileNameConstant.GRADE_CLASS_PDF_FILE_NAME,
                    school.getName(),
                    schoolGradeService.getById(gradeId).getName(),
                    schoolClassService.getById(classId).getName());
        }
        return String.format(PDFFileNameConstant.ALL_SCHOOL_PDF_FILE_NAME, school.getName());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        int total = visionScreeningResultService.count(new VisionScreeningResult().setScreeningOrgId(exportCondition.getScreeningOrgId()).setPlanId(exportCondition.getPlanId()).setIsDoubleScreen(Boolean.FALSE));
        if (total == 0) {
            throw new BusinessException("该计划下暂无筛查学生数据，无法导出档案卡");
        }
    }

    @Override
    public String getRedisKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_ARCHIVES_ORG,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getClassId(),
                exportCondition.getGradeId(),
                exportCondition.getPlanStudentIds());
    }

}
