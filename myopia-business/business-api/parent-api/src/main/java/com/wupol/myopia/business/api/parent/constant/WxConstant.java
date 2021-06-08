package com.wupol.myopia.business.api.parent.constant;

/**
 * @Author HaoHao
 * @Date 2021/3/2
 **/
public interface WxConstant {
    /**
     * 微信静默授权地址
     **/
    String WX_AUTHORIZE_BASE_FULL_URL = "%s?appid=%s&redirect_uri=%s/parent/wx/callback/login&response_type=code&scope=snsapi_base&state=%s#wechat_redirect";
    /**
     * 获取微信用户个人信息授权地址
     **/
    String WX_AUTHORIZE_USER_INFO_FULL_URL = "%s?appid=%s&redirect_uri=%s/parent/wx/callback/userInfo&response_type=code&scope=snsapi_userinfo&state=%s#wechat_redirect";

    /**
     * 家长端前端地址
     **/
    String WX_H5_CLIENT_URL = "%s/#/middle-transform?code=%s";
    /**
     * 家长端前端地址，带openId
     **/
    String WX_H5_CLIENT_URL_WITH_OPENID = "%s/#/middle-transform?code=%s&openId=%s&state=%s";
    /**
     * 家长端前端地址，带登录token相关信息
     **/
    String WX_H5_CLIENT_URL_WITH_TOKEN = "%s/#/middle-transform?code=%s&accessToken=%s&refreshToken=%s&expiresIn=%d";

    /**
     * 微信api返回数据 - accessToken字段名
     **/
    String ACCESS_TOKEN = "access_token";
    /**
     * 微信api返回数据 - 错误码字段名
     **/
    String WX_ERROR_CODE = "errcode";
    /**
     * 微信api返回数据 - 错误信息字段名
     **/
    String WX_ERROR_MSG = "errmsg";
}
