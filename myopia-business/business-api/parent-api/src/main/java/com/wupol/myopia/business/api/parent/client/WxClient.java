package com.wupol.myopia.business.api.parent.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author HaoHao
 * @Date 2021/2/26 16:17
 **/
@FeignClient(name = "wechat-service", url = "https://api.weixin.qq.com")
public interface WxClient {

    @GetMapping("/sns/oauth2/access_token")
    String getAccessToken(@RequestParam("appid") String appId, @RequestParam("secret") String secret, @RequestParam("code") String code, @RequestParam("grant_type") String grantType);

    @GetMapping("/sns/userinfo")
    String getUserInfo(@RequestParam("access_token") String accessToken, @RequestParam("openid") String openId, @RequestParam("lang") String lang);

}
