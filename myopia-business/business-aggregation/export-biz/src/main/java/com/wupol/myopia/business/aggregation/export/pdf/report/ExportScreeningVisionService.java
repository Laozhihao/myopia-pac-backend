package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.system.constants.ScreeningTypeConst;
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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 导出视力筛查
 *
 * @author Simple4H
 */
@Service
public class ExportScreeningVisionService implements ExportPdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Resource
    private Html2PdfService html2PdfService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;

    private static final String abc = "筛查报告-视力分析";

    private static final String k_report = "-视力分析【幼儿园】.pdf";

    private static final String p_report = "-视力分析【小学及以上】.pdf";


    @Override
    public Integer getScreeningType() {
        return ScreeningTypeConst.VISION;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.District)) {
            String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
            return districtName + abc;
        }

        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.SCHOOL)) {
            return schoolService.getById(exportCondition.getSchoolId()).getName() + abc;
        }
        return StrUtil.EMPTY;
    }

    @Override
    public void generateDistrictReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        generateDistrictVisionReport(exportCondition.getNotificationId(), exportCondition.getDistrictId(), fileSavePath, fileName);
    }

    @Override
    public void generateSchoolReportPdfFile(String fileSavePath, String fileName, ExportCondition exportCondition) {
        Integer schoolId = exportCondition.getSchoolId();
        Integer planId = exportCondition.getPlanId();
        List<StatConclusion> statConclusions = statConclusionService.getByPlanIdSchoolId(planId, schoolId);

        String schoolName = schoolService.getById(exportCondition.getSchoolId()).getName();

        // 幼儿园
        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            generateKindergartenVisionReport(planId, schoolId, fileSavePath, schoolName + k_report);
        }

        // 小学以上
        if (!CollectionUtils.isEmpty(statConclusions.stream().filter(grade -> !GradeCodeEnum.kindergartenSchoolCode().contains(grade.getSchoolGradeCode())).collect(Collectors.toList()))) {
            generatePrimaryVisionReport(planId, schoolId, fileSavePath, schoolName + p_report);
        }
    }

    private void generateDistrictVisionReport(Integer noticeId, Integer districtId, String fileSavePath, String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, fileName + ".pdf", UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    private void generateKindergartenVisionReport(Integer planId, Integer schoolId, String fileSavePath, String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_KINDERGARTEN_VISION, htmlUrlHost, planId, schoolId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    private void generatePrimaryVisionReport(Integer planId, Integer schoolId, String fileSavePath, String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_PRIMARY_VISION, htmlUrlHost, planId, schoolId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }
}
