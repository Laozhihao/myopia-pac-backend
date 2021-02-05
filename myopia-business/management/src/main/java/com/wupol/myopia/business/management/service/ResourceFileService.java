package com.wupol.myopia.business.management.service;

import com.wupol.framework.core.util.DateUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.domain.mapper.ResourceFileMapper;
import com.wupol.myopia.business.management.domain.model.ResourceFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author Alix
 * @Date 2021-02-04
 */
@Service
@Slf4j
public class ResourceFileService extends BaseService<ResourceFileMapper, ResourceFile> {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UploadConfig uploadConfig;
//    @Autowired
//    private S3Client s3Client;
    /**
     * 获取影像链接
     *
     * @param bucketName
     * @param s3Key
     * @return
     */
    public String getResourcePath(String bucketName, String s3Key) {
        String key = String.format(CacheKey.FILE_URL, s3Key);
        Object fileUrl = redisUtil.get(key);
        if (Objects.nonNull(fileUrl)) {
            return fileUrl.toString();
        }
        Integer expiredHours= uploadConfig.getExpiredHours();
        Date expire = DateUtil.getRecentDate(expiredHours);
//        try {
//            String resourceS3Url = s3Client.getResourceS3Url(bucketName, key, expire);
//            redisUtil.set(key, resourceS3Url, expiredHours * 60 * 60);
            return "";
//        } catch (UtilException e) {
//            log.error(String.format("获取影像链接失败, bucket: %s, key: %s", bucketName, key));
//            return null;
//        }
    }

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
        return getResourcePath(file.getBucket(), file.getS3Key());
    }
}
