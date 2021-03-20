package com.wupol.myopia.business.management.service;

import cn.hutool.core.util.ZipUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/20
 **/
@Service
public class ReportService {
    private static final String REPORT_HTML_PATH = "%s?notificationId=%d&districtId=%d&token=Bearer %s";

    @Value("${report.html.url-host}")
    private String htmlUrlHost;
    @Value("${report.pdf.save-path}")
    private String pdfSavePath;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DistrictService districtService;

    @Async
    public void exportDistrictReport(Integer notificationId, Integer districtId, CurrentUser currentUser) {
        // 获取该行政区下的所有学校

        // 获取token
        Object token = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getId()));
        Assert.notNull(token, "没有访问权限");
        District district = districtService.getById(districtId);
        // 生成PDF
        // 区域
        String htmlUrl = String.format(REPORT_HTML_PATH, htmlUrlHost, notificationId, districtId, token.toString());
        String pdfPath = pdfSavePath + UUID.randomUUID() + "/" + district.getName() + "/" + district.getName() + ".pdf";
        HtmlToPdfUtil.convert(htmlUrl, pdfPath);
        // 学校

        // 打包文件
        File zipFile = ZipUtil.zip(pdfPath);
        // 上传到S3

        // 发消息通知
    }
}
