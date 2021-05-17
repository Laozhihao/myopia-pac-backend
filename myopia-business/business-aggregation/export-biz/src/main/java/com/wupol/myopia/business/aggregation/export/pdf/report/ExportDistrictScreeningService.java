package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出行政区域的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("districtScreeningReportService")
public class ExportDistrictScreeningService extends BaseExportPdfFileService {

    @Autowired
    private DistrictService districtService;
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
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        // 区域筛查报告
        generateReportPdfService.generateDistrictScreeningReportPdfFile(fileSavePath, fileName, exportCondition.getNotificationId(), exportCondition.getDistrictId());
        // 学校筛查报告
        generateReportPdfService.generateSchoolScreeningReportPdfFileByNoticeId(fileSavePath, exportCondition.getNotificationId(), exportCondition.getDistrictId());
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.getById(exportCondition.getDistrictId());
        String districtFullName = districtService.getTopDistrictName(district.getCode());
        return String.format(PDFFileNameConstant.REPORT_PDF_FILE_NAME, districtFullName);
    }
}
