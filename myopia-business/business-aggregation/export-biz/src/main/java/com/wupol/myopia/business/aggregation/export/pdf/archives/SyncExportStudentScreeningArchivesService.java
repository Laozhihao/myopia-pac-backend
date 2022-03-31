package com.wupol.myopia.business.aggregation.export.pdf.archives;/*
 * @Author  钓猫的小鱼
 * @Date  2022/3/18 10:59 AM
 * @Email: shuailong.wu@vistel.cn
 * @Des: 学生档案卡-近视筛查结果记录表
 */

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.business.aggregation.export.pdf.constant.HtmlPageUrlConstant;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service("syncExportStudentScreeningArchivesService")
public class SyncExportStudentScreeningArchivesService {
    @Autowired
    private Html2PdfService html2PdfService;

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    /**
     * 生成档档案卡路径
     * @param resultId 结果ID
     * @param templateId 模板ID
     */
    public String generateArchivesPdfUrl(Integer resultId,Integer templateId) {

        String studentScreeningArchives = String.format(HtmlPageUrlConstant.STUDENT_ARCHIVES,htmlUrlHost, resultId, templateId);
        String fileName = "学生档案卡-近视筛查结果.pdf";
        String uuid = UUID.randomUUID().toString();
        log.info("请求地址:{}", studentScreeningArchives);
        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(studentScreeningArchives, fileName , uuid);
        log.info("响应参数:{}", JSONObject.toJSONString(pdfResponseDTO));

        return pdfResponseDTO.getUrl();
    }

}
