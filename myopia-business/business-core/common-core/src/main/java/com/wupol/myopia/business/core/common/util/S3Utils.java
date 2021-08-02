package com.wupol.myopia.business.core.common.util;

import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.exception.UtilException;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.framework.core.util.DateUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.core.common.constant.FileCacheKey;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Author: jacob
 * Date: 2020/6/17 14:38
 * version: 1.1.0
 * 从imageService中解耦出来,单独做一个工具类
 * <p>工具类,用于下载影像,或者将影像转换成url等功能;
 * </p>
 */
@Slf4j
@Component
public final class S3Utils {
    @Autowired
    private S3Client s3Client;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UploadConfig uploadConfig;
    @Autowired
    private ResourceFileService resourceFileService;

    public static final String CACHE_IMAGE_PREFIX = "cache_image_";
    public static final String CACHE_FILE_PREFIX = "cache_file_";
    /**
     * 默认是1个小时
     */
    public static final Long URL_DEFAULT_SECONDS =  60 * 60L;
    /**
     * 6个小时
     */
    public static final Long URL_SIX_HOURS_EXPIRATION = 6 * 60 * 60L;
    /**
     * 1天
     */
    public static final Long URL_ONE_DAY_HOURS_EXPIRATION = 24 * 60 * 60L;
    /**
     * 已发布的pdf报告路径
     */
    private static final String PDF_PUBLISH_KEY_FORMAT = "myopia/pdf/%s/%s";
    /**
     * 文件路径key
     */
    private static final String S3_STATIC_KEY_FORMAT = "%s/%s";
    /**
     * 文件路径key
     */
    private static final String S3_KEY_FORMAT = "%s/%s/%s/%s";

    /**
     * 上传文件到S3静态bucket
     *
     * @param fileTempPath
     * @param fileName
     * @return
     * @throws UtilException
     */
    public String uploadStaticS3AndDeleteTempFile(String fileTempPath, String fileName) throws UtilException {
        String bucket = uploadConfig.getBucketName();
        String prefix = uploadConfig.getStaticPrefix();
        String key = String.format(S3_STATIC_KEY_FORMAT, prefix, fileName);
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        File file = new File(fileTempPath);
        s3Client.uploadFile(bucket, key, file);
        String host = uploadConfig.getStaticHost();
        try {
            FileUtils.forceDelete(file);
        } catch (Exception e) {
            log.error("UploadS3上传完成，删除缓存文件失败", e);
        }
        return String.format(S3_STATIC_KEY_FORMAT, host, key);
    }

    /**
     * 上传文件到S3并保存到resourceFile表
     *
     * @param file 文件
     * @return com.wupol.myopia.business.management.domain.model.ResourceFile
     **/
    public ResourceFile uploadS3AndGetResourceFile(File file) throws UtilException {
        return uploadS3AndGetResourceFileAndDeleteTempFile(file, file.getName());
    }

    /**
     * 上传文件到S3并保存到resourceFile表
     *
     * @param fileTempPath 文件路径
     * @param fileName 文件名
     * @return com.wupol.myopia.business.management.domain.model.ResourceFile
     * @throws UtilException
     */
    public ResourceFile uploadS3AndGetResourceFile(String fileTempPath, String fileName) throws UtilException {
        return uploadS3AndGetResourceFileAndDeleteTempFile(new File(fileTempPath), fileName);
    }

    /**
     * 上传文件到S3并保存到resourceFile表，且删除原有缓存文件
     *
     * @param file 文件
     * @param fileName 文件名
     * @return com.wupol.myopia.business.management.domain.model.ResourceFile
     **/
    public ResourceFile uploadS3AndGetResourceFileAndDeleteTempFile(File file, String fileName) throws UtilException {
        String key = uploadFileToS3(file, fileName);
        ResourceFile resourceFile = new ResourceFile().setBucket(uploadConfig.getBucketName()).setS3Key(key).setFileName(fileName);
        resourceFileService.save(resourceFile);
        try {
            FileUtils.forceDelete(file);
        } catch (Exception e) {
            log.error("UploadS3上传完成，删除缓存文件失败", e);
        }
        return resourceFile;
    }

    /**
     * 上传文件到S3
     *
     * @param file 文件
     * @param fileName 文件名
     * @return java.lang.String
     * @throws UtilException
     */
    public String uploadFileToS3(File file, String fileName) throws UtilException {
        String bucket = uploadConfig.getBucketName();
        String prefix = uploadConfig.getPrefix();
        String key = String.format(S3_KEY_FORMAT, prefix, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_ONLY_DATE), UUID.randomUUID().toString(), fileName);
        s3Client.uploadFile(bucket, key, file);
        return key;
    }

    /**
     * 获取文件链接
     *
     * @param bucketName
     * @param s3Key
     * @return
     */
    public String getResourcePath(String bucketName, String s3Key) {
        Integer expiredHours= uploadConfig.getExpiredHours();
        return getResourcePathWithExpiredHours(bucketName, s3Key, expiredHours);
    }

    /**
     * 获取文件链接
     *
     * @param bucketName
     * @param s3Key
     * @return
     */
    public String getResourcePathWithExpiredHours(String bucketName, String s3Key, Integer expiredHours) {
        String key = String.format(FileCacheKey.FILE_URL, s3Key);
        Object fileUrl = redisUtil.get(key);
        if (Objects.nonNull(fileUrl)) {
            return fileUrl.toString();
        }
        Date expire = DateUtil.getRecentDate(expiredHours);
        try {
            String resourceS3Url = s3Client.getResourceS3Url(bucketName, s3Key, expire);
            redisUtil.set(key, resourceS3Url, expiredHours * 60l * 60);
            return resourceS3Url;
        } catch (UtilException e) {
            log.error(String.format("获取文件链接失败, bucket: %s, key: %s", bucketName, s3Key), e);
            return null;
        }
    }

    /**
     * 获取文件链接
     *
     * @param bucketName
     * @param s3Key
     * @return
     */
    public String getResourcePath(String bucketName, String s3Key, ResponseHeaderOverrides responseHeaderOverrides) {
        String key = String.format(FileCacheKey.FILE_URL, s3Key);
        Object fileUrl = redisUtil.get(key);
        if (Objects.nonNull(fileUrl)) {
            return fileUrl.toString();
        }
        Integer expiredHours= uploadConfig.getExpiredHours();
        Date expire = DateUtil.getRecentDate(expiredHours);
        try {
            String resourceS3Url = s3Client.getResourceS3Url(bucketName, s3Key, responseHeaderOverrides, expire);
            redisUtil.set(key, resourceS3Url, expiredHours * 60l * 60);
            return resourceS3Url;
        } catch (UtilException e) {
            log.error(String.format("获取文件链接失败, bucket: %s, key: %s", bucketName, key));
            return null;
        }
    }

    /**
     * 上传发布的PDF到s3
     * @param fileName 文件名
     * @param file PDF文件
     * @return s3 key
     * @throws UtilException
     */
    public String uploadPublishPDFFile(String fileName, File file) throws UtilException {
        String key = String.format(PDF_PUBLISH_KEY_FORMAT, DateFormatUtil.formatNow(DateFormatUtil.FORMAT_ONLY_DATE), fileName);
        String bucket = uploadConfig.getBucketName();
        s3Client.uploadFile(bucket, key, file);
        return key;
    }

    /**
     * 生成一个带PDF头的链接
     * @param fileName 文件名
     * @param file PDF文件
     * @return s3 key
     */
    public String getPdfUrl(String fileName, File file) throws UtilException {
        String key = uploadPublishPDFFile(fileName, file);
        ResponseHeaderOverrides responseHeaderOverrides = getPDFAttachmentHeader();
        return getResourcePath(uploadConfig.getBucketName(), key, responseHeaderOverrides);
    }

    /**
     * 生成一个带PDF附件头
     * @return
     */
    private ResponseHeaderOverrides getPDFAttachmentHeader() {
        ResponseHeaderOverrides responseHeaderOverrides = new ResponseHeaderOverrides();
        responseHeaderOverrides.setContentDisposition("attachment;");
        responseHeaderOverrides.setContentType("application/PDF");
        return responseHeaderOverrides;
    }

    /**
     * 上传文件到S3
     *
     * @param file 文件
     * @return 资源文件ID
     * @throws UtilException 异常
     */
    public Integer uploadFileToS3(File file) throws UtilException {
        ResourceFile resourceFile = uploadS3AndGetResourceFile(file);
        return resourceFile.getId();
    }
}
