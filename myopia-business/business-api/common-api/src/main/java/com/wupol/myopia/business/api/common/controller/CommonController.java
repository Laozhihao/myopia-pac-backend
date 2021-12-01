package com.wupol.myopia.business.api.common.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.system.domain.model.AppVersion;
import com.wupol.myopia.business.core.system.service.AppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
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
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/common")
public class CommonController {

    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private AppVersionService appVersionService;

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
            ResourceFile resourceFile = resourceFileService.uploadFileAndSave(file);
            Map<String, Object> resultMap = new HashMap<>(4);
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
            // 检查文件并保存到本地临时目录
            String tempPath = resourceFileService.checkFileAndSaveToLocal(file);
            // 上传
            Map<String, String> resultMap = new HashMap<>(4);
            resultMap.put("url", s3Utils.uploadStaticS3AndDeleteTempFile(tempPath, UploadUtil.genNewFileName(file)));
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }

    /**
     * 获取最新的一个版本
     *
     * @param packageName 包名
     * @param channel 渠道
     * @return com.wupol.myopia.business.core.system.domain.model.AppVersion
     **/
    @GetMapping("/app/version/latest")
    public AppVersion getAppLatestVersion(@NotBlank(message = "packageName不能为空") String packageName, @NotBlank(message = "channel不能为空") String channel) {
        return appVersionService.getLatestVersionByPackageNameAndChannel(packageName, channel);
    }
}
