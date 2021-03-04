package com.wupol.myopia.business.parent.constant;

/**
 * @Author HaoHao
 * @Date 2021/3/2
 **/
public interface WxConstant {

    String WX_AUTHORIZE_BASE_FULL_URL = "%s?appid=%s&redirect_uri=%s/parent/wx/callback/login&response_type=code&scope=snsapi_base&state=state#wechat_redirect";
    String WX_AUTHORIZE_USER_INFO_FULL_URL = "%s?appid=%s&redirect_uri=%s/parent/wx/callback/userInfo&response_type=code&scope=snsapi_userinfo&state=state#wechat_redirect";
    String WX_H5_CLIENT_URL = "%s/#/?code=%s";

    String ACCESS_TOKEN = "access_token";
    String WX_ERROR_CODE = "errcode";
    String WX_ERROR_MSG = "errmsg";

    String ZONE = "+86";

    long EXPIRED_SECONDS = 300;
    long EXPIRED_MINUTE = 5;
    long FAIL_MAX_TIME = 5;
    /**
     * 短信验证码
     */
    String SMS_REG_PREFIX = "MSG_TOKEN_%s";
    /**
     * 短信校验失败数量
     */
    String SMS_TOKEN_FAIL_COUNT = "TOKEN_FAIL_COUNT_%s";
}
