package com.wupol.myopia.rec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan({"com.wupol.myopia.rec.**.domain.mapper", "com.wupol.myopia.business.core.**.domain.mapper"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wupol.myopia.business.**.client", "com.wupol.myopia.oauth.sdk.client"})
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia", "com.wupol.framework.api"})
public class MyopiaRecApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaRecApplication.class, args);
    }

}
