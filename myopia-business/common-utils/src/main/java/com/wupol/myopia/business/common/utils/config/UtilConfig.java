package com.wupol.myopia.business.common.utils.config;


import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.exception.UtilException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author SheldonX
 * @date 2018/8/24
 */
@Configuration
public class UtilConfig {

    @Bean
    public S3Client getS3Client() throws UtilException {
        return S3Client.getInstance();
    }
}
