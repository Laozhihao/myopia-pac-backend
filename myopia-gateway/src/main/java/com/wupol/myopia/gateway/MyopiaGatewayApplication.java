package com.wupol.myopia.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.wupol.myopia.gateway", "com.wupol.myopia.base.cache"})
public class MyopiaGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaGatewayApplication.class, args);
    }

}
