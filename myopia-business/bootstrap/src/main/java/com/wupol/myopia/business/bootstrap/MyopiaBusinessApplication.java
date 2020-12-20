package com.wupol.myopia.business.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.wupol.myopia.business.**.domain.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wupol.myopia.business.**.client")
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia" })
public class MyopiaBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaBusinessApplication.class, args);
    }

}
