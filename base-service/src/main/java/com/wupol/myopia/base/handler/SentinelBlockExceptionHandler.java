package com.wupol.myopia.base.handler;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.fastinfoset.Encoder;
import com.wupol.myopia.base.domain.ApiResult;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义sentinel异常返回信息
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
@Component
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        ApiResult apiResult;
        if (e instanceof FlowException) {
            apiResult = ApiResult.failure(101, "接口限流了");
        } else if (e instanceof DegradeException) {
            apiResult = ApiResult.failure(102, "服务降级了");
        } else if (e instanceof ParamFlowException) {
            apiResult = ApiResult.failure(103, "热点参数限流了");
        } else if (e instanceof SystemBlockException) {
            apiResult = ApiResult.failure(104, "系统规则（负载/...不满足要求）");

        } else if (e instanceof AuthorityException) {
            apiResult = ApiResult.failure(105, "授权规则不通过...");
        } else {
            apiResult = ApiResult.failure("未知流控规则异常");
        }
        // http状态码
        httpServletResponse.setStatus(500);
        httpServletResponse.setCharacterEncoding(Encoder.UTF_8);
        httpServletResponse.setHeader("Content-Type", "application/json;charset=utf-8");
        httpServletResponse.setContentType("application/json;charset=utf-8");
        // spring mvc自带的json操作工具，叫jackson
        new ObjectMapper().writeValue(httpServletResponse.getWriter(), apiResult);
    }

    @PostConstruct
    public void init() {
        new SentinelBlockExceptionHandler();
    }
}
