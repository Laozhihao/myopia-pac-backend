package com.wupol.myopia.business.management.exception;

import com.wupol.myopia.base.exception.BusinessException;

/**
 *
 * @author Alix
 * @date 2018/10/16
 */
public class SerializeErrorException extends BusinessException {

    public SerializeErrorException(String msg) {
        super(msg);
    }

    public SerializeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
