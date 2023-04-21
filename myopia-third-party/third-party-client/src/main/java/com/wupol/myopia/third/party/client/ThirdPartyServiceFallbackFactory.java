package com.wupol.myopia.third.party.client;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BusinessUtil;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 调用third-party服务时容错异常获取与降级处理
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@Slf4j
@Component
public class ThirdPartyServiceFallbackFactory implements FallbackFactory<ThirdPartyServiceClient> {

    @Override
    public ThirdPartyServiceClient create(Throwable throwable) {
        log.error("【调用third-party服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = BusinessUtil.getMsgFromBodyWithDefault(feignException.getMessage(), feignException.contentUTF8());
        throw new BusinessException(message);
    }
}
