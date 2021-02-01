package com.wupol.myopia.oauth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.oauth.domain.dto.LoginDTO;
import com.wupol.myopia.oauth.domain.dto.RefreshTokenDTO;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.vo.LoginInfoVO;
import com.wupol.myopia.oauth.domain.vo.TokenInfoVO;
import com.wupol.myopia.oauth.service.AuthService;
import com.wupol.myopia.oauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;


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
    private AuthService authService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;

    /**
     * 登录
     *
     * - 判断来源客户端合法性（ClientCredentialsAccessTokenFilter中拦截校验）
     * - 校验账号密码正确性
     * - 生成并返回token、菜单权限数据
     *
     * @param principal 客户端信息
     * @param loginDTO  用户账号与授权类型信息
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/login")
    public ApiResult login(Principal principal, LoginDTO loginDTO) throws ParseException {
        // 生成token
        loginDTO.setGrant_type(AuthConstants.GRANT_TYPE_PASSWORD);
        Map<String, String> parameters = JSON.parseObject(JSON.toJSONString(loginDTO), new TypeReference<Map<String, String>>(){});
        OAuth2AccessToken oAuth2AccessToken;
        try {
            oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        } catch (InvalidGrantException e) {
            logger.error("登录失败", e);
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("登录失败", e);
            return ApiResult.failure("登录失败");
        }
        if (Objects.isNull(oAuth2AccessToken)) {
            return ApiResult.failure("登录失败");
        }
        // 获取菜单权限，并缓存
        List<Permission> permissions = authService.cacheUserPermission(loginDTO.getUsername(), Integer.parseInt(loginDTO.getClient_id()), oAuth2AccessToken.getExpiresIn());
        // 更新用户最后登录时间
        CurrentUser currentUser = authService.parseToken(oAuth2AccessToken.getValue());
        userService.updateById(new User().setId(currentUser.getId()).setLastLoginTime(new Date()));
        return ApiResult.success(new LoginInfoVO(oAuth2AccessToken, permissions));
    }

    /**
     * 刷新token
     *
     * - 判断来源客户端合法性（ClientCredentialsAccessTokenFilter中拦截校验）
     * - 检验 refresh_token
     * - 生成新的token信息
     *
     * @param principal 客户端信息
     * @param refreshToken  refresh_token与授权类型信息
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/refresh/token")
    public ApiResult refreshAccessToken(Principal principal, RefreshTokenDTO refreshToken) throws ParseException {
        // 获取新token
        refreshToken.setGrant_type(AuthConstants.GRANT_TYPE_REFRESH_TOKEN);
        Map<String, String> parameters = JSON.parseObject(JSON.toJSONString(refreshToken), new TypeReference<Map<String, String>>() {});
        OAuth2AccessToken oAuthToken;
        try {
            oAuthToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        } catch (InvalidTokenException e) {
            return ApiResult.failure("无效的刷新令牌");
        } catch (Exception e) {
            return ApiResult.failure("刷新令牌失败");
        }
        if (Objects.isNull(oAuthToken)) {
            return ApiResult.failure("刷新令牌失败");
        }
        // 延长权限缓存过期时间
        authService.delayPermissionCache(refreshToken.getRefresh_token(), oAuthToken.getExpiresIn());
        return ApiResult.success(new TokenInfoVO(oAuthToken.getValue(), oAuthToken.getRefreshToken().getValue(), oAuthToken.getExpiresIn()));
    }

    /**
     * 退出登录
     *
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/exit")
    public ApiResult logout() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        redisUtil.del(String.format(RedisConstant.USER_PERMISSION_KEY, currentUser.getId()));
        return ApiResult.success();
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