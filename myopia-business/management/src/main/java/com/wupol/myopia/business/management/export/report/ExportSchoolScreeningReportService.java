package com.wupol.myopia.business.management.export.report;

import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import org.springframework.stereotype.Service;

/**
 * 导出学校的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolScreeningReportService")
public class ExportSchoolScreeningReportService extends AbstractExportReportFileService {

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
        generateSchoolPdfFile(fileSavePath, exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getSchoolId());
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
        return String.format(PDF_REPORT_FILE_NAME, school.getName());
    }
}
