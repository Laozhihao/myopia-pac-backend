package com.wupol.myopia.rec.server.config;


import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.config.AWSConfig;
import com.vistel.Interface.exception.UtilException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 *
 * @author SheldonX
 * @date 2018/8/24
 */
@Configuration
public class UtilConfig {

    private static final String S3_TYPE = "minio";

    @Bean
    public S3Client getS3Client(UploadConfig uploadConfig) throws UtilException {
        if (S3_TYPE.equals(uploadConfig.getS3Type()) && !StringUtils.isEmpty(uploadConfig.getEndpoint())) {
            return S3Client.getInstance(new AWSConfig(uploadConfig.getEndpoint(), null, null, uploadConfig.getRegion(), false));

        }
        return S3Client.getInstance();
    }
}
