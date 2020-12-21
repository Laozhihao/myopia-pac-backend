package com.wupol.myopia.business.management.client;

import com.wupol.myopia.base.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author HaoHao
 * @Date 2020/12/11
 **/
@FeignClient(name ="myopia-oauth", fallback = OauthServiceFallback.class)
public interface OauthServiceClient {

    @GetMapping("login")
    ApiResult login(@RequestParam("data") String data);
}
