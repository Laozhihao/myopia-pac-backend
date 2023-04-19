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
    String getWxAccessToken(@RequestParam("appid") String appId, @RequestParam("secret") String secret, @RequestParam("code") String code, @RequestParam("grant_type") String grantType);

    @GetMapping("/sns/userinfo")
    String getUserInfo(@RequestParam("access_token") String accessToken, @RequestParam("openid") String openId, @RequestParam("lang") String lang);


    /**
     * 获取AccessToken
     *
     * @param grantType 获取access_token填写client_credential
     * @param appid     第三方用户唯一凭证
     * @param secret    第三方用户唯一凭证密钥，即appsecret
     *                  <p>接口更多说明<a href="https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html#value">微信文档</a></p>
     * @return String
     */
    @GetMapping("/cgi-bin/token")
    String getWxAccessToken(@RequestParam("grant_type") String grantType, @RequestParam("appid") String appid, @RequestParam("secret") String secret);


    /**
     * 获取JsapiTicket
     *
     * @param accessToken accessToken
     * @param type        填写 jsapi
     * @return JsapiTicket
     */
    @GetMapping("/cgi-bin/ticket/getticket")
    String getJsapiTicket(@RequestParam("access_token") String accessToken, @RequestParam("type") String type);

}
