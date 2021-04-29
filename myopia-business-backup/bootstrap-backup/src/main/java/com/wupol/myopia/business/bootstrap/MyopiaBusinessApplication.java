package com.wupol.myopia.business.bootstrap;

import org.springframework.boot.SpringApplication;

/*@EnableAsync
@MapperScan("com.wupol.myopia.business.**.domain.mapper")
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.wupol.myopia.business.**.client")
@SpringBootApplication(scanBasePackages = { "com.wupol.myopia", "com.wupol.framework.api" })*/
public class MyopiaBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyopiaBusinessApplication.class, args);
    }

}
