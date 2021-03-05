package com.wupol.myopia.business.parent.constant;


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
    UNAUTHORIZED("WX401", "全部住校"),
    FORBIDDEN("WX403", "部分住校"),
    NOT_FOUND("WX404", "部分住校"),
    OK("WX200", "不住校"),
    INTERNAL_ERROR("WX500", "小学");

    /** 类型 **/
    private final String code;
    /** 描述 **/
    private final String desc;

    WxBusinessExceptionCodeEnum(String type, String desc) {
        this.code = type;
        this.desc = desc;
    }
}

