package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.domain.model.ResourceFile;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.util.S3Utils;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.management.util.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
public class CommonController {

    @Autowired
    private UploadConfig uploadConfig;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 上传图片
     */
    @PostMapping("/fileUpload")
    public Map<String, Object> fileUpload(MultipartFile file) throws AccessDeniedException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(user)) {
            throw new AccessDeniedException("请先登录");
        }
        try {
            String savePath = uploadConfig.getSavePath();
            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
            String tempPath = uploadToServerResults.getSecond();
            // 判断上传的文件是否图片或者PDF
            String allowExtension = uploadConfig.getSuffixs();
            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
            // 上传
            ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFile(tempPath, UploadUtil.genNewFileName(file));
            Map<String, Object> resultMap = new HashMap<>(16);
            resultMap.put("url", resourceFileService.getResourcePath(resourceFile.getId()));
            resultMap.put("fileId", resourceFile.getId());
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }

    /**
     * 获取图片
     */
    @GetMapping("/file/{fileId}")
    public Map<String, String> file(@PathVariable Integer fileId) throws AccessDeniedException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(user)) {
            throw new AccessDeniedException("请先登录");
        }
        Map<String, String> resultMap = new HashMap<>(16);
        resultMap.put("url", resourceFileService.getResourcePath(fileId));
        return resultMap;
    }

    /**
     * 富文本上传图片
     * 直接返回访问地址
     */
    @PostMapping("/richTextFileUpload")
    public Map<String, String> richTextFileUpload(MultipartFile file) throws AccessDeniedException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(user)) {
            throw new AccessDeniedException("请先登录");
        }
        try {
            String savePath = uploadConfig.getSavePath();
            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
            String tempPath = uploadToServerResults.getSecond();
            // 判断上传的文件是否图片或者PDF
            String allowExtension = uploadConfig.getSuffixs();
            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
            // 上传
            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("url", s3Utils.uploadStaticS3AndDeleteTempFile(tempPath, UploadUtil.genNewFileName(file)));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }
}
