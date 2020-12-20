package com.wupol.myopia.business.management.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author HaoHao
 * @Date 2020/12/11
 **/
@FeignClient(name ="myopia-oauth", fallback = OauthServiceFallback.class)
//@FeignClient(name ="myopia-oauth")
public interface OauthServiceClient {

    @GetMapping("login")
    ApiResult login(@RequestParam("data") String data);
}
