package com.wupol.myopia.business.sdk.client;

import com.wupol.myopia.base.config.feign.BusinessServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author wulizhou
 * @Date 2022/6/29 17:01
 */
@FeignClient(name = "myopia-business", decode404 = true, fallbackFactory = BusinessServiceFallbackFactory.class, configuration = BusinessServiceFeignConfig.class)
public interface BusinessServiceClient {



}
