package com.wupol.myopia.base.exception;

/**
 * 业务异常信息
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
public enum BusinessExceptionMsgEnum {
    /** 参数异常 */
    PARMETER_EXCEPTION(101, "参数异常!"),
    /** 等待超时 */
    SERVICE_TIME_OUT(101, "服务调用超时！"),
    /** 参数过大 */
    PARMETER_BIG_EXCEPTION(103, "输入的图片数量不能超过50张!"),
    /** 500 : 发生异常 */
    UNEXPECTED_EXCEPTION(500, "系统发生异常，请联系管理员！");

    /**
     * 消息码
     */
    private final int code;
    /**
     * 消息内容
     */
    private final String message;

    BusinessExceptionMsgEnum(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
