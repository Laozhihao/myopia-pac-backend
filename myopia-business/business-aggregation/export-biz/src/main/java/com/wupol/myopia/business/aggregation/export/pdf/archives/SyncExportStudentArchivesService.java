package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 同步导出学生档案卡
 *
 * @author Simple4H
 */
@Service("studentArchivesService")
public class SyncExportStudentArchivesService extends BaseExportPdfFileService {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private GeneratePdfFileService generatePdfFileService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     */
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        generatePdfFileService.generateStudentArchivesPdfFile(exportCondition, fileSavePath, fileName);
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


}
