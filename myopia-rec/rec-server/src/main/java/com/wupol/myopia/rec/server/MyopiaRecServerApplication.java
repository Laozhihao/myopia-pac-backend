package com.wupol.myopia.rec.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 * @author hang.yuan
 * @date 2022/8/12
 */
//@EnableDiscoveryClient
//@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.wupol.myopia"})
public class MyopiaRecServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaRecServerApplication.class, args);
    }

}
