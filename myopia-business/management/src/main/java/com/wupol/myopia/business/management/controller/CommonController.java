package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.domain.UploadFileInfo;
import com.vistel.Interface.exception.UtilException;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.framework.core.util.DateUtil;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.domain.model.DataCommit;
import com.wupol.myopia.business.management.domain.model.ResourceFile;
import com.wupol.myopia.business.management.exception.UploadException;
import com.wupol.myopia.business.management.service.DataCommitService;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.util.S3Utils;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.management.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.util.*;

/**
 * 公共的API接口
 *
 * @author Alix
 * @Date 2021-02-03
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/common")
public class CommonController extends BaseController<DataCommitService, DataCommit> {

    @Autowired
    private UploadConfig uploadConfig;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private ResourceFileService resourceFileService;

    private final static String FILE_URI = "/management/common/file/%s";

    /**
     * 上传图片
     */
    @PostMapping("/fileUpload")
    public Object fileUpload(MultipartFile file) throws AccessDeniedException {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (Objects.isNull(user)) {
//            throw new AccessDeniedException("请先登录");
//        }
        try {
            String savePath = uploadConfig.getSavePath();
            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
            String originalFilename = uploadToServerResults.getFirst();
            String tempPath = uploadToServerResults.getSecond();
            // 判断上传的文件是否图片或者PDF
            String allowExtension = uploadConfig.getSuffixs();
            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
            // 上传
            ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFile(tempPath, originalFilename);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", String.format(FILE_URI, resourceFile.getId()));
            return resultMap;
        } catch (Exception e) {
            throw new UploadException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }

    /**
     * 获取图片
     */
    @GetMapping("/file/{fileId}")
    public Object file(@PathVariable Integer fileId) throws AccessDeniedException {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (Objects.isNull(user)) {
//            throw new AccessDeniedException("请先登录");
//        }
        Map<String, String> resultMap = new HashMap<>(16);
        resultMap.put("url", resourceFileService.getResourcePath(fileId));
        return resultMap;
    }

    /**
     * 富文本上传图片
     * 直接返回访问地址
     */
    @PostMapping("/richTextFileUpload")
    public Object richTextFileUpload(MultipartFile file) throws AccessDeniedException {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (Objects.isNull(user)) {
//            throw new AccessDeniedException("请先登录");
//        }
        try {
            String savePath = uploadConfig.getSavePath();
            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
            String originalFilename = uploadToServerResults.getFirst();
            String tempPath = uploadToServerResults.getSecond();
            // 判断上传的文件是否图片或者PDF
            String allowExtension = uploadConfig.getSuffixs();
            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
            // 上传
            ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFile(tempPath, originalFilename);
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.getResourcePath(resourceFile.getBucket(), resourceFile.getS3Key()));
            return resultMap;
        } catch (Exception e) {
            throw new UploadException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }
}
