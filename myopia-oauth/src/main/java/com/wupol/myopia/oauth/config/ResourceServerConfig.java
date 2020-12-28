package com.wupol.myopia.oauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 资源服务配置
 *   1.网关是微服务资源访问的统一入口，所以在网关做资源访问的统一鉴权是再合适不过，授权中心不再鉴权只负责颁发token
 *   2.如果有需要鉴权，打开注解即可
 * @Author HaoHao
 * @Date 2020/12/25
 **/
//@Configuration
//@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .requestMatchers()
                // 配置需要保护的资源路径，**没有配置的资源路径无法访问**
                .antMatchers("/oauth/**");
    }

}
