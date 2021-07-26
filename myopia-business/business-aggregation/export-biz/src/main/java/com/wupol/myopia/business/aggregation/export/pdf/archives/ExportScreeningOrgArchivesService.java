package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
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
    private ScreeningOrganizationService screeningOrganizationService;
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
        generateReportPdfService.generateScreeningOrgArchivesPdfFile(fileSavePath, exportCondition.getPlanId());
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(exportCondition.getScreeningOrgId());
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, screeningOrganization.getName());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        int total = visionScreeningResultService.count(new VisionScreeningResult().setScreeningOrgId(exportCondition.getScreeningOrgId()).setPlanId(exportCondition.getPlanId()).setIsDoubleScreen(Boolean.FALSE));
        if (total == 0) {
            throw new BusinessException("该计划下暂无筛查学生数据，无法导出档案卡");
        }
    }

}
