package com.wupol.myopia.oauth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.oauth.domain.dto.LoginDTO;
import com.wupol.myopia.oauth.domain.dto.LoginInfoDTO;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.vo.Oauth2TokenVO;
import com.wupol.myopia.oauth.service.PermissionService;
import com.wupol.myopia.oauth.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@RestController
public class AuthController {
    private static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private TokenEndpoint tokenEndpoint;
    @Autowired
    private KeyPair keyPair;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    /**
     * 认证生成token，OAuth2默认支持为该接口提供客户端参数校验，不用自己去判断来的客户端是否合法
     *
     * @param principal
     * @param parameters
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/oauth/token")
    public ApiResult postAccessToken(Principal principal, @RequestParam Map<String, String> parameters)
            throws HttpRequestMethodNotSupportedException {
        String clientId = parameters.get(AuthConstants.JWT_CLIENT_ID_KEY);
        if (SystemCode.getByCode(Integer.valueOf(clientId)) == null) {
            return ApiResult.failure("client_id错误");
        }
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenVO oauth2Token = Oauth2TokenVO.builder().token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn()).build();
        // TODO: 方案一：这里同时返回用户的权限菜单树，前端动态渲染菜单；
        // TODO: 方案二：提供获取权限菜单树接口，前端拿到token后再次请求获取
        return ApiResult.success(oauth2Token);
    }

    @PostMapping("/login")
    public ApiResult getResource(LoginDTO loginDTO) {
        String clientId = loginDTO.getClientId();
        if (SystemCode.getByCode(Integer.valueOf(clientId)) == null) {
            return ApiResult.failure("system错误");
        }
        Map<String, String> parameters = new HashMap();
        parameters.put(OAuth2Utils.CLIENT_ID, clientId);
        parameters.put(OAuth2Utils.GRANT_TYPE, AuthorizationGrantType.PASSWORD.getValue());

        OAuth2AccessToken oAuth2AccessToken;
        Oauth2TokenVO oauth2Token;
        try {
            oAuth2AccessToken = tokenEndpoint.postAccessToken(loginDTO, parameters).getBody();
            oauth2Token = Oauth2TokenVO.builder().token(oAuth2AccessToken.getValue())
                    .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                    .expiresIn(oAuth2AccessToken.getExpiresIn()).build();
        } catch (HttpRequestMethodNotSupportedException e) {
            logger.error("Failed to get access token", e);
            return ApiResult.failure("内部错误");
        }

        // User user = userService.getByUsername(loginDTO.getUsername(), clientId);
        // LoginInfoDTO.UserInfo userInfo =
        // LoginInfoDTO.UserInfo.builder().userId(user.getId()).roleType(user.get);
        // List<Permission> permissions =
        // permissionService.getUserPermissionByUserId(user.getId());
        // LoginInfoDTO loginInfo =
        // LoginInfoDTO.builder().accessToken(oauth2Token.getToken()).userInfo(userInfo).build();

        return ApiResult.success(oauth2Token);
    }

    /**
     * 获取rsa公钥
     *
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @GetMapping("/rsa/publicKey")
    public Map<String, Object> getPublicKey() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();
    }

    /**
     * 获取rsa公私钥 TODO：测试用，上线前关闭该接口
     *
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @GetMapping("/rsa/key")
    public ApiResult getPrivateKey() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String pubKey = "-----BEGIN PUBLIC KEY-----" + new String(Base64.encode(publicKey.getEncoded()))
                + "-----END PUBLIC KEY-----";
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String priKey = "-----BEGIN RSA PRIVATE KEY-----" + new String(Base64.encode(privateKey.getEncoded()))
                + "-----END RSA PRIVATE KEY-----";
        HashMap<String, String> keyMap = new HashMap<>(3);
        keyMap.put("publicKey", pubKey);
        keyMap.put("privateKey", priKey);
        return ApiResult.success(keyMap);
    }

}