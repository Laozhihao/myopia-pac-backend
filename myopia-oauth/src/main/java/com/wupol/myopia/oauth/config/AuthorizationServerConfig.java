package com.wupol.myopia.oauth.config;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.oauth.domain.model.SecurityUserDetails;
import com.wupol.myopia.oauth.filter.ClientCredentialsAccessTokenEndpointFilter;
import com.wupol.myopia.oauth.service.JdbcClientDetailsServiceImpl;
import com.wupol.myopia.oauth.service.UserDetailsServiceImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权服务配置
 *
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private DataSource dataSource;
    @Resource
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * 配置从数据库获取客户端详情
     */
    @Override
    @SneakyThrows
    public void configure(ClientDetailsServiceConfigurer clients) {
        clients.withClientDetails(jdbcClientDetailsService());
    }

    /**
     * 配置获取客户端详情执行类
     */
    @Bean
    public JdbcClientDetailsServiceImpl jdbcClientDetailsService() {
        JdbcClientDetailsServiceImpl jdbcClientDetailsService = new JdbcClientDetailsServiceImpl(dataSource);
        jdbcClientDetailsService.setFindClientDetailsSql(AuthConstants.FIND_CLIENT_DETAILS_SQL);
        jdbcClientDetailsService.setSelectClientDetailsSql(AuthConstants.SELECT_CLIENT_DETAILS_SQL);
        return jdbcClientDetailsService;
    }

    /**
     * 配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> tokenEnhancers = new ArrayList<>();
        tokenEnhancers.add(tokenEnhancer());
        tokenEnhancers.add(jwtAccessTokenConverter());
        tokenEnhancerChain.setTokenEnhancers(tokenEnhancers);

        endpoints.authenticationManager(authenticationManager)
                .accessTokenConverter(jwtAccessTokenConverter()).tokenEnhancer(tokenEnhancerChain)
                .userDetailsService(userDetailsService)
                // 替换默认获取token的入口
                .pathMapping("/oauth/token", "/login")
                // refresh token有两种使用方式：重复使用(true)、非重复使用(false)，默认为true
                // 1 重复使用：access token过期刷新时， refresh token过期时间未改变，仍以初次生成的时间为准
                // 2 非重复使用：access token过期刷新时， refresh token过期时间延续，在refresh token有效期内刷新便永不失效达到无需再次登录的目的
                .reuseRefreshTokens(true);
    }


    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        ClientCredentialsAccessTokenEndpointFilter endpointFilter = new ClientCredentialsAccessTokenEndpointFilter(security);
        endpointFilter.afterPropertiesSet();
        endpointFilter.setAuthenticationEntryPoint(authenticationEntryPoint());
        security.addTokenEndpointAuthenticationFilter(endpointFilter);

        security.authenticationEntryPoint(authenticationEntryPoint())
                // 开启/oauth/token_key端口认证权限访问
                .tokenKeyAccess("isAuthenticated()")
                // 开启/oauth/check_token端口无权限访问
                .checkTokenAccess("permitAll()");
    }


    /**
     * 自定义认证异常响应数据
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, e) -> {
            response.setStatus(HttpStatus.HTTP_OK);
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Cache-Control", "no-cache");
            ApiResult result = ApiResult.failure(ResultCode.CLIENT_AUTHENTICATION_FAILED.getMessage());
            response.getWriter().print(JSONUtil.toJsonStr(result));
            response.getWriter().flush();
        };
    }

    /**
     * 使用非对称SHA256withRSA(简称RS256)加密算法对token签名，默认为HMACSHA256(简称HS256)
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair());
        return converter;
    }

    /**
     * 从classpath下的密钥库中获取密钥对(公钥+私钥) TODO：密码从配置文件读、秘钥库从外部读取
     *
     *   1. 使用 keytool 生成 RSA 证书 myopia.jks
     *   2. 生成命令：keytool -genkey -alias myopia -keyalg RSA -keypass 123456 -keystore myopia.jks -storepass 123456
     *
     *      -genkey 生成密钥
     *      -alias 别名
     *      -keyalg 密钥算法
     *      -keypass 密钥口令
     *      -keystore 生成密钥库的存储路径和名称
     *      -storepass 密钥库口令
     */
    @Bean
    public KeyPair keyPair() {
        // 密钥库的存储路径和名称、密钥库口令
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("myopia.jks"), "123456".toCharArray());
        // 别名、密钥口令
        return factory.getKeyPair("myopia", "123456".toCharArray());
    }

    /**
     * JWT内容增强
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return (accessToken, authentication) -> {
            Map<String, Object> map = new HashMap<>(2);
            SecurityUserDetails user = (SecurityUserDetails) authentication.getUserAuthentication().getPrincipal();
            map.put(AuthConstants.JWT_USER_INFO_KEY, user.getUserInfo());
//            map.put(AuthConstants.JWT_CLIENT_ID_KEY, user.getClientId());
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(map);
            return accessToken;
        };
    }
}