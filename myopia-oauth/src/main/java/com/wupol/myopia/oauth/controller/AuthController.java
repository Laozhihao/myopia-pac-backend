package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.oauth.constant.SystemCode;
import com.wupol.myopia.oauth.domain.model.Oauth2Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@RestController
@RequestMapping("/oauth")
public class AuthController {
    @Autowired
    private TokenEndpoint tokenEndpoint;

    /**
     * 认证生成token，OAuth2默认支持为该接口提供客户端参数校验，不用自己去判断来的客户端是否合法
     * @param principal
     * @param parameters
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/token")
    public ApiResult postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        String clientId = parameters.get("client_id");
        if (SystemCode.getByCode(Integer.valueOf(clientId)) == null) {
            return ApiResult.failure("client_id错误");
        }
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2Token oauth2Token = Oauth2Token.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .build();
        // TODO; 如果没有把用户的基本信息放在JWT，则需要把token和用户信息放到Redis缓存，其他端可以解密JWT或者到缓存拿到用户信息
        return ApiResult.success(oauth2Token);
    }

}