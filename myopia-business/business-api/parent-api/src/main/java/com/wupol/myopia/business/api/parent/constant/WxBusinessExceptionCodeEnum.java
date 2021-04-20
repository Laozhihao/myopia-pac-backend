package com.wupol.myopia.business.api.parent.constant;


import lombok.Getter;

/**
 * 微信端业务异常code枚举类
 *
 * @Author HaoHao
 * @Date 2021/3/2
 **/
@Getter
public enum WxBusinessExceptionCodeEnum {
    // 微信端业务异常code枚举类
    UNAUTHORIZED("WX401", "未授权"),
    FORBIDDEN("WX403", "未绑定手机号码"),
    NOT_FOUND("WX404", "访问资源不存在"),
    OK("WX200", "成功"),
    INTERNAL_ERROR("WX500", "系统异常");

    /** 类型 **/
    private final String code;
    /** 描述 **/
    private final String desc;

    WxBusinessExceptionCodeEnum(String type, String desc) {
        this.code = type;
        this.desc = desc;
    }
}

