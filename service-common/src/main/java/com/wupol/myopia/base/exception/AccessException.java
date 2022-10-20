package com.wupol.myopia.base.exception;

import com.wupol.myopia.base.domain.ResultCode;

/**
 * 权限校验
 *
 * @author Simple4H
 */
public class AccessException extends BusinessException{

    public AccessException(String message) {
        super(message, ResultCode.CLIENT_AUTHENTICATION_FAILED.getCode());
    }
}
