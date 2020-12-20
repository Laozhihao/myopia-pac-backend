package com.wupol.myopia.business.management.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @Author HaoHao
 * @Date 2020/12/14
 **/
@Log4j2
@Component
public class OauthServiceFallback implements OauthServiceClient {
    @Override
    public ApiResult login(String data) {
        log.info("【business】远程调用服务异常 - 降级返回");
        return ApiResult.failure("远程调用服务异常 - 降级返回");
    }
}
