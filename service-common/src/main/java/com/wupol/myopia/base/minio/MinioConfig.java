package com.wupol.myopia.base.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio参数配置类
 *
 * @Author lzh
 * @Date 2023/5/4
 **/
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    /**
     * 端点
     */
    private String endpoint;

    /**
     * 访问key
     */
    private String accesskey;

    /**
     * 密码key
     */
    private String secretKey;

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 前缀
     */
    private String prefix;
}
