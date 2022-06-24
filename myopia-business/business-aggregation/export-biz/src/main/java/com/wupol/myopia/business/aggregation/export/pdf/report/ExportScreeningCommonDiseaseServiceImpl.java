package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.system.constants.ScreeningTypeConst;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * 导出常见病筛查报告
 *
 * @author hang.yuan 2022/6/23 10:31
 */
@Service
public class ExportScreeningCommonDiseaseServiceImpl implements ExportPdfFileService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Autowired
    private Html2PdfService html2PdfService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;


    @Override
    public void generateDistrictReportPdfFile(String fileSavePath,String fileName, ExportCondition exportCondition) {
        generateDistrictCommonDiseaseReport(exportCondition.getNotificationId(), exportCondition.getDistrictId(), fileSavePath,fileName);
    }

    private void generateDistrictCommonDiseaseReport(Integer noticeId, Integer districtId, String fileSavePath,String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.DISTRICT_COMMON_DISEASE, htmlUrlHost,  districtId,noticeId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl, fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }
    }

    @Override
    public void generateSchoolReportPdfFile(String fileSavePath,String fileName, ExportCondition exportCondition) {
        generateSchoolCommonDiseaseReport(exportCondition.getPlanId(), exportCondition.getSchoolId(), fileSavePath,fileName);
    }

    private void generateSchoolCommonDiseaseReport(Integer planId, Integer schoolId, String fileSavePath,String fileName) {
        String reportHtmlUrl = String.format(HtmlPageUrlConstant.SCHOOL_COMMON_DISEASE, htmlUrlHost, schoolId,planId);
        String pdfUrl = html2PdfService.syncGeneratorPDF(reportHtmlUrl,fileName, UUID.randomUUID().toString()).getUrl();
        try {
            FileUtils.copyURLToFile(new URL(pdfUrl), new File(Paths.get(fileSavePath).toString()));
        } catch (IOException e) {
            throw new BusinessException("生成学校报告PDF文件异常", e);
        }
    }

    @Override
    public Integer getScreeningType() {
        return ScreeningTypeConst.COMMON_DISEASE;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        if (Objects.equals(exportCondition.getExportType(), ExportTypeConst.District)){
            String districtName = districtService.getDistrictNameByDistrictId(exportCondition.getDistrictId());
            return districtName+"区域筛查报告-常见病分析.pdf";
        }
        if (Objects.equals(exportCondition.getExportType(),ExportTypeConst.SCHOOL)){
            School school = schoolService.getById(exportCondition.getSchoolId());
            return school.getName()+"学校筛查报告-常见病分析.pdf";
        }
        return StrUtil.EMPTY;
    }
}
