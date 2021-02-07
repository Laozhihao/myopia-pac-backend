package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.domain.UploadFileInfo;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.domain.model.DataCommit;
import com.wupol.myopia.business.management.domain.model.ResourceFile;
import com.wupol.myopia.business.management.exception.UploadException;
import com.wupol.myopia.business.management.service.DataCommitService;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.management.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
//    @Autowired
//    private ImageAnalysis imageAnalysis;
    @Autowired
    private ResourceFileService resourceFileService;

    private final static String FILE_URI = "/management/common/file/%s";
    /**
     * 上传图片
     */
    @PostMapping("/fileUpload")
    public String fileUpload(MultipartFile file) throws AccessDeniedException {
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

            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            // 上传
            ResourceFile resourceFile = uploadS3(tempPath, originalFilename);
            return String.format(FILE_URI, resourceFile.getId());
        } catch (Exception e) {
            throw new UploadException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }

    /**
     * 上传图片
     */
    @GetMapping("/file/{fileId}")
    public String file(@PathVariable Integer fileId) throws AccessDeniedException {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (Objects.isNull(user)) {
//            throw new AccessDeniedException("请先登录");
//        }
        return resourceFileService.getResourcePath(fileId);
    }

    /**
     * 上传文件到S3
     *
     * @param fileTempPath
     * @param originalFilename
     * @return
     * @throws UtilException
     */
    private ResourceFile uploadS3(String fileTempPath, String originalFilename) throws UtilException {
        String bucket = uploadConfig.getBucketName();
        List<UploadFileInfo> uploadFileInfos = getUploadFileInfoForUpload(fileTempPath);
        List<String> keys = uploadFiles(bucket, uploadFileInfos);
        ResourceFile file = new ResourceFile().setBucket(bucket).setS3Key(keys.get(0)).setFileName(originalFilename);
        resourceFileService.save(file);
        return file;
    }

    /**
     * 根据文件路径组装ImageAnalysis接口可用的数据形式
     */
    private List<UploadFileInfo> getUploadFileInfoForUpload(String imgPath) {
        List<UploadFileInfo> fileList = new ArrayList<>();
        fileList.add(new UploadFileInfo(imgPath));
        return fileList;
    }

    /**
     * 上传文件
     *
     * @param bucket
     * @param uploadFileInfos
     * @return
     */
    public List<String> uploadFiles(String bucket, List<UploadFileInfo> uploadFileInfos) throws UtilException {
//        return imageAnalysis.uploadFiles(bucket, uploadFileInfos);
        return Collections.emptyList();
    }
}
