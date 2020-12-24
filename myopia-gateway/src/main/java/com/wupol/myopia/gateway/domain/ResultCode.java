package com.wupol.myopia.gateway.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    USER_ACCESS_UNAUTHORIZED (401, "访问未授权"),

    TOKEN_INVALID_OR_EXPIRED(403, "token失效");

    /** 业务异常码 */
    private Integer code;
    /** 业务异常信息描述 */
    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
