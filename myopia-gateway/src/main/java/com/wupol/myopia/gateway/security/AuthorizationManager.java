package com.wupol.myopia.gateway.security;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
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
        // 认证通过且权限匹配的用户，才可访问当前路径
        JWSObject jwsObject;
        try {
            jwsObject = JWSObject.parse(token.replace(AuthConstants.JWT_TOKEN_PREFIX, Strings.EMPTY));
        } catch (ParseException e) {
            return Mono.just(new AuthorizationDecision(false));
        }
        String payload = jwsObject.getPayload().toString();
        JSONObject payloadJson = JSONUtil.parseObj(payload);
        JSONObject userInfoJson = payloadJson.getJSONObject(AuthConstants.JWT_USER_INFO_KEY);
        List<Object> permissions = redisUtil.lGetAll(String.format(RedisConstant.USER_PERMISSION_KEY, Integer.parseInt(userInfoJson.getStr("id"))));
        if (CollectionUtils.isEmpty(permissions)) {
            return Mono.just(new AuthorizationDecision(false));
        }
        List<String> permissionList = JSONUtil.toBean(JSONUtil.toJsonStr(permissions.get(0)), new TypeReference<List<String>>() {}, false);
        boolean isAuthenticated = permissionList.stream().anyMatch(x -> pathMatcher.match(x, String.format(AuthConstants.REQUEST_PATH_WITH_METHOD, method.toLowerCase(), path)));
        return  Mono.just(new AuthorizationDecision(isAuthenticated));
        /*return mono
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(x -> pathMatcher.match(x, String.format(AuthConstants.REQUEST_PATH_WITH_METHOD, method.toLowerCase(), path)))
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));*/
    }
}
