package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出学校的档案卡（压缩文件仅包含一个该学校的PDF文件）
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolArchivesService")
public class ExportSchoolArchivesService extends BaseExportPdfFileService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private GeneratePdfFileService generateReportPdfService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generateReportPdfService.generateSchoolArchivesPdfFile(fileSavePath, exportCondition.getPlanId(), exportCondition.getSchoolId());
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, school.getName());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        int total = visionScreeningResultService.count(new VisionScreeningResult().setPlanId(exportCondition.getPlanId()).setSchoolId(exportCondition.getSchoolId()).setIsDoubleScreen(Boolean.FALSE));
        if (total == 0) {
            throw new BusinessException("该计划下该学校暂无筛查学生数据，无法导出档案卡");
        }
    }
}
