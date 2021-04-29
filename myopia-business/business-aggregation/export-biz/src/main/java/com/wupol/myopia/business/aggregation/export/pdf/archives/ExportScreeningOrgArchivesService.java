package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.business.aggregation.export.pdf.BaseExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.FileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出筛查机构的档案卡
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("screeningOrgArchivesService")
public class ExportScreeningOrgArchivesService extends BaseExportFileService {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private GeneratePdfFileService generateReportPdfService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void generateFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
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
        return String.format(FileNameConstant.ARCHIVES_PDF_FILE_NAME, screeningOrganization.getName());
    }

}
