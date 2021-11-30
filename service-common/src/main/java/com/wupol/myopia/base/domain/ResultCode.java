package com.wupol.myopia.base.domain;

import lombok.Getter;

/**
 * 通用接口响应状态码
 *
 * @Author HaoHao
 * @Date 2020/12/14 19:42
 **/
@Getter
public enum ResultCode {
    /** 请求成功 */
    SUCCESS(200, "OK"),
    /** 请求失败 */
    BAD_REQUEST(400, "Bad Request"),
    /** 系统异常 */
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后重试"),

    CLIENT_AUTHENTICATION_FAILED(401,"客户端认证失败"),
    USER_ACCESS_UNAUTHORIZED (401, "访问未授权"),
    TOKEN_INVALID_OR_EXPIRED(403, "token无效或已过期"),

    ;

    /** 业务异常码 */
    private Integer code;
    /** 业务异常信息描述 */
    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
