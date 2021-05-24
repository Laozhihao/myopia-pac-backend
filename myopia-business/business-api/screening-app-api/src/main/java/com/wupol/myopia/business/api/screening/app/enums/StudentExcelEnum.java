package com.wupol.myopia.business.api.screening.app.enums;

import lombok.Getter;

@Getter
public enum StudentExcelEnum {
    EXCEL_SCHOOL_NULL(120001, "【数据导入异常】学校为空"),
    EXCEL_SCHOOL_ERROR(120002, "【数据导入异常】当前学校名称和系统名称不相等"),

    EXCEL_GRADE_NULL(120003, "【数据导入异常】年级为空"),
    EXCEL_CLAZZ_NULL(120004, "【数据导入异常】班级为空"),
    EXCEL_IDCARD_REPEAT(120005, "【数据导入异常】该身份证号出现了重复，请检查:"),
    EXCEL_PHONE_NULL(120006, "【数据导入异常】手机号为空"),
    EXCEL_PHONE_ERROR(120007, "【数据导入异常】手机号为错误"),
    EXCEL_IDCARD_NULL(120008, "【数据导入异常】身份证号为空"),
    EXCEL_IDCARD_ERROR(120009, "【数据导入异常】身份证号异常:"),


    EXCEL_BIRTHDAY_NULL(120010, "【数据导入异常】出生年月日为空"),
    EXCEL_BIRTHDAY_ERROR(120011, "【数据导入异常】出生年月日错误："),
    EXCEL_SEX_NULL(120012, "【数据导入异常】性别为空"),
    EXCEL_SEX_ERROR(120013, "【数据导入异常】性别为错误"),
    EXCEL_USER(120014, "【数据导入异常】该用户已经存在"),

    GRADE_ERROR(120015, "【数据导入异常】年级格式错误"),
    CLAZZ_ERROR(120016, "【数据导入异常】班级格式错误"),
    EXCEL_STUDENT_NAME_NULL(120017, "【数据导入异常】学生名称为空"),
    EXCEL_STUDENT_NAME_ERROR(120018, "【数据导入异常】学生名称异常"),
    EXCEL_STUDENT_NO_NULL(120019, "【数据导入异常】学生学籍号为空"),
    EXCEL_STUDENT_NO_ERROR(120020, "【数据导入异常】学生学籍号格式错误"),
    EXCEL_STUDENT_NO_REPEAT(120021, "【数据导入异常】该学籍号出现了重复，请检查:"),
    ;

    private Integer code;
    private String message;
    StudentExcelEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
