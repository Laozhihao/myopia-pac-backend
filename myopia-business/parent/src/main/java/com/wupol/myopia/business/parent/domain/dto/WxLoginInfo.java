package com.wupol.myopia.business.parent.domain.dto;

import lombok.Data;

/**
 * 微信手机登录信息
 *
 * @Author HaoHao
 * @Date 2021/3/1
 **/
@Data
public class WxLoginInfo {
    /**
     * 手机号码
     **/
    private String phone;
    /**
     * 短信验证码
     **/
    private String smsCode;
    /**
     * 微信用户的唯一标识
     **/
    private String openId;
}
