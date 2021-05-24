package com.wupol.myopia.business.common.utils.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 上传配置
 * @author Alix
 * @Date 2021/02/04
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {
    private String savePath;
    private String suffixs;
    private String bucketName;
    private String prefix;
    private String staticPrefix;
    private String staticHost;
    private Integer expiredHours;
}
