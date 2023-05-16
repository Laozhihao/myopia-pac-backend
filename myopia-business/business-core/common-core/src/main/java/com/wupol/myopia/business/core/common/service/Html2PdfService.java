package com.wupol.myopia.business.core.common.service;

import com.alibaba.fastjson.JSON;
import com.vistel.framework.nodejs.pdf.client.NodeJSPdfGeneratorBusinessClient;
import com.vistel.framework.nodejs.pdf.domain.constant.WaitUntil;
import com.vistel.framework.nodejs.pdf.domain.dto.config.PageConfig;
import com.vistel.framework.nodejs.pdf.domain.dto.config.PageMargin;
import com.vistel.framework.nodejs.pdf.domain.dto.request.PdfHttpCallbackRequestDto;
import com.vistel.framework.nodejs.pdf.domain.dto.response.PdfGenerateResponse;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * HTML转换PDF
 *
 * @author Simple4H
 */
@Service("html2PdfService")
@Log4j2
@ConditionalOnProperty(name = "myopia.upload.enabled", havingValue = "true", matchIfMissing = true)
public class Html2PdfService {

    /** 超时时间，秒 */
    public static final int TIME_OUT_SECONDS = 90;

    @Value("${report.pdf.callbackUrl}")
    private String callbackUrl;

    @Autowired
    private UploadConfig uploadConfig;


    @Resource
    private NodeJSPdfGeneratorBusinessClient nodeJSPdfGeneratorBusinessClient;

    /**
     * 异步导出PDF
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     */
    public PdfGenerateResponse asyncGeneratorPDF(String url, String fileName, String uuid) {
        PdfHttpCallbackRequestDto pdfHttpCallbackRequestDto = getPdfHttpCallbackRequestDto(url, fileName, uuid, Boolean.TRUE);
        return nodeJSPdfGeneratorBusinessClient.asyncGeneratePdfWithPresignedUrl(pdfHttpCallbackRequestDto);
    }

    /**
     * 转换html页面为PDF
     *
     * @param url html 地址
     * @param fileName 文件名，如：student.pdf
     * @return java.lang.String
     **/
    public String convertHtmlToPdf(String url, String fileName) {
        PdfGenerateResponse pdfResponse = syncGeneratorPDF(url, fileName);
        log.info("【请求node-js服务】响应：{}", JSON.toJSONString(pdfResponse));
        return pdfResponse.getUrl();
    }

    /**
     * 同步生成PDF
     *
     * @param url      文件URL
     * @param fileName 文件名，如：123.pdf
     * @return PdfResponseDTO
     */
    public PdfGenerateResponse syncGeneratorPDF(String url, String fileName) {
        return syncGeneratorPDF(url, fileName, UUID.randomUUID().toString());
    }

    /**
     * 同步生成PDF (视力分析或者常见病5份大报告专用)
     *
     * @param url      文件URL
     * @param fileName 文件名，如：123.pdf
     * @return PdfResponseDTO
     */
    public PdfGenerateResponse syncGeneratorPdfSpecial(String url, String fileName) {
        return syncGeneratorPdfSpecial(url, fileName, UUID.randomUUID().toString());
    }

    /**
     * 同步生成PDF
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @return PdfResponseDTO
     */
    public PdfGenerateResponse syncGeneratorPDF(String url, String fileName, String uuid) {
        log.info("【同步生成PDF】url = {}，fileName = {}，uuid = {}", url, fileName, uuid);
        PdfHttpCallbackRequestDto pdfHttpCallbackRequestDto = getPdfHttpCallbackRequestDto(url, fileName, uuid);
        log.info("【请求node-js服务】：{}", JSON.toJSONString(pdfHttpCallbackRequestDto));
        return nodeJSPdfGeneratorBusinessClient.syncGeneratePdfWithPresignedUrl(pdfHttpCallbackRequestDto);
    }

    /**
     * 同步生成PDF (视力分析或者常见病5份大报告专用)
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @return PdfResponseDTO
     */
    public PdfGenerateResponse syncGeneratorPdfSpecial(String url, String fileName, String uuid) {
        log.info("【同步生成PDF,专用方法】url = {}，fileName = {}，uuid = {}", url, fileName, uuid);
        PdfHttpCallbackRequestDto pdfHttpCallbackRequestDto = getPdfHttpCallbackRequestDto(url, fileName, uuid, Boolean.TRUE);
        log.info("【请求node-js服务,专用方法】：{}", JSON.toJSONString(pdfHttpCallbackRequestDto));
        return nodeJSPdfGeneratorBusinessClient.syncGeneratePdfWithPresignedUrl(pdfHttpCallbackRequestDto);
    }

    /**
     * 生成请求参数
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @return HttpEntity<String>
     */
    private PdfHttpCallbackRequestDto getPdfHttpCallbackRequestDto(String url, String fileName, String uuid) {
        return getPdfHttpCallbackRequestDto(url,fileName,uuid,Boolean.FALSE);
    }

    /**
     * 生成请求参数
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @param isReport   是否是视力分析或者常见病5份大报告
     * @return PdfHttpCallbackRequestDto
     */
    private PdfHttpCallbackRequestDto getPdfHttpCallbackRequestDto(String url, String fileName, String uuid, Boolean isReport) {
        PdfHttpCallbackRequestDto pdfHttpCallbackRequestDto = new PdfHttpCallbackRequestDto();
        pdfHttpCallbackRequestDto.setUrl(url);
        pdfHttpCallbackRequestDto.setOutput(fileName);
        pdfHttpCallbackRequestDto.setBucket(uploadConfig.getBucketName());
        pdfHttpCallbackRequestDto.setRegion(uploadConfig.getRegion());
        pdfHttpCallbackRequestDto.setKeyPrefix(uploadConfig.getPrefix() + "/" + DateFormatUtil.format(new Date(), DateFormatUtil.FORMAT_ONLY_DATE) + "/" + uuid);
        pdfHttpCallbackRequestDto.setUuid(uuid);
        pdfHttpCallbackRequestDto.setTimeout(TIME_OUT_SECONDS);
        pdfHttpCallbackRequestDto.setCallbackUrl(callbackUrl);

        PageConfig config = new PageConfig();
        config.setSize("a4");
        config.setHeaderTemplate("<div></div>");
        config.setMargin(new PageMargin().setBottom("10cm"));
        config.setWaitUntilType(WaitUntil.NETWORK_IDLE0);
        if (Objects.equals(isReport, Boolean.TRUE)){
            config.setDisplayHeaderFooter(true);
            config.setFooterTemplate("<div style='font-size: 8px; text-align: right; width: 95%;'><span>致远青眸-儿童青少年近视防控平台</span> <span class='pageNumber' style='display: inline-block; margin-left: 5px'></span> - <span class='totalPages'></span></div>");
            config.setSelector(".layout");
        }else {
            config.setDisplayHeaderFooter(false);
            config.setFooterTemplate("<h1>Page <span class='pageNumber'></span> of <span class='totalPages'></span></h1>");
        }
        pdfHttpCallbackRequestDto.setConfig(config);
        return pdfHttpCallbackRequestDto;
    }
}
