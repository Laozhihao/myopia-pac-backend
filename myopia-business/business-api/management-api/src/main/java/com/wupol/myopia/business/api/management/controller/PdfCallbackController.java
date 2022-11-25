package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.common.utils.util.FileUtils;
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
        String exportUuid = StringUtils.substringBefore(responseDTO.getUuid(), StrUtil.SLASH);
        // Redis Key
        String key = String.format(RedisConstant.FILE_EXPORT_ASYNC_TASK_KEY, exportUuid);
        PdfGeneratorVO pdfGeneratorVO = (PdfGeneratorVO) redisUtil.get(key);

        if (Objects.equals(responseDTO.getStatus(), Boolean.FALSE)
                || Objects.isNull(pdfGeneratorVO)
                || Objects.equals(pdfGeneratorVO.getStatus(), Boolean.FALSE)) {
            redisUtil.del(key);
            if (Objects.nonNull(pdfGeneratorVO) && Objects.nonNull(pdfGeneratorVO.getLockKey())) {
                noticeService.sendErrorNotice(exportUuid, pdfGeneratorVO);
                redisUtil.del(pdfGeneratorVO.getLockKey());
            }
            return;
        }
        // 统计次数
        int currentCount = pdfGeneratorVO.getExportCount() + 1;
        boolean isFinish = pdfGeneratorVO.getExportTotal().equals(currentCount);
        // 下载文件
        try {
            FileUtils.downloadFile(responseDTO.getUrl(), Paths.get(pdfSavePath, responseDTO.getUuid()).toString());
        } catch (Exception e) {
            redisUtil.del(key);
            noticeService.sendErrorNotice(exportUuid, pdfGeneratorVO);
            redisUtil.del(pdfGeneratorVO.getLockKey());
        }

        // 如果没有完成，则更新次数
        if (!isFinish) {
            pdfGeneratorVO.setExportCount(currentCount);
            redisUtil.set(key, pdfGeneratorVO);
            return;
        }
        // 如果次数相同，则压缩文件
        try {
            String zipFileName = pdfGeneratorVO.getZipFileName();
            File file = FileUtil.rename(ZipUtil.zip(Paths.get(pdfSavePath, exportUuid).toString()), zipFileName, true, true);
            noticeService.sendExportSuccessNotice(pdfGeneratorVO.getUserId(), pdfGeneratorVO.getUserId(), zipFileName, s3Utils.uploadFileToS3(file));
            FileUtil.del(file);
        } catch (Exception e) {
            log.error("PDF请求回调异常, 请求参数:{}", JSON.toJSONString(responseDTO), e);
            noticeService.sendExportFailNotice(pdfGeneratorVO.getUserId(), pdfGeneratorVO.getUserId(), "【导出失败】，" + pdfGeneratorVO.getZipFileName() + "请稍后重试");
        } finally {
            redisUtil.del(key);
            redisUtil.del(pdfGeneratorVO.getLockKey());
            FileUtil.del(Paths.get(pdfSavePath, exportUuid).toString());
        }
    }
}
