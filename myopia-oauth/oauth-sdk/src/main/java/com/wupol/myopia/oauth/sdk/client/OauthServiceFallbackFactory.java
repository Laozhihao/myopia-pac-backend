package com.wupol.myopia.oauth.sdk.client;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BusinessUtil;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 容错异常获取与降级处理
 *
 * @Author HaoHao
 * @Date 2020/12/30
 **/
@Component
public class OauthServiceFallbackFactory implements FallbackFactory<OauthServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(OauthServiceFallbackFactory.class);

    @Override
    public OauthServiceClient create(Throwable throwable) {
        logger.error("【调用Oauth服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = BusinessUtil.getMsgFromBodyWithDefault(feignException.getMessage(), feignException.contentUTF8());
        throw new BusinessException(message);
    }

}
