package com.wupol.myopia.business.management.exception;

/**
 * @author Alix
 * @Date 2021/02/03
 */
public class FileTypeException extends RuntimeException {
    public FileTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileTypeException(String msg) {
        super(msg);
    }

}
