package com.wupol.myopia.business.common.utils.config;


import com.vistel.Interface.aws.S3Client;
import com.vistel.Interface.config.AWSConfig;
import com.vistel.Interface.exception.UtilException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return S3Client.getInstance(new AWSConfig(uploadConfig.getEndpoint(), uploadConfig.getAccesskey(), uploadConfig.getSecretKey(), uploadConfig.getRegion()));
    }

    public static void main(String[] args) {
        String javaHome = System.getenv("AWS_ACCESS_KEY_ID_1");
        String javaHome2 = System.getenv("AWS_ACCESS_KEY_ID");
        System.out.println("javaHome的值:" + javaHome);
        System.out.println("javaHome的值:" + javaHome2);
    }
}
