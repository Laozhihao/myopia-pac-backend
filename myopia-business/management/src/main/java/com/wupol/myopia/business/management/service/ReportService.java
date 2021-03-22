package com.wupol.myopia.business.management.service;

import cn.hutool.core.util.ZipUtil;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import com.wupol.myopia.business.management.util.S3Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/20
 **/
@Log4j2
@Service
public class ReportService {
    private static final String DISTRICT_REPORT_HTML_PATH = "%s?notificationId=%d&districtId=%d&token=Bearer %s";
    private static final String SCHOOL_REPORT_HTML_PATH = "%s?notificationId=%d&schoolId=%d&token=Bearer %s";
    private static final String PDF_REPORT_FILE_NAME = "%s筛查报告";

    @Value("${report.html.url-host}")
    private String htmlUrlHost;
    @Value("${report.pdf.save-path}")
    private String pdfSavePath;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;

    /**
     * 导出区域筛查报告
     *
     * @param notificationId 筛查通知ID
     * @param districtId 行政区域ID
     * @param currentUser 当前登录用户
     * @return void
     **/
    @Async
    public void exportDistrictReport(Integer notificationId, Integer districtId, CurrentUser currentUser) throws UtilException {
        // 获取token
        Object token = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getId()));
        Assert.notNull(token, "没有访问权限");
        // 生成PDF
        // 1.区域
        District district = districtService.getById(districtId);
        String districtFullName = districtService.getTopDistrictName(district.getCode());
        String districtReportFileName = String.format(PDF_REPORT_FILE_NAME, districtFullName);
        String htmlUrl = String.format(DISTRICT_REPORT_HTML_PATH, htmlUrlHost, notificationId, districtId, token.toString());
        String pdfFileParentPath = Paths.get(pdfSavePath, UUID.randomUUID().toString(), districtReportFileName).toString();
        try {
            boolean isSuccessful = HtmlToPdfUtil.convert(htmlUrl, Paths.get(pdfFileParentPath, districtReportFileName + ".pdf").toString());
            Assert.isTrue(isSuccessful, "【生成区域报告异常】：" + districtFullName);
            // 2.学校
            // 获取该行政区下的所有学校
            List<Integer> schoolIdList = statConclusionService.getSchoolIdByNoticeId(notificationId);
            schoolIdList.forEach(schoolId -> {
                School school = schoolService.getById(schoolId);
                String schoolReportFileName = String.format(PDF_REPORT_FILE_NAME, school.getName());
                String schoolPdfHtmlUrl = String.format(SCHOOL_REPORT_HTML_PATH, htmlUrlHost, notificationId, schoolId, token.toString());
                Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(pdfFileParentPath, schoolReportFileName + ".pdf").toString()), "【生成区域报告异常】：" + school.getName());
            });
        } catch (Exception e) {
            log.error("【生成区域报告异常】：" + districtFullName, e);
        }
        // 打包文件
        File zipFile = ZipUtil.zip(pdfFileParentPath);
        // 上传到S3
        Integer fileId = s3Utils.uploadFile(zipFile);
        // 发消息通知
        String content = String.format(CommonConst.CONTENT, districtFullName, "筛查报告", new Date());
        noticeService.createExportNotice(currentUser.getId(), currentUser.getId(), content, content, fileId, CommonConst.NOTICE_STATION_LETTER);
    }
}
