package com.wupol.myopia.oauth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.oauth.domain.dto.LoginDTO;
import com.wupol.myopia.oauth.domain.vo.Oauth2TokenVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
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

    /**
     * 认证生成token，OAuth2默认支持为该接口提供客户端参数校验，不用自己去判断来的客户端是否合法
     *
     * @param principal
     * @param loginDTO
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/login")
    public ApiResult login(Principal principal, LoginDTO loginDTO)
            throws HttpRequestMethodNotSupportedException {
        String clientId = loginDTO.getClient_id();
        if (SystemCode.getByCode(Integer.valueOf(clientId)) == null) {
            return ApiResult.failure("client_id错误");
        }
        Map<String, String> parameters = JSON.parseObject(JSON.toJSONString(loginDTO),
                new TypeReference<Map<String, String>>() {
                });
        OAuth2AccessToken oAuth2AccessToken =
                tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenVO oauth2Token = Oauth2TokenVO.builder().token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn()).build();
        // TODO: 方案一：这里同时返回用户的权限菜单树，前端动态渲染菜单；
        // TODO: 方案二：提供获取权限菜单树接口，前端拿到token后再次请求获取
        return ApiResult.success(oauth2Token);
    }

    /**
     * 刷新token
     *
     * @param principal
     * @param loginDTO
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/refresh/token")
    public ApiResult refreshAccessToken(Principal principal, LoginDTO loginDTO)
            throws HttpRequestMethodNotSupportedException {
        String clientId = loginDTO.getClient_id();
        if (SystemCode.getByCode(Integer.valueOf(clientId)) == null) {
            return ApiResult.failure("client_id错误");
        }
        Map<String, String> parameters = JSON.parseObject(JSON.toJSONString(loginDTO),
                new TypeReference<Map<String, String>>() {
                });
        OAuth2AccessToken oAuth2AccessToken =
                tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenVO oauth2Token = Oauth2TokenVO.builder().token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn()).build();
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
        String pubKey = "-----BEGIN PUBLIC KEY-----"
                + new String(Base64.getEncoder().encode(publicKey.getEncoded()))
                + "-----END PUBLIC KEY-----";
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String priKey = "-----BEGIN RSA PRIVATE KEY-----"
                + new String(Base64.getEncoder().encode(privateKey.getEncoded()))
                + "-----END RSA PRIVATE KEY-----";
        HashMap<String, String> keyMap = new HashMap<>(3);
        keyMap.put("publicKey", pubKey);
        keyMap.put("privateKey", priKey);
        return ApiResult.success(keyMap);
    }

}
