package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.export.BaseExportFileService;
import com.wupol.myopia.business.management.export.GeneratePdfFileService;
import com.wupol.myopia.business.management.export.constant.FileNameConstant;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 导出学校的档案卡
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolArchivesService")
public class ExportSchoolArchivesService extends BaseExportFileService {

    @Autowired
    private SchoolService schoolService;
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
        return String.format(FileNameConstant.ARCHIVES_PDF_FILE_NAME, school.getName());
    }
}
