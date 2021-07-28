package com.wupol.myopia.oauth.sdk.client;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 容错异常获取与降级处理
 *
 * @Author HaoHao
 * @Date 2020/12/30
 **/
@Component
public class OauthServiceFallbackFactory implements FallbackFactory<OauthServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(OauthServiceFallbackFactory.class);
    private static final String SYSTEM_ERROR_MESSAGE = "系统异常，请联系管理员";


    @Override
    public OauthServiceClient create(Throwable throwable) {
        logger.error("【调用Oauth服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = getMsgFromBodyWidthDefault(feignException.getMessage(), feignException.contentUTF8());
        throw new BusinessException(message);
    }

    private static String getMsgFromBodyWidthDefault(String message, String body) {
        try {
            ApiResult result = JSONObject.parseObject(body, ApiResult.class);
            if (Objects.nonNull(result) && !StringUtils.isEmpty(result.getMessage())) {
                return result.getMessage();
            }
            return StringUtils.isEmpty(message) ? SYSTEM_ERROR_MESSAGE : message;
        } catch (Exception e) {
            return SYSTEM_ERROR_MESSAGE;
        }
    }
}
