package com.wupol.myopia.base.minio;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio客户端配置类
 *
 * @Author lzh
 * @Date 2023/5/4
 **/
@Configuration
@EnableConfigurationProperties(MinioConfig.class)
public class MinioClientConfig {

    /**
     * 注入minio客户端
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient(MinioConfig minioConfig) {
        return MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccesskey(), minioConfig.getSecretKey())
                .build();
    }
}
