package com.wupol.myopia.gateway.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author HaoHao
 * @Date 2020/12/16
 **/
@Log4j2
@Component
public class SentinelStatusFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        // TODO Auto-generated method stub
        // 小于 -1
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // get match route id
                // 获取此次请求命中的路由，以便记录异常到具体资源
                Route route = (Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                String id = route.getId();
                // 返回500错误则记录异常，判断条件可自定义
                if (originalResponse.getRawStatusCode() == 500) {
                    Entry entry = null;
                    try {
                        // 第三个参数为0，这样返回的时候不会再次累加QPS数（进来的时候已经统计过一次）
                        // 分别对routeId和path登记统计
                        entry = SphU.entry(id, EntryType.OUT, 0);
                        Tracer.trace(new Exception("error"));
                        entry = SphU.entry(path, EntryType.OUT, 0);
                        Tracer.trace(new Exception("error"));
                    } catch (BlockException e) {
                        // TODO Auto-generated catch block
                        log.error("记录异常数失败", e);
                    } finally {
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                }
                return super.writeWith(body);
            }
        };
        // replace response with decorator
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

}