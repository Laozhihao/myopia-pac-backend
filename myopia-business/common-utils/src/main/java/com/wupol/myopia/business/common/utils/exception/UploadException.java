package com.wupol.myopia.business.common.utils.exception;

import com.wupol.myopia.base.exception.BusinessException;

/**
 * @author Alix
 * @Date 2021/02/03
 */
public class UploadException extends BusinessException {
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadException(String msg) {
        super(msg);
    }

}
