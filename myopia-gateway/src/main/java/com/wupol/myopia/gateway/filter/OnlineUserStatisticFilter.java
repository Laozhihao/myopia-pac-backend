package com.wupol.myopia.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 在线用户统计过滤器
 *
 * @author hang.yuan 2022/5/9 15:14
 */
@Component
public class OnlineUserStatisticFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisUtil redisUtil;

    /** 有效时间：五分钟 5*60 **/
    private static final long ONLINE_USERS_EXPIRED = 300L;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String onlineUsersNum = "online:%s:%s";
        ServerHttpRequest request = exchange.getRequest();
        String payload = request.getHeaders().getFirst(AuthConstants.JWT_PAYLOAD_KEY);
        if (StringUtils.isNotBlank(payload)) {
            String user = JSON.parseObject(payload).getString(AuthConstants.JWT_USER_INFO_KEY);
            CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
            if (Objects.nonNull(currentUser)) {
                String format = String.format(onlineUsersNum, currentUser.getClientId(), currentUser.getId());
                redisUtil.set(format,currentUser.getRealName(),ONLINE_USERS_EXPIRED);
            }
        } else {
            // 无token的访问，例如白名单中的接口（家长端入口、报告端均有），统一归为同一个端
            String ip = RequestUtil.getIP(request);
            String format = String.format(onlineUsersNum, -1, ip);
            redisUtil.set(format, ip + ", " + request.getURI().getPath(), ONLINE_USERS_EXPIRED);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
