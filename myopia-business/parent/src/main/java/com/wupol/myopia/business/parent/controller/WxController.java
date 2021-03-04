package com.wupol.myopia.business.parent.controller;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.login.LoginInfoDTO;
import com.wupol.myopia.business.parent.constant.WxBusinessExceptionCodeEnum;
import com.wupol.myopia.business.parent.constant.WxConstant;
import com.wupol.myopia.business.parent.domain.dto.WxAccessTokenInfo;
import com.wupol.myopia.business.parent.domain.dto.WxLoginInfo;
import com.wupol.myopia.business.parent.domain.dto.WxUserInfo;
import com.wupol.myopia.business.parent.domain.model.Parent;
import com.wupol.myopia.business.parent.service.ParentService;
import com.wupol.myopia.business.parent.service.WxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

/**
 * 微信授权与登录处理相关
 *
 * @Author HaoHao
 * @Date 2021/2/26
 **/
@CrossOrigin
@Controller
@RequestMapping("/parent/wx")
public class WxController {
    private static final Logger logger = LoggerFactory.getLogger(WxController.class);

    @Value("${wechat.app.id}")
    private String appId;
    @Value("${wechat.app.secret}")
    private String appSecret;
    @Value("${wechat.authorize.url}")
    private String wechatAuthorizeUrl;
    @Value("${wechat.callback.url-host}")
    private String wechatCallbackUrlHost;
    @Value("${wechat.h5-client.url-host}")
    private String h5ClientUrlHost;

    @Autowired
    private WxService wxService;
    @Autowired
    private ParentService parentService;
    @Autowired
    private OauthService oauthService;

    /**
     * 家长端入口，访问微信api获取授权code
     **/
    @GetMapping("/index")
    public String getCode() {
        return "redirect:" + String.format(WxConstant.WX_AUTHORIZE_BASE_FULL_URL, wechatAuthorizeUrl, appId, wechatCallbackUrlHost);
    }

    /**
     * 唤起获取微信头像、昵称、地区和性别信息授权页面地址
     **/
    @GetMapping("/authorize")
    public String authorize() {
        return "redirect:" + String.format(WxConstant.WX_AUTHORIZE_USER_INFO_FULL_URL, wechatAuthorizeUrl, appId, wechatCallbackUrlHost);
    }

    /**
     * 微信授权回调
     *
     * @param code code
     * @return java.lang.String
     **/
    @GetMapping("/callback/login")
    public String wxCallbackToLogin(String code) {
        logger.debug("【微信回调-login】code = " + code);
        try {
            // 获取openId
            String openId = wxService.getOpenId(code);
            // 根据openId判断用户是否授权，未授权则跳到“用户协议”页面
            Parent parent = parentService.getPatientByOpenId(openId);
            if (Objects.isNull(parent)) {
                return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.UNAUTHORIZED.getCode());
            }
            // 判断用户是否已经绑定手机号码，未绑定则跳到“绑定手机”页面
            UserDTO user = oauthService.getUserDetailByUserId(parent.getUserId());
            if (Objects.isNull(user) || StringUtils.isEmpty(user.getPhone())) {
                return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.FORBIDDEN.getCode()) + "&openId=" + parent.getHashKey();
            }
            // 自动登录
            LoginInfoDTO loginInfo = oauthService.login("5", "123456", user.getPhone(), openId);
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.OK.getCode())
                    + "&accessToken=" + loginInfo.getTokenInfo().getAccessToken()
                    + "&refreshToken=" + loginInfo.getTokenInfo().getRefreshToken() + "&expiresTime=" + loginInfo.getTokenInfo().getExpiresIn();
        } catch (Exception e) {
            logger.error("生成openId的hashKey失败", e);
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.INTERNAL_ERROR.getCode());
        }
    }

    @GetMapping("/callback/userInfo")
    public String wxCallbackToCreateUser(String code) {
        logger.debug("【微信回调-userInfo】code = " + code);
        try {
            // 获取 accessToken 和 openId
            WxAccessTokenInfo accessTokenWithOpenId = wxService.getAccessTokenWithOpenId(code);
            // 获取用户个人信息
            WxUserInfo wxUserInfo = wxService.getWxUserInfo(accessTokenWithOpenId);
            // 创建家长和用户
            Parent parent = wxService.addParentAndUser(wxUserInfo);
            // 跳到“绑定手机”页面
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.FORBIDDEN.getCode()) + "&openId=" + parent.getHashKey();
        } catch (Exception e) {
            logger.error("获取微信用户个人信息失败", e);
            return "redirect:"+ String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.INTERNAL_ERROR.getCode());
        }
    }

    @GetMapping("/smsCode/{phone}")
    @ResponseBody
    public ApiResult sendSmsCode(@PathVariable String phone) {
        wxService.sendSms(phone);
        return ApiResult.success();
    }

    @PostMapping("/phone/bind")
    @ResponseBody
    public ApiResult bindPhoneToParent(@RequestBody WxLoginInfo wxLoginInfo) throws IOException {
        // 校验短信验证码
        wxService.checkVerifyCode(wxLoginInfo.getPhone(), wxLoginInfo.getSmsCode());
        // 家长绑定手机
        wxService.bindPhoneToParent(wxLoginInfo);
        // 自动登录
        LoginInfoDTO loginInfo = oauthService.login("5", "123456", wxLoginInfo.getPhone(), wxLoginInfo.getOpenId());
        return ApiResult.success(loginInfo);
    }
}
