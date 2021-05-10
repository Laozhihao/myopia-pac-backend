package com.wupol.myopia.business.management.exception;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据不存在异常
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class DataNotExistException extends BusinessException {

    public DataNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotExistException(String message) {
        super(message);
    }

 }