package com.wupol.myopia.base.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Feign服务请求失败异常
 *
 * @Author HaoHao
 * @Date 2021/5/6
 **/
@Getter
public class BusinessServiceRequestException extends RuntimeException {

    private final int code;

    public BusinessServiceRequestException(String message) {
        super(message);
        this.code = HttpStatus.BAD_REQUEST.value();
    }

    public BusinessServiceRequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BusinessServiceRequestException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
