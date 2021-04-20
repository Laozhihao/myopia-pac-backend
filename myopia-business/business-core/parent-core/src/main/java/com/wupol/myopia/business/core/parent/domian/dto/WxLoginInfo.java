package com.wupol.myopia.business.core.parent.domian.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

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
    @NotBlank(message = "手机号码不能为空")
    private String phone;
    /**
     * 短信验证码
     **/
    @NotBlank(message = "验证码不能为空")
    private String smsCode;
    /**
     * 微信用户的唯一标识
     **/
    @NotBlank(message = "openId不能为空")
    private String openId;
}
