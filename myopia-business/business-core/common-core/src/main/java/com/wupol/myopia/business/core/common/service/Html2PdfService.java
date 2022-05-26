package com.wupol.myopia.business.core.common.service;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.PdfRequestDTO;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.util.DateFormatUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

/**
 * HTML转换PDF
 *
 * @author Simple4H
 */
@Service("html2PdfService")
@Log4j2
public class Html2PdfService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${upload.bucketName}")
    private String bucket;

    @Value("${upload.region}")
    private String region;

    @Value("${upload.prefix}")
    private String prefix;

    @Value("${report.pdf.async-request-url}")
    private String asyncRequestUrl;

    @Value("${report.pdf.sync-request-url}")
    private String syncRequestUrl;

    @Value("${report.pdf.callbackUrl}")
    private String callbackUrl;

    /**
     * 异步导出PDF
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     */
    public PdfResponseDTO asyncGeneratorPDF(String url, String fileName, String uuid) {
        HttpEntity<String> request = getStringHttpEntity(url, fileName, uuid);
        return restTemplate.postForObject(asyncRequestUrl, request, PdfResponseDTO.class);
    }

    /**
     * 转换html页面为PDF
     *
     * @param url html 地址
     * @param fileName 文件名，如：student.pdf
     * @return java.lang.String
     **/
    public String convertHtmlToPdf(String url, String fileName) {
        PdfResponseDTO pdfResponse = syncGeneratorPDF(url, fileName);
        Assert.isTrue(pdfResponse.getStatus(), "转换html为PDF异常：" + JSONObject.toJSONString(pdfResponse));
        return pdfResponse.getUrl();
    }

    /**
     * 同步生成PDF
     *
     * @param url      文件URL
     * @param fileName 文件名，如：123.pdf
     * @return PdfResponseDTO
     */
    public PdfResponseDTO syncGeneratorPDF(String url, String fileName) {
        return syncGeneratorPDF(url, fileName, UUID.randomUUID().toString());
    }

    /**
     * 同步生成PDF
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @return PdfResponseDTO
     */
    public PdfResponseDTO syncGeneratorPDF(String url, String fileName, String uuid) {
        log.info("同步生成PDF，url = {}，fileName = {}，uuid = {}", url, fileName, uuid);
        HttpEntity<String> request = getStringHttpEntity(url, fileName, uuid);
        return restTemplate.postForObject(syncRequestUrl, request, PdfResponseDTO.class);
    }

    /**
     * 生成请求参数
     *
     * @param url      文件URL
     * @param fileName 文件名
     * @param uuid     uuid
     * @return HttpEntity<String>
     */
    private HttpEntity<String> getStringHttpEntity(String url, String fileName, String uuid) {
        PdfRequestDTO requestDTO = new PdfRequestDTO();
        requestDTO.setUrl(url);
        requestDTO.setOutput(fileName);
        requestDTO.setBucket(bucket);

        requestDTO.setRegion(region);
        requestDTO.setKeyPrefix(prefix + "/" + DateFormatUtil.format(new Date(), DateFormatUtil.FORMAT_ONLY_DATE) + "/" + uuid + "/");
        requestDTO.setUuid(uuid);
        requestDTO.setTimeout(90);
        requestDTO.setCallbackUrl(callbackUrl);

        PdfRequestDTO.Config config = new PdfRequestDTO.Config();
        config.setSize("a4");
        config.setDisplayHeaderFooter(true);
        config.setHeaderTemplate("<div></div>");
        config.setFooterTemplate("<h1>Page <span class='pageNumber'></span> of <span class='totalPages'></span></h1>");
        config.setMargin("{ \"bottom\": \"10cm\"}");
        requestDTO.setConfig(config);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(JSONObject.toJSONString(requestDTO), httpHeaders);
    }
}
