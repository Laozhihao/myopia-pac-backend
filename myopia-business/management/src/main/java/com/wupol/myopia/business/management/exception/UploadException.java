package com.wupol.myopia.business.management.exception;

/**
 * @author Alix
 * @Date 2021/02/03
 */
public class UploadException extends RuntimeException {
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadException(String msg) {
        super(msg);
    }

}
