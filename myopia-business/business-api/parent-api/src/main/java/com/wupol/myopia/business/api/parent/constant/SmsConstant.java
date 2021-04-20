package com.wupol.myopia.business.api.parent.constant;

/**
 * 短信验证码相关常量
 *
 * @Author HaoHao
 * @Date 2021/3/2
 **/
public interface SmsConstant {
    /**
     * 地区，大陆
     **/
    String ZONE = "+86";
    /**
     * 短信验证码有效期，单位秒
     **/
    long EXPIRED_SECONDS = 300;
    /**
     * 最近两次发送短信验证码间隔时间，单位分钟
     **/
    long EXPIRED_MINUTE = 5;
    /**
     * 短信验证码输入错误最大次数
     **/
    long FAIL_MAX_TIME = 5;
}
