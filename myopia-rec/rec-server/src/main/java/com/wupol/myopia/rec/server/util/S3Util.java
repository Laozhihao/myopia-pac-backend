package com.wupol.myopia.rec.server.util;

import cn.hutool.core.date.DatePattern;
import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.rec.server.config.UploadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * S3工具类
 *
 * @author hang.yuan 2022/8/12 16:01
 */
@Slf4j
@Component
public class S3Util {
    @Autowired
    private S3Client s3Client;
    @Autowired
    private UploadConfig uploadConfig;

    /**
     * 文件路径key
     */
    private static final String S3_KEY_FORMAT = "%s/%s/%s/%s";
    /**
     * 文件访问地址
     */
    private static final String FILE_URL = "file:url:key_%s";

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
        String key = String.format(S3_KEY_FORMAT, prefix, DateUtil.format(new Date(), DatePattern.NORM_DATE_PATTERN), UUID.randomUUID().toString(), fileName);
        s3Client.uploadFile(bucket, key, file);
        return key;
    }

    /**
     * 获取文件链接
     *
     * @param s3Key
     * @return 文件链接
     */
    public String getResourcePathWithExpiredHours(String s3Key) {
        String key = String.format(FILE_URL, s3Key);
//        Object fileUrl = redisUtil.get(key);
//        if (Objects.nonNull(fileUrl)) {
//            return fileUrl.toString();
//        }
        Date expire = DateUtil.getRecentDate(uploadConfig.getExpiredHours());
        try {
            String resourceS3Url = s3Client.getResourceS3Url(uploadConfig.getBucketName(), s3Key, expire);
//            redisUtil.set(key, resourceS3Url, expiredHours * 60L * 60);
            return resourceS3Url;
        } catch (UtilException e) {
            log.error(String.format("获取文件链接失败, bucket: %s, key: %s", uploadConfig.getBucketName(), s3Key), e);
            return null;
        }
    }

}
