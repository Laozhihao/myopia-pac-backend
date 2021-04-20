package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.export.BaseExportFileService;
import com.wupol.myopia.business.management.export.GeneratePdfFileService;
import com.wupol.myopia.business.management.export.constant.FileNameConstant;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出学校的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolScreeningReportService")
public class ExportSchoolScreeningService extends BaseExportFileService {

    @Autowired
    private GeneratePdfFileService generateReportPdfService;
    @Autowired
    private SchoolService schoolService;

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
        generateReportPdfService.generateSchoolScreeningReportPdfFile(fileSavePath, exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getSchoolId());
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
        return String.format(FileNameConstant.REPORT_PDF_FILE_NAME, school.getName());
    }
}
