package com.wupol.myopia.business.management.exception;

/**
 *
 * @author Alix
 * @date 2018/10/16
 */
public class SerializeErrorException extends RuntimeException {

    public SerializeErrorException(String msg) {
        super(msg);
    }

    public SerializeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
