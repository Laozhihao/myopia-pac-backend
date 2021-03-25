package com.wupol.myopia.business.management.export.report;

import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.file.Paths;
import java.util.List;

/**
 * 导出行政区域的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service("districtScreeningReportService")
public class ExportDistrictScreeningReportService extends AbstractExportReportFileService {
    /**
     * 行政区域报告HTML页面地址
     **/
    private static final String DISTRICT_REPORT_HTML_URL = "%s?notificationId=%d&districtId=%d";

    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;

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
        createDistrictPdfFile(fileSavePath, fileName, exportCondition.getNotificationId(), exportCondition.getDistrictId());
        // 学校
        createSchoolPdfFileByNoticeId(fileSavePath, exportCondition.getNotificationId());
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
        return String.format(PDF_REPORT_FILE_NAME, districtFullName);
    }

    /**
     * 生成行政区区域PDF报告文件
     *
     * @param saveDirectory 文件保存目录
     * @param fileName 文件名
     * @param noticeId 筛查通知ID
     * @param districtId 行政区域ID
     * @return void
     **/
    private void createDistrictPdfFile(String saveDirectory, String fileName, Integer noticeId, Integer districtId) {
        String htmlUrl = String.format(DISTRICT_REPORT_HTML_URL, htmlUrlHost, noticeId, districtId);
        boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(saveDirectory, fileName + ".pdf").toString());
        Assert.isTrue(isSuccessful, "【生成行政区域报告异常】" + fileName);
    }

    /**
     * 根据筛查通知ID，生成学校的筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @return void
     **/
    private void createSchoolPdfFileByNoticeId(String saveDirectory, Integer noticeId) {
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByNoticeId(noticeId);
        generateSchoolPdfFileBatch(saveDirectory, noticeId, null, schoolIdList);
    }
}
