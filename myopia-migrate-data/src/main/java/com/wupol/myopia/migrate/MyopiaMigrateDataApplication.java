package com.wupol.myopia.migrate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan({"com.wupol.myopia.migrate.**.domain.mapper", "com.wupol.myopia.business.core.**.domain.mapper"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wupol.myopia.business.**.client", "com.wupol.myopia.oauth.sdk.client"})
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia", "com.wupol.framework.api"})
public class MyopiaMigrateDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaMigrateDataApplication.class, args);
    }

}
