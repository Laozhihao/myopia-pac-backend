package com.wupol.myopia.business.common.exceptions;

/**
 * @Description 部分受检异常不可能出现，但是又不想吃掉，则转换为runtimeException ,其中，必须将实际异常嵌入到该包装异常中
 * @Date 2021/1/22 18:37
 * @Author by Jacob
 */
public class ManagementUncheckedException extends RuntimeException {

    public ManagementUncheckedException(Throwable throwable) {
        super(throwable);
    }

    public ManagementUncheckedException(Throwable throwable, String msg) {
        super(msg, throwable);
    }

    public ManagementUncheckedException(String msg) {
        super(msg);
    }
}
