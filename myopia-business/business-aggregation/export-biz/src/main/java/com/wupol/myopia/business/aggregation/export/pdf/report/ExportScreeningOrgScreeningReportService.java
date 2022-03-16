package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.GeneratePdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 导出筛查机构的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service("screeningOrgScreeningReportService")
public class ExportScreeningOrgScreeningReportService extends BaseExportPdfFileService {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private GeneratePdfFileService generateReportPdfService;
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
        // 所有学校汇总 如果后期需要以整个计划导出，则可用该注释代码
        // generateReportPdfService.generateScreeningPlanReportPdfFile(fileSavePath, exportCondition.getPlanId());
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByPlanId(exportCondition.getPlanId());
        if (schoolIdList.contains(exportCondition.getSchoolId())){
            // 各个学校详情
            generateReportPdfService.generateScreeningOrgScreeningReportPdfFile(fileSavePath, exportCondition.getPlanId(),exportCondition.getSchoolId());
        }
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

        return String.format(PDFFileNameConstant.REPORT_PDF_FILE_NAME, screeningOrganization.getName());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.notNull(exportCondition.getPlanId(), "筛查计划ID不能为空");
        Assert.notNull(exportCondition.getSchoolId(), "学校ID不能为空");

    }
    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_ORG_SCREENING,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getScreeningOrgId(),
                exportCondition.getSchoolId()
        );
    }
}
