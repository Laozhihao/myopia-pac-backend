package com.wupol.myopia.oauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.wupol.myopia.oauth.**.domain.mapper")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia" })
public class MyopiaOauthBackupApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaOauthBackupApplication.class, args);
    }

}
