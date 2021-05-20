package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Objects;

/**
 * 导出学校的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("schoolScreeningReportService")
public class ExportSchoolScreeningService extends BaseExportPdfFileService {

    @Autowired
    private GeneratePdfFileService generateReportPdfService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StatConclusionService statConclusionService;

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
        return String.format(PDFFileNameConstant.REPORT_PDF_FILE_NAME, school.getName());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) throws IOException {
        Assert.isTrue(Objects.nonNull(exportCondition.getNotificationId()) || Objects.nonNull(exportCondition.getPlanId()), "筛查通知ID和筛查计划ID都为空");
        int total = statConclusionService.count(new StatConclusion().setSrcScreeningNoticeId(exportCondition.getNotificationId()).setPlanId(exportCondition.getPlanId()).setSchoolId(exportCondition.getSchoolId()));
        if (total == 0) {
            throw new BusinessException("暂无筛查数据，无法导出筛查报告");
        }
    }
}
