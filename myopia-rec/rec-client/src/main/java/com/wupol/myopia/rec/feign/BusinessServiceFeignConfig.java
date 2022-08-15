package com.wupol.myopia.rec.feign;

import org.springframework.context.annotation.Bean;

/**
 * @author jacob
 * @date 2020/9/2 17:27
 * @version 1.0.0
 * Detail: Feign配置
 */
public class BusinessServiceFeignConfig {

    /**
     * 统一返回自定义解码器
     * @return
     */
    @Bean
    public BusinessServiceCustomDecoder customDecoder() {
        return new BusinessServiceCustomDecoder();
    }

}
