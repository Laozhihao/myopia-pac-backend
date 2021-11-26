package com.wupol.myopia.business.core.common.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.mapper.ResourceFileMapper;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.util.S3Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-02-04
 */
@Service
@Slf4j
public class ResourceFileService extends BaseService<ResourceFileMapper, ResourceFile> {

    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private UploadConfig uploadConfig;

    /**
     * 根据文件Id获取路径
     * @param fileId
     * @return
     */
    public String getResourcePath(Integer fileId) {
        ResourceFile file = getById(fileId);
        if (Objects.isNull(file)) {
            return null;
        }
        return s3Utils.getResourcePath(file.getBucket(), file.getS3Key());
    }

    /**
     * 根据文件Id获取路径
     * @param fileIdList
     * @return
     */
    public List<String> getBatchResourcePath(List<Integer> fileIdList) {
        if (CollectionUtils.isEmpty(fileIdList)) {
            return Collections.emptyList();
        }
        return fileIdList.stream().map(this::getResourcePath).collect(Collectors.toList());
    }

    /**
     * 上传文件，并保存入库
     *
     * @param file 文件
     * @return com.wupol.myopia.business.core.common.domain.model.ResourceFile
     **/
    public ResourceFile uploadFileAndSave(MultipartFile file) throws UtilException {
        // 检查文件并保存到本地临时目录
        String tempPath = checkFileAndSaveToLocal(file);
        // 上传文件
        return s3Utils.uploadS3AndGetResourceFile(tempPath, UploadUtil.genNewFileName(file));
    }

    /**
     * 上传文件，并保存入库
     *
     * @param file 文件
     * @param fileExtensions 允许的后缀列表
     * @return com.wupol.myopia.business.core.common.domain.model.ResourceFile
     **/
    public ResourceFile uploadFileAndSave(MultipartFile file, String... fileExtensions) throws UtilException {
        // 检查文件并保存到本地临时目录
        String tempPath = checkFileAndSaveToLocal(file, fileExtensions);
        // 上传文件
        return s3Utils.uploadS3AndGetResourceFile(tempPath, UploadUtil.genNewFileName(file));
    }

    /**
     * 检查文件有效性并保存到本地
     *
     * @param file 文件
     * @return java.lang.String
     **/
    public String checkFileAndSaveToLocal(MultipartFile file) {
        // 判断上传的文件是否图片或者PDF
        String allowExtension = uploadConfig.getSuffixs();
        return checkFileAndSaveToLocal(file, allowExtension.split(","));
    }

    /**
     * 检查文件有效性并保存到本地
     *
     * @param file 文件
     * @param fileExtensions 允许的后缀列表
     * @return java.lang.String
     **/
    public String checkFileAndSaveToLocal(MultipartFile file, String... fileExtensions) {
        // 判断上传的文件类型是否允许
        UploadUtil.validateFileIsAllowed(file, fileExtensions);
        // 保存到本地临时目录
        String savePath = uploadConfig.getSavePath();
        TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
        return uploadToServerResults.getSecond();
    }

}
