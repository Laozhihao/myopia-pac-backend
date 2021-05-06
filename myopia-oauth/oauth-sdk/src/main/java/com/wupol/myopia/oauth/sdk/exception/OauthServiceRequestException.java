package com.wupol.myopia.oauth.sdk.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Oauth服务请求失败异常
 *
 * @Author HaoHao
 * @Date 2021/5/6
 **/
@Getter
public class OauthServiceRequestException extends RuntimeException {

    private int code;

    public OauthServiceRequestException(String message) {
        super(message);
        this.code = HttpStatus.BAD_REQUEST.value();
    }

    public OauthServiceRequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public OauthServiceRequestException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
