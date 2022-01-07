package com.wupol.myopia.base.service;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.PdfRequestDTO;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    @Value("${upload.prefix}")
    private String prefix;

    @Value("${report.pdf.async-request-url}")
    private String asyncRequestUrl;

    @Value("${report.pdf.callbackUrl}")
    private String callbackUrl;

    public void sendRequest(String fileName, String UUID) {
        PdfRequestDTO requestDTO = new PdfRequestDTO();
        requestDTO.setUrl("https://t-myopia-pac-report.tulab.cn?planId=9&schoolId=14");
        requestDTO.setOutput(fileName);
        requestDTO.setBucket(bucket);
        requestDTO.setKeyPrefix(prefix);
        requestDTO.setUuid(UUID);
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
        HttpEntity<String> request = new HttpEntity<>(JSONObject.toJSONString(requestDTO), httpHeaders);


        PdfResponseDTO pdfResponseDTO = restTemplate.postForObject(asyncRequestUrl, request, PdfResponseDTO.class);
        log.info(JSONObject.toJSONString(pdfResponseDTO));
    }
}
