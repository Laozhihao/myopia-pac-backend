package com.wupol.myopia.base.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用JSON序列化对象
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
@Data
@Accessors(chain = true)
public class ApiResult<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    public static <T> ApiResult<T> success() {
        return new ApiResult<T>().setCode(ResultCode.SUCCESS.getCode()).setMessage(ResultCode.SUCCESS.getMessage());
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<T>().setCode(ResultCode.SUCCESS.getCode()).setData(data).setMessage(ResultCode.SUCCESS.getMessage());
    }

    public static <T> ApiResult<T> failure(int code, String message) {
        return new ApiResult<T>().setCode(code).setMessage(message);
    }

    public static <T> ApiResult<T> failure(String message) {
        return failure(ResultCode.BAD_REQUEST.getCode(), message);
    }
}
