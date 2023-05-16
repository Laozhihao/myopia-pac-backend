package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.alibaba.fastjson.JSON;
import com.vistel.framework.nodejs.pdf.domain.dto.response.PdfGenerateResponse;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author  钓猫的小鱼
 * @Date  2022/3/18 10:59 AM
 * @Email: shuailong.wu@vistel.cn
 * @Des: 学生档案卡-近视筛查结果记录表
 */
@Slf4j
@Service("syncExportStudentScreeningArchivesService")
public class SyncExportStudentScreeningArchivesService {
    @Autowired
    private Html2PdfService html2PdfService;

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    /**
     * 生成档档案卡路径  TODO: 废除相关接口，统一用复合接口
     * @param resultId 结果ID
     * @param templateId 模板ID
     */
    public String generateArchivesPdfUrl(Integer resultId,Integer templateId) {

        String studentScreeningArchives = String.format(HtmlPageUrlConstant.STUDENT_ARCHIVES,htmlUrlHost, resultId, templateId);
        String fileName = "学生档案卡-近视筛查结果.pdf";
        log.info("请求地址:{}", studentScreeningArchives);
        PdfGenerateResponse pdfResponse = html2PdfService.syncGeneratorPDF(studentScreeningArchives, fileName);
        log.info("响应参数:{}", JSON.toJSONString(pdfResponse));
        return pdfResponse.getUrl();
    }

}
