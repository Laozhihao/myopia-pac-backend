package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 导出视力筛查
 *
 * @author Simple4H
 */
@Service
public class ExportScreeningVisionService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Resource
    private Html2PdfService html2PdfService;

    @Resource
    private StatConclusionService statConclusionService;

    /**
     * 区域报告
     *
     * @param saveDirectory   保存目录
     * @param exportCondition 条件
     **/
    public void generateDistrictReportPdfFile(String saveDirectory, ExportCondition exportCondition) {
        generateVisionReport(exportCondition.getNotificationId(), exportCondition.getDistrictId(), saveDirectory);
    }

    /**
     * 学校报告
     *
     * @param fileSavePath    保存目录
     * @param exportCondition 条件
     **/
    public void generateSchoolReportPdfFile(String fileSavePath, ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        Integer noticeId = exportCondition.getNotificationId();
        List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolIdNoticeId(planId, schoolId, noticeId);

        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            generateKindergartenVisionReport(planId, schoolId, noticeId, fileSavePath);
        }
        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            generatePrimaryVisionReport(planId, schoolId, noticeId, fileSavePath);
        }
    }


    private void generateVisionReport(Integer noticeId, Integer districtId, String fileSavePath) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, "区域报告.pdf", UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    private void generateKindergartenVisionReport(Integer planId, Integer schoolId, Integer noticeId, String fileSavePath) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, planId, schoolId, noticeId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, "幼儿园筛查报告-视力分析.pdf", UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    private void generatePrimaryVisionReport(Integer planId, Integer schoolId, Integer noticeId, String fileSavePath) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, planId, schoolId, noticeId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, "小学筛查报告-视力分析.pdf", UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }
}
