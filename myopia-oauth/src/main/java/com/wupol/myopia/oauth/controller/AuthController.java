package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.oauth.constant.AuthConstants;
import com.wupol.myopia.oauth.domain.model.Oauth2Token;
import org.apache.commons.lang3.StringUtils;
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

    /*@ApiOperation("OAuth2认证生成token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "grant_type", defaultValue = "password", value = "授权模式", required = true),
            @ApiImplicitParam(name = "client_id", defaultValue = "client", value = "Oauth2客户端ID", required = true),
            @ApiImplicitParam(name = "client_secret", defaultValue = "123456", value = "Oauth2客户端秘钥", required = true),
            @ApiImplicitParam(name = "refresh_token", value = "刷新token"),
            @ApiImplicitParam(name = "username", defaultValue = "admin", value = "登录用户名"),
            @ApiImplicitParam(name = "password", defaultValue = "123456", value = "登录密码"),

            @ApiImplicitParam(name = "code", value = "小程序code"),
            @ApiImplicitParam(name = "encryptedData", value = "包括敏感数据在内的完整用户信息的加密数据"),
            @ApiImplicitParam(name = "iv", value = "加密算法的初始向量"),
    })*/
    /**
     * OAuth2认证生成token
     * @param principal
     * @param parameters
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/token")
    public ApiResult postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        String clientId = parameters.get("client_id");
        switch (clientId) {
            // 微信认证
            case AuthConstants.WEAPP_CLIENT_ID:
                return this.handleForWxAuth(principal, parameters);
            default:
                OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
                Oauth2Token oauth2Token = Oauth2Token.builder()
                        .token(oAuth2AccessToken.getValue())
                        .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                        .expiresIn(oAuth2AccessToken.getExpiresIn())
                        .build();
                return ApiResult.success(oauth2Token);
        }
    }


    public ApiResult handleForWxAuth(Principal principal, Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        String code = parameters.get("code");
        if (StringUtils.isBlank(code)) {
            throw new BusinessException("code不能为空");
        }
        // 根据code获取openId，再根据openId获取用户信息
        String username = "";

        // oauth2认证参数对应授权登录时注册会员的username、password信息，模拟通过oauth2的密码模式认证
        parameters.put("username", username);
        parameters.put("password", username);

        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2Token oauth2Token = Oauth2Token.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .build();
        return ApiResult.success(oauth2Token);
    }


}