package com.wupol.myopia.business.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@MapperScan("com.wupol.myopia.business.core.**.domain.mapper")
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = {"com.wupol.myopia.business.**.client", "com.wupol.myopia.oauth.sdk.client"})
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia", "com.wupol.framework.api"})
public class MyopiaBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaBusinessApplication.class, args);
    }

}
