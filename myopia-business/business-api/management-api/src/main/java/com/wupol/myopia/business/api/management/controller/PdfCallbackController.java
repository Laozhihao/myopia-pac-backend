package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

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
    private RedisUtil redisUtil;

    @Value("${file.temp.save-path}")
    public String pdfSavePath;

    @Resource
    private S3Utils s3Utils;

    @PostMapping("callback")
    @Transactional(rollbackFor = Exception.class)
    public synchronized void callback(@RequestBody PdfResponseDTO responseDTO) {

        String key = StringUtils.substringBefore(responseDTO.getUuid(), StrUtil.SLASH);
        PdfGeneratorVO pdfGeneratorVO = (PdfGeneratorVO) redisUtil.get(key);
        if (Objects.isNull(pdfGeneratorVO)) {
            return;
        }
        // 如果次数相同，则压缩文件
        int currentCount = pdfGeneratorVO.getExportCount() + 1;
        boolean isFinish = pdfGeneratorVO.getExportTotal().equals(currentCount);
        com.wupol.myopia.business.common.utils.util.FileUtils.downloadFile(responseDTO.getUrl(), Paths.get(pdfSavePath, responseDTO.getUuid()).toString());
        if (isFinish) {
            String srcPath = Paths.get(pdfSavePath, key).toString();
            String zipFileName = pdfGeneratorVO.getZipFileName();
            try {
                File file = FileUtil.rename(ZipUtil.zip(srcPath), zipFileName, true, true);
                noticeService.sendExportSuccessNotice(pdfGeneratorVO.getUserId(), pdfGeneratorVO.getUserId(), file.getName(), s3Utils.uploadFileToS3(file));
                FileUtil.del(file);
            } catch (UtilException e) {
                log.error("PDF请求回调异常, 请求参数:{}", JSON.toJSONString(responseDTO), e);
                noticeService.sendExportFailNotice(pdfGeneratorVO.getUserId(), pdfGeneratorVO.getUserId(), "【导出失败】，" + zipFileName + "请稍后重试");
            } finally {
                redisUtil.del(key);
                FileUtil.del(srcPath);
            }
            return;
        }
        pdfGeneratorVO.setExportCount(currentCount);
        redisUtil.set(key, pdfGeneratorVO);
    }
}
