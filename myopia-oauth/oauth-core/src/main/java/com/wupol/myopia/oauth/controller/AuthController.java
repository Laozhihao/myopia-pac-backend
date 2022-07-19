package com.wupol.myopia.oauth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.wupol.myopia.base.annotation.LimitedAccess;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.oauth.domain.dto.LoginDTO;
import com.wupol.myopia.oauth.domain.dto.RefreshTokenDTO;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.vo.CaptchaImageVO;
import com.wupol.myopia.oauth.domain.vo.LoginInfoVO;
import com.wupol.myopia.oauth.domain.vo.TokenInfoVO;
import com.wupol.myopia.oauth.service.AuthService;
import com.wupol.myopia.oauth.service.ImageService;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    @Autowired
    private ImageService imageService;

    private static final String REFRESH_TOKEN_ERROR = "刷新令牌失败";


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
        //判断图片验证
        if (!imageService.verify(loginDTO.getVerify(),loginDTO.getClient_id())){
            return ApiResult.failure("图片滑块校验失败!");
        }
        // 生成token
        loginDTO.setGrant_type(AuthConstants.GRANT_TYPE_PASSWORD);
        Map<String, String> parameters = JSON.parseObject(JSON.toJSONString(loginDTO), new TypeReference<Map<String, String>>(){});
        OAuth2AccessToken oAuth2AccessToken;
        try {
            oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        } catch (InvalidGrantException e) {
            logger.error("登录失败", e);
            return ApiResult.failure("账号或密码错误!");
        } catch (Exception e) {
            logger.error("登录失败", e);
            return ApiResult.failure(e.getMessage());
        }
        if (Objects.isNull(oAuth2AccessToken)) {
            return ApiResult.failure("登录失败");
        }
        CurrentUser currentUser = authService.parseToken(oAuth2AccessToken.getValue());
        Integer userId = currentUser.getId();
        // 获取菜单权限，并缓存token和权限
        List<Permission> permissions = authService.cachePermissionAndToken(userId, currentUser.getSystemCode(), currentUser.getUserType(),
                oAuth2AccessToken.getExpiresIn(), oAuth2AccessToken.getValue());
        // 更新用户最后登录时间，问卷系统用户无需更新
        if (!SystemCode.QUESTIONNAIRE.getCode().equals(currentUser.getSystemCode())) {
            userService.updateById(new User().setId(userId).setLastLoginTime(new Date()));
        }
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
            logger.error("无效的刷新令牌", e);
            return ApiResult.failure("无效的刷新令牌");
        } catch (Exception e) {
            logger.error(REFRESH_TOKEN_ERROR, e);
            return ApiResult.failure(REFRESH_TOKEN_ERROR);
        }
        if (Objects.isNull(oAuthToken)) {
            return ApiResult.failure(REFRESH_TOKEN_ERROR);
        }
        // 延长权限缓存过期时间
        authService.delayPermissionAndTokenCache(oAuthToken.getValue(), oAuthToken.getRefreshToken().getValue(), oAuthToken.getExpiresIn());
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
        redisUtil.del(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getId()));
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

    /**
     * 获取验证图片
     * @param imageId 图片ID
     */
    @LimitedAccess
    @GetMapping("/verify/image")
    public ApiResult<CaptchaImageVO> getVerifyImage(@RequestParam(required = false) Integer imageId ) {
        CaptchaImageVO captchaImageVO = imageService.getVerifyImage(imageId);
        return ApiResult.success(captchaImageVO);
    }


}