package com.wupol.myopia.business.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableAsync
@MapperScan("com.wupol.myopia.business.core.**.domain.mapper")
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = {"com.wupol.myopia.**.client"})
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia", "com.wupol.framework.api", "com.vistel.framework"})
public class MyopiaBusinessApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.setProperty("java.io.tmpdir","/tmp");
        SpringApplication.run(MyopiaBusinessApplication.class, args);
    }

}
