package com.wupol.myopia.business.sdk.client;

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
 * @Author wulizhou
 * @Date 2022/6/29 17:01
 */
@Component
public class BusinessServiceFallbackFactory implements FallbackFactory<BusinessServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceFallbackFactory.class);

    @Override
    public BusinessServiceClient create(Throwable throwable) {
        logger.error("【调用Business服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = BusinessUtil.getMsgFromBodyWithDefault(feignException.getMessage(), feignException.contentUTF8());
        throw new BusinessException(message);
    }

}
