package com.wupol.myopia.oauth.filter;

import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 自定义客户端刷新token时权限校验过滤器
 *
 * @Author HaoHao
 * @Date 2020/12/24
 **/
public class ClientCredentialsRefreshTokenEndpointFilter extends ClientCredentialsTokenEndpointFilter {

    private AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 指定拦截的接口
     **/
    public ClientCredentialsRefreshTokenEndpointFilter() {
        super("/refresh/token");
    }

    @Override
    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        super.setAuthenticationEntryPoint(null);
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * 失败时返回自定义格式异常
     **/
    @Override
    public void afterPropertiesSet() {
        setAuthenticationFailureHandler((request, response, e) -> authenticationEntryPoint.commence(request, response, e));
        setAuthenticationSuccessHandler((request, response, authentication) -> {
        });
    }

}
