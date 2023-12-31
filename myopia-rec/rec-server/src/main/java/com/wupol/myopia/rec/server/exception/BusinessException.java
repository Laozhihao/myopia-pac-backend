package com.wupol.myopia.rec.server.exception;

import com.wupol.myopia.rec.server.constant.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    /**
     * 异常码
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable t) {
        super(message, t);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(String message, Integer code, Throwable t) {
        super(message, t);
        this.code = code;
    }

}
