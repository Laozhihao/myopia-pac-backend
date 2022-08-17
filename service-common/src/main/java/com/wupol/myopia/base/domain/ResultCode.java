package com.wupol.myopia.base.domain;

import lombok.Getter;

/**
 * 通用接口响应状态码
 *
 * @Author HaoHao
 * @Date 2020/12/14 19:42
 **/
@Getter
public enum ResultCode {
    /** 请求成功 */
    SUCCESS(200, "OK"),
    /** 请求失败 */
    BAD_REQUEST(400, "Bad Request"),
    /** 系统异常 */
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后重试"),

    CLIENT_AUTHENTICATION_FAILED(401,"客户端认证失败"),
    USER_ACCESS_UNAUTHORIZED (401, "访问未授权"),
    TOKEN_INVALID_OR_EXPIRED(403, "token无效或已过期"),

    DATA_UPLOAD_DEVICE_ERROR(5001,"无法找到设备"),
    DATA_UPLOAD_SCREENING_ORG_ERROR(5002,"筛查机构异常"),
    DATA_UPLOAD_DATA_EMPTY_ERROR(5003,"数据不能为空"),
    DATA_UPLOAD_DATA_OUT_DATE(5004,"数据已过期"),

    DATA_UPLOAD_PLAN_STUDENT_ERROR(6001, "筛查学生异常"),
    DATA_UPLOAD_PLAN_STUDENT_MATCH_ERROR(6002, "学生筛查机构匹配异常"),

    DATA_STUDENT_NOT_EXIST(7001,"账号密码错误，请重新操作！"),
    DATA_STUDENT_PLAN_NOT_EXIST(7002,"暂不需要填写问卷")


    ;

    /** 业务异常码 */
    private final Integer code;
    /** 业务异常信息描述 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
