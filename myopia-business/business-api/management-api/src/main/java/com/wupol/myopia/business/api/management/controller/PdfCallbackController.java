package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * pdf回调地址
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/pdf")
@Log4j2
public class PdfCallbackController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private RedisUtil redisUtil;

    @Value("${report.pdf.callbackUrl}")
    private String callbackUrl;

    @PostMapping("callback")
    @Transactional(rollbackFor = Exception.class)
    public void callback(@RequestBody PdfResponseDTO responseDTO) {
        String uuid = responseDTO.getUuid();

        // 通过UUID获取信息
        PdfGeneratorVO pdfGeneratorVO = (PdfGeneratorVO) redisUtil.get(uuid);
        String fileName = pdfGeneratorVO.getFileName();
        Integer userId = pdfGeneratorVO.getUserId();
        String bucket = responseDTO.getBucket();
        String s3key = responseDTO.getS3key();

        // 保存到resourceFile
        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setFileName(fileName);
        resourceFile.setBucket(bucket);
        resourceFile.setS3Key(s3key);

        resourceFileService.save(resourceFile);
        noticeService.sendExportSuccessNotice(userId, userId, fileName, resourceFile.getId());
        redisUtil.del(uuid);
        log.info(JSONObject.toJSONString(responseDTO));
    }

    @GetMapping("getCallbackUrl")
    public void getCallbackUrl() {
        log.info(callbackUrl);
    }

}
