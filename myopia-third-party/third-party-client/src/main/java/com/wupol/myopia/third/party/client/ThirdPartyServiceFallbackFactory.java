package com.wupol.myopia.third.party.client;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.domain.ApiResult;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 调用third-party服务时容错异常获取与降级处理
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@Slf4j
@Component
public class ThirdPartyServiceFallbackFactory implements FallbackFactory<ThirdPartyServiceClient> {

    private static final String SYSTEM_ERROR_MESSAGE = "系统异常，请联系管理员";

    @Override
    public ThirdPartyServiceClient create(Throwable throwable) {
        log.error("【调用Rec服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = getMsgFromBodyWithDefault(feignException.getMessage(), feignException.contentUTF8());
        throw new RuntimeException(message);
    }

    public static String getMsgFromBodyWithDefault(String message, String body) {
        try {
            ApiResult result = JSON.parseObject(body, ApiResult.class);
            if (Objects.nonNull(result) && StringUtils.hasText(result.getMessage())) {
                return result.getMessage();
            }
            return StringUtils.isEmpty(message) ? SYSTEM_ERROR_MESSAGE : message;
        } catch (Exception e) {
            return SYSTEM_ERROR_MESSAGE;
        }
    }
}
