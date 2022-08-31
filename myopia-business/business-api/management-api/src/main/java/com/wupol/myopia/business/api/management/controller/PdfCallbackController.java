package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    @Value("${file.temp.save-path}")
    public String pdfSavePath;

    @Resource
    private S3Utils s3Utils;

    @PostMapping("callback")
    @Transactional(rollbackFor = Exception.class)
    public synchronized void callback(@RequestBody PdfResponseDTO responseDTO) {

        if(Objects.equals(responseDTO.getStatus(), Boolean.FALSE))  {
            log.error("report callback info:{}", JSON.toJSONString(responseDTO));
            return;
        }
        String uuid = responseDTO.getUuid();

        // 通过UUID获取信息
        PdfGeneratorVO pdfGeneratorVO = (PdfGeneratorVO) redisUtil.get(uuid);

        if (Objects.isNull(pdfGeneratorVO)) {
            return;
        }
        Integer exportTotal = pdfGeneratorVO.getExportTotal();
        Integer exportCount = pdfGeneratorVO.getExportCount() + 1;
        Integer userId = pdfGeneratorVO.getUserId();
        String bucket = responseDTO.getBucket();
        String s3key = responseDTO.getS3key();
        String fileName = StringUtils.substringAfterLast(s3key, "/") + ".pdf";

        // 保存到resourceFile
        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setFileName(fileName);
        resourceFile.setBucket(bucket);
        resourceFile.setS3Key(s3key);
        resourceFileService.save(resourceFile);
        Integer fileId = resourceFile.getId();

        pdfGeneratorVO.getFileIds().add(fileId);
        pdfGeneratorVO.setFileIds(pdfGeneratorVO.getFileIds());

        if (!exportTotal.equals(exportCount)) {
            pdfGeneratorVO.setExportCount(exportCount);
            redisUtil.set(uuid, pdfGeneratorVO);
        } else {
            List<Integer> fileIds = pdfGeneratorVO.getFileIds();
            List<TwoTuple<String, String>> batchResourcePath = resourceFileService.getBatchFileNamePath(fileIds);
            String fileSaveParentPath = getFileSaveParentPath();
            String abc = Paths.get(fileSaveParentPath, pdfGeneratorVO.getFileName()).toString();
            try {
                for (TwoTuple<String, String> s : batchResourcePath) {
                    FileUtils.copyURLToFile(new URL(s.getSecond()), new File(Paths.get(abc, s.getFirst()).toString()));
                }
                File zip = ZipUtil.zip(abc);
                noticeService.sendExportSuccessNotice(userId, userId, zip.getName(), s3Utils.uploadFileToS3(zip));
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                redisUtil.del(uuid);
                FileUtil.del(new File(fileSaveParentPath));
            }
        }
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    public String getFileSaveParentPath() {
        return Paths.get(pdfSavePath, UUID.randomUUID().toString()).toString();
    }

}
