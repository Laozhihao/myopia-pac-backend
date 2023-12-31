package com.wupol.myopia.business.api.parent.controller;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.api.parent.constant.ParentClientConstant;
import com.wupol.myopia.business.api.parent.constant.WxBusinessExceptionCodeEnum;
import com.wupol.myopia.business.api.parent.constant.WxConstant;
import com.wupol.myopia.business.api.parent.domain.dto.WxAuthorizationInfo;
import com.wupol.myopia.business.api.parent.domain.dto.WxLoginInfo;
import com.wupol.myopia.business.api.parent.domain.dto.WxUserInfo;
import com.wupol.myopia.business.api.parent.service.SmsService;
import com.wupol.myopia.business.api.parent.service.WxService;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.LoginInfo;
import com.wupol.myopia.oauth.sdk.domain.response.TokenInfo;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 微信授权与登录处理相关
 *
 * @Author HaoHao
 * @Date 2021/2/26
 **/
@Validated
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
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private SmsService smsService;

    /**
     * 家长端入口，访问微信api获取授权code
     * 访问流程：
     *      1. Index?state=1访问入口
     *      2. 重定向到微信获取code
     *      3. 微信回调到“/callback/login”接口
     *      4. 根据code从微信获取openId
     *      5. 重定向到前端“用户协议”页面（或重定向到步骤11；或自动登录重定向到前端中间页，前端最终根据state跳转到对应页面）
     *      6. 点击按钮重定向到“/authorize”接口
     *      7. 服务器重定向到微信获取code
     *      8. 微信回调到“/callback/userInfo”接口
     *      9. 根据code调微信获取accessToken和OpenId
     *      10.根据accessToken和OpenId调微信获取userInfo(昵称、头像)
     *      11.重定向“绑定手机”页面
     *      12.绑定手机成功后，前端根据state跳转到对应页面
     * state取值范围
     *      1 - 默认值，会跳到“报告查看”页面
     *      2 - 扫码进来，会跳到“我的孩子”页面
     **/
    @GetMapping("/index")
    public String getCode(String state) {
        logger.info("家长端入口，访问微信api获取授权code，获取state:{}", state);
        return "redirect:" + String.format(WxConstant.WX_AUTHORIZE_BASE_FULL_URL, wechatAuthorizeUrl, appId, wechatCallbackUrlHost, state);
    }

    /**
     * 唤起获取微信头像、昵称、地区和性别信息授权页面地址
     **/
    @GetMapping("/authorize")
    public String authorize(String state) {
        logger.debug("唤起获取微信头像、昵称、地区和性别信息授权页面地址，获取state:{}", state);
        return "redirect:" + String.format(WxConstant.WX_AUTHORIZE_USER_INFO_FULL_URL, wechatAuthorizeUrl, appId, wechatCallbackUrlHost, state);
    }

    /**
     * 微信授权回调-检查是否授权登录、绑定手机号码
     *
     * @param code code
     * @return java.lang.String
     **/
    @GetMapping("/callback/login")
    public String wxCallbackToLogin(String code, String state) {
        logger.debug("微信授权回调-检查是否授权登录、绑定手机号码，获取state:{}", state);
        try {
            // 获取openId
            String openId = wxService.getOpenId(code);
            // 根据openId判断用户是否授权，未授权则跳到“用户协议”页面
            Parent parent = parentService.getParentByOpenId(openId);
            if (Objects.isNull(parent)) {
                String url = String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.UNAUTHORIZED.getCode(), state);
                logger.debug("重定向到协议页面：{}", url);
                return "redirect:" + url;
            }
            // 判断用户是否已经绑定手机号码，未绑定则跳到“绑定手机”页面
            User user = oauthServiceClient.getUserDetailByUserId(parent.getUserId());
            if (Objects.isNull(user) || StringUtils.isEmpty(user.getPhone())) {
                String url = String.format(WxConstant.WX_H5_CLIENT_URL_WITH_OPENID, h5ClientUrlHost, WxBusinessExceptionCodeEnum.FORBIDDEN.getCode(), URLEncoder.encode(parent.getHashKey(), StandardCharsets.UTF_8.toString()), state);
                logger.debug("重定向到绑定手机页面页面：{}", url);
                return "redirect:" + url;
            }
            // 自动登录
            LoginInfo loginInfo = oauthServiceClient.login(ParentClientConstant.PARENT_CLIENT_ID, ParentClientConstant.PARENT_CLIENT_SECRET, user.getPhone(), parent.getHashKey());
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL_WITH_TOKEN, h5ClientUrlHost,
                    WxBusinessExceptionCodeEnum.OK.getCode(),
                    loginInfo.getTokenInfo().getAccessToken(),
                    loginInfo.getTokenInfo().getRefreshToken(),
                    loginInfo.getTokenInfo().getExpiresIn(),
                    state);
        } catch (Exception e) {
            logger.error("微信登录失败", e);
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.INTERNAL_ERROR.getCode(), state);
        }
    }

    /**
     * 微信回调-获取微信用户个人信息
     *
     * @param code 授权code
     * @return java.lang.String
     **/
    @GetMapping("/callback/userInfo")
    public String wxCallbackToCreateUser(String code, String state) {
        logger.debug("微信回调-获取微信用户个人信息，获取state:{}", state);
        try {
            // 获取 accessToken 和 openId
            WxAuthorizationInfo accessTokenAndOpenId = wxService.getAccessTokenAndOpenId(code);
            // 获取用户个人信息
            WxUserInfo wxUserInfo = wxService.getWxUserInfo(accessTokenAndOpenId);
            // 创建家长和用户
            Parent parent = wxService.addParentAndUser(wxUserInfo);
            // 跳到“绑定手机”页面
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL_WITH_OPENID, h5ClientUrlHost, WxBusinessExceptionCodeEnum.FORBIDDEN.getCode(), URLEncoder.encode(parent.getHashKey(), StandardCharsets.UTF_8.toString()), state);
        } catch (Exception e) {
            logger.error("获取微信用户个人信息失败", e);
            return "redirect:" + String.format(WxConstant.WX_H5_CLIENT_URL, h5ClientUrlHost, WxBusinessExceptionCodeEnum.INTERNAL_ERROR.getCode(), state);
        }
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号码
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/smsCode/{phone}")
    @ResponseBody
    public ApiResult<Objects> sendSmsCode(@PathVariable String phone) {
        Assert.hasLength(phone, "手机号码不能为空");
        smsService.sendSms(phone);
        return ApiResult.success();
    }

    /**
     * 绑定家长手机号码
     *
     * @param wxLoginInfo 手机信息
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/phone/bind")
    @ResponseBody
    public ApiResult<TokenInfo> bindPhoneToParent(@RequestBody @Validated WxLoginInfo wxLoginInfo) {
        // 校验短信验证码
        boolean isRight = smsService.checkSmsCode(wxLoginInfo.getPhone(), wxLoginInfo.getSmsCode());
        if (!isRight) {
            return ApiResult.failure("验证码错误");
        }
        // 家长绑定手机
        wxService.bindPhoneToParent(wxLoginInfo);
        // 自动登录
        LoginInfo loginInfo = oauthServiceClient.login(ParentClientConstant.PARENT_CLIENT_ID, ParentClientConstant.PARENT_CLIENT_SECRET, wxLoginInfo.getPhone(), wxLoginInfo.getOpenId());
        return ApiResult.success(loginInfo.getTokenInfo());
    }
}
