package com.wupol.myopia.business.management.exception;

import com.wupol.myopia.base.exception.BusinessException;

/**
 * @author Alix
 * @Date 2021/02/03
 */
public class FileTypeException extends BusinessException {
    public FileTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileTypeException(String msg) {
        super(msg);
    }

}
