package com.wupol.myopia.oauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.TimeZone;

@MapperScan("com.wupol.myopia.oauth.**.domain.mapper")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia" })
public class MyopiaOauthApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(MyopiaOauthApplication.class, args);
    }

}
