package com.wupol.myopia.oauth.config;

import com.wupol.myopia.oauth.filter.ClientCredentialsRefreshTokenEndpointFilter;
import com.wupol.myopia.oauth.service.JdbcClientDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.Collections;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JdbcClientDetailsServiceImpl jdbcClientDetailsService;

    /**
     * 配置请求访问权限
     **/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll().and()
                // 配置不需要拦截的请求
                .authorizeRequests().antMatchers("/rsa/publicKey", "/rsa/key", "/oauth/**", "/refresh/token").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterAfter(refreshTokenEndpointFilter(), LogoutFilter.class)
                .csrf().disable();
    }

    /**
     * 定义认证管理器，如果不配置，SpringBoot会自动配置一个AuthenticationManager，覆盖掉内存中的用户
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 密码加密配置
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 自定义刷新token时的client校验过滤器
     */
    @Bean
    public ClientCredentialsRefreshTokenEndpointFilter refreshTokenEndpointFilter() {
        ClientCredentialsRefreshTokenEndpointFilter refreshTokenEndpointFilter = new ClientCredentialsRefreshTokenEndpointFilter();
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(new ClientDetailsUserDetailsService(jdbcClientDetailsService));
        refreshTokenEndpointFilter.setAuthenticationManager(new ProviderManager(Collections.singletonList(daoAuthenticationProvider)));
        return refreshTokenEndpointFilter;
    }
}
