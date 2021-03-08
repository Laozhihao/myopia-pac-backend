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
}
