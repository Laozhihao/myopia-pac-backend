package com.wupol.myopia.business.management.export.report;

import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.export.BaseExportFileService;
import com.wupol.myopia.business.management.export.constant.FileNameConstant;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出行政区域的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("districtScreeningReportService")
public class ExportDistrictScreeningService extends BaseExportFileService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private GenerateReportPdfService generateReportPdfService;

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
        // 区域
        generateReportPdfService.generateDistrictPdfFile(fileSavePath, fileName, exportCondition.getNotificationId(), exportCondition.getDistrictId());
        // 学校
        generateReportPdfService.generateSchoolPdfFileByNoticeId(fileSavePath, exportCondition.getNotificationId());
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
        return String.format(FileNameConstant.PDF_REPORT_FILE_NAME, districtFullName);
    }
}
