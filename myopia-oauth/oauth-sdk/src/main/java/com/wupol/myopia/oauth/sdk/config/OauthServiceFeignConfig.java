package com.wupol.myopia.oauth.sdk.config;

import org.springframework.context.annotation.Bean;

/**
 * @author jacob
 * @date 2020/9/2 17:27
 * @version 1.0.0
 * Detail: Feign配置
 */
public class OauthServiceFeignConfig {

    /**
     * open Feign 有默认实现,也是有条件的加载,注意加载顺序,如引用Ai gateway包后,需要自定义,请自行引入
     * <p>
     * 自定义解码器
     * </p>
     *
     * @return
     */
    @Bean
    public OauthServiceCustomDecoder customDecoder() {
        return new OauthServiceCustomDecoder();
    }

}
