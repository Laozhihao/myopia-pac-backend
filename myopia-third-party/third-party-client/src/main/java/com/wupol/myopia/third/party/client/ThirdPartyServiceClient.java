package com.wupol.myopia.third.party.client;

import com.wupol.myopia.base.config.feign.BusinessServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 *
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@FeignClient(name = "myopia-third-party", decode404 = true, fallbackFactory = ThirdPartyServiceFallbackFactory.class, configuration = BusinessServiceFeignConfig.class)
public interface ThirdPartyServiceClient {

}
