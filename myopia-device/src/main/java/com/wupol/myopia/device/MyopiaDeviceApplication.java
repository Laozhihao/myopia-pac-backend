package com.wupol.myopia.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MyopiaDeviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaDeviceApplication.class, args);
    }

}
