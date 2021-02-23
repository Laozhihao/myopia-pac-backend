package com.wupol.myopia.gateway.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.gateway.security.AuthorizationManager;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@AllArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    private AuthorizationManager authorizationManager;
    private WhiteListConfig whiteListConfig;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
        http.authorizeExchange().pathMatchers(ArrayUtil.toArray(whiteListConfig.getUrls(), String.class)).permitAll()
                .anyExchange().access(authorizationManager).and().exceptionHandling()
                // 处理未授权
                .accessDeniedHandler(accessDeniedHandler())
                // 处理未认证
                .authenticationEntryPoint(authenticationEntryPoint())
                .and().csrf().disable();

        return http.build();
    }

    /**
     * 未授权
     *
     * @return
     */
    @Bean
    ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap(response -> {
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                response.getHeaders().set("Access-Control-Allow-Origin", "*");
                response.getHeaders().set("Cache-Control", "no-cache");
                String body = JSONUtil.toJsonStr(ApiResult.failure(ResultCode.USER_ACCESS_UNAUTHORIZED.getCode(), ResultCode.USER_ACCESS_UNAUTHORIZED.getMessage()));
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(Charset.forName("UTF-8")));
                return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
            });

            return mono;
        };
    }

    /**
     * token无效或者已过期自定义响应
     */
    @Bean
    ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, e) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap(response -> {
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                response.getHeaders().set("Access-Control-Allow-Origin", "*");
                response.getHeaders().set("Cache-Control", "no-cache");
                String body = JSONUtil.toJsonStr(ApiResult.failure(ResultCode.TOKEN_INVALID_OR_EXPIRED.getCode(),
                        ResultCode.TOKEN_INVALID_OR_EXPIRED.getMessage()));
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(Charset.forName("UTF-8")));
                return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
            });
            return mono;
        };
    }

    /**
     *  ServerHttpSecurity没有将jwt中authorities的负载部分当做Authentication需要把jwt的Claim中的authorities加入
     *  方案：重新定义权限管理器，默认转换器JwtGrantedAuthoritiesConverter
     */
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(Strings.EMPTY);
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AuthConstants.AUTHORITY_CLAIM_NAME);
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
