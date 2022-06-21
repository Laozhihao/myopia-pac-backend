package com.wupol.myopia.business.aggregation.export.pdf.report;

import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.util.HtmlToPdfUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.file.Paths;

/**
 * 导出视力筛查
 *
 * @author Simple4H
 */
@Service
public class ExportScreeningVisionService {

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    /**
     * 区域报告
     *
     * @param saveDirectory   保存目录
     * @param exportCondition 条件
     **/
    public void generateDistrictReportPdfFile(String saveDirectory, ExportCondition exportCondition) {
        generateVisionArchivesPDF(exportCondition.getNotificationId(), exportCondition.getDistrictId(), saveDirectory);
    }

    private void generateVisionArchivesPDF(Integer noticeId, Integer districtId, String fileSavePath) {
        String studentPdfHtmlUrl = String.format(HtmlPageUrlConstant.REPORT_AREA_VISION, htmlUrlHost, noticeId, districtId);
        Assert.isTrue(HtmlToPdfUtil.convertArchives(studentPdfHtmlUrl, Paths.get(fileSavePath).toString()), "生成区域报告PDF文件异常");
    }

}
