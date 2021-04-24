package com.wupol.myopia.oauth.sdk.exception;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

/**
 * @author jacob
 * @date 2020/9/10 12:03
 * @version 1.0.0
 * Detail: 解码异常 ,表示http状态正常,业务异常
 */
@Getter
public class OauthServiceDecodeException extends RuntimeException {

    private final Object data;

    /**
     * @param message the reason for the failure.
     */
    public OauthServiceDecodeException(String message) {
        super(message);
        data = null;
    }

    public OauthServiceDecodeException(String message, Object keyObj) {
        super(message + "," + JSONObject.toJSONString(keyObj));
        data = keyObj;
    }

    /**
     * @param message possibly null reason for the failure.
     * @param cause   the cause of the error.
     */
    public OauthServiceDecodeException(String message, Object keyObj, Throwable cause) {
        super(message+keyObj.toString(), cause);
        data = keyObj;
    }
}
