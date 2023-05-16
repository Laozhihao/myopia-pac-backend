package com.wupol.myopia.business.common.utils.config;


import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.config.AWSConfig;
import com.vistel.Interface.exception.UtilException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 初始化 S3Client
 *
 * @author SheldonX
 * @date 2018/8/24
 */
@Configuration
public class S3ClientConfig {

    @Bean
    public S3Client getS3Client(UploadConfig uploadConfig) throws UtilException {
        if (StringUtils.isEmpty(uploadConfig.getEndpoint())) {
            return S3Client.getInstance();
        }
        return S3Client.getInstance(new AWSConfig(uploadConfig.getEndpoint(), null, null, uploadConfig.getRegion(), false));
    }
}
