package com.wupol.myopia.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author HaoHao
 * @Date 2020/12/14
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult {
    private Integer code;
    private String message;
    private Object data;

    public static ApiResult success(String msg) {
        return new ApiResult(ResultStatus.SUCCESS.getCode(), msg, null);
    }

    public static ApiResult success(Object data) {
        return new ApiResult(ResultStatus.SUCCESS.getCode(), "success", data);
    }

    public static ApiResult failure(String msg) {
        return new ApiResult(ResultStatus.BAD_REQUEST.getCode(), msg, null);
    }

    public static ApiResult failure(Integer code, String msg) {
        return new ApiResult(code, msg, null);
    }
}
