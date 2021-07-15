package com.wupol.myopia.business.api.screening.app.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {
    UNKNOWN_ERROR(-1,"未知异常"),
    SYS_SCHOOL_IS_NOT_EXIST(12008,"该学校存在"),
    SYS_STUDENT_BIRTHDAY_FORMAT_ERROR(15001,"学生生日格式错误"),
    SYS_STUDENT_SCHOOL_NULL(15004,"请输入学校ID");

    private final Integer code;
    private final String message;
    ErrorEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
