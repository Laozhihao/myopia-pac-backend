package com.wupol.myopia.gateway.security;

import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 问卷系统请求映射正则表达式
     */
    public static final String QUESTIONNAIRE_REQUEST_MAPPING_REGEX = "/questionnaire/**";

    /**
     * 退出登陆路径
     */
    public static final String AUTH_EXIT_PATH = "/auth/exit";

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
        if (StringUtils.isBlank(token)) {
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
        Object accessToken = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getSystemCode(), currentUser.getId()));
        Object oldAccessToken = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_OLD_KEY, currentUser.getSystemCode(), currentUser.getId()));
        // 判断是否已经退出登录
        if (!tokenWithoutPrefix.equals(accessToken) && !tokenWithoutPrefix.equals(oldAccessToken)) {
            return Mono.just(new AuthorizationDecision(false));
        }
        // 家长端、筛查端、学校端的用户，不需要校验接口访问权限。TODO：等后面系统迭代中，有了维护各个端的接口资源权限地方，再打开
        Integer systemCode = currentUser.getSystemCode();
        if (SystemCode.PARENT_CLIENT.getCode().equals(systemCode) || SystemCode.SCREENING_CLIENT.getCode().equals(systemCode) || SystemCode.SCHOOL_CLIENT.getCode().equals(systemCode)) {
            return Mono.just(new AuthorizationDecision(true));
        }
        // 问卷系统 若访问路径不是问卷系统的，不给访问权限
        if (SystemCode.QUESTIONNAIRE.getCode().equals(systemCode)) {
            if (pathMatcher.match(QUESTIONNAIRE_REQUEST_MAPPING_REGEX, path) || path.contains(AUTH_EXIT_PATH)) {
                return Mono.just(new AuthorizationDecision(true));
            } else {
                return Mono.just(new AuthorizationDecision(false));
            }
        }
        // 判断接口访问权限
        List<Object> permissions = redisUtil.lGetAll(String.format(RedisConstant.USER_PERMISSION_KEY, currentUser.getSystemCode(), currentUser.getId()));
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
