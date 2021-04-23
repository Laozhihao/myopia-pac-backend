package com.wupol.myopia.gateway.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

/**
 * 鉴权管理器
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Component
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    private static Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        ServerHttpRequest request = authorizationContext.getExchange().getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethodValue();
        PathMatcher pathMatcher = new AntPathMatcher();
        // 对应跨域的预检请求直接放行
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return Mono.just(new AuthorizationDecision(true));
        }
        // token为空拒绝访问
        String token = request.getHeaders().getFirst(AuthConstants.JWT_TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            return Mono.just(new AuthorizationDecision(false));
        }
        // 解析token
        String tokenWithoutPrefix = token.replace(AuthConstants.JWT_TOKEN_PREFIX, Strings.EMPTY);
        JWSObject jwsObject;
        try {
            jwsObject = JWSObject.parse(tokenWithoutPrefix);
        } catch (ParseException e) {
            return Mono.just(new AuthorizationDecision(false));
        }
        CurrentUser currentUser = JSONUtil.parseObj(jwsObject.getPayload().toString()).get(AuthConstants.JWT_USER_INFO_KEY, CurrentUser.class);
        Object accessTokenCache = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getId()));
        // 判断是否已经退出登录
        if (Objects.isNull(accessTokenCache) || !accessTokenCache.equals(tokenWithoutPrefix)) {
            return Mono.just(new AuthorizationDecision(false));
        }
        // 家长端、筛查端、医院端的用户，不需要校验接口访问权限。TODO：等后面系统迭代中，有了维护各个端的接口资源权限地方，再打开
        Integer systemCode = currentUser.getSystemCode();
        if (SystemCode.PATENT_CLIENT.getCode().equals(systemCode) || SystemCode.SCREENING_CLIENT.getCode().equals(systemCode) || SystemCode.HOSPITAL_CLIENT.getCode().equals(systemCode)) {
            return Mono.just(new AuthorizationDecision(true));
        }
        // 判断接口访问权限
        List<Object> permissions = redisUtil.lGetAll(String.format(RedisConstant.USER_PERMISSION_KEY, currentUser.getId()));
        if (CollectionUtils.isEmpty(permissions)) {
            return Mono.just(new AuthorizationDecision(false));
        }
        // 认证通过且权限匹配的用户，才可访问当前路径
        boolean isAuthenticated = permissions.stream().anyMatch(x -> pathMatcher.match(x.toString(), String.format(AuthConstants.REQUEST_PATH_WITH_METHOD, method.toLowerCase(), path)));
        return mono
                .filter(Authentication::isAuthenticated)
                .map(x -> new AuthorizationDecision(isAuthenticated))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
