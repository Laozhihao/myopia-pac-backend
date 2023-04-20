package com.wupol.myopia.third.party;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@MapperScan("com.wupol.myopia.third.party.**.domain.mapper")
@EnableDiscoveryClient
@EnableScheduling
@SpringBootApplication
public class MyopiaThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaThirdPartyApplication.class, args);
    }

}
