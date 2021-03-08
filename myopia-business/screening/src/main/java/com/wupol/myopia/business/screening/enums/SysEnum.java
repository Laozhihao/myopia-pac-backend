package com.wupol.myopia.business.screening.enums;

import lombok.Getter;

@Getter
public enum SysEnum {
    SYS_USER(1000, "用户存在"),

    SYS_USER_NULL(1001, "用户不存在"),

    SYS_USERID_NULL(1002, "请输入用户ID"),

    SYS_ROLE_NULL(2001, "角色不存在"),

    SYS_MENU_NULL(3001, "菜单不存在"),

    SYS_MENU_URL_NULL(3002, "菜单地址不能为空"),

    SYS_MENU_CODE_NULL(3002, "菜单标号不能为空"),

    SYS_MENU_NAME_NULL(3004, "菜单标号不能为空"),

    SYS_MENU_URL(3005, "菜单地址存在"),


    SYS_DEPT(4000, "筛查机构存在"),

    SYS_DEPT_NULL(4001, "筛查机构不存在"),

    SYS_IPAD_USER_NULL(4002, "筛查人员不存在"),

    SYS_DEPT_NAME_NULL(4003, "筛查机构名称不能为空"),

    SYS_DEPT_NAME_FOUL(4004, "筛查机构名称不符合规范"),

    SYS_LOG_NULL(5001, "日志不存在"),

    SYS_SCHOOL_NULL(6001, "学校不存在"),

    SYS_TASK_NULL(7001, "任务不存在"),

    SYS_STUDENT_NULL(8001, "学生不存在"),

    SYS_EXCEL_NULL(8003, "模板为空"),

    SYS_ID_CARD_EXIST(8004, "学生身份证信息已存在"),

    SYS_ID_CARD_ERROR(8005, "身份证不符合规范"),

    SYS_STUDENT_NO_EXIST(8006, "学生学籍号已存在"),

    SYS_STUDENT_NO_ERROR(8007, "学生学籍号不符合规范"),

    SYS_STUDENT_PHONE_EXIST(8008, "学生手机号信息已存在"),


    SYS_DATA_80(8050, "身份证，学籍号，手机号单条存在率小于80%"),

    SYS_DATA_STUDENT(8051, ""),

    SYS_EYE_NULL(9001, "眼睛信息为空"),



    ;

    
    private Integer code;
    private String message;
    SysEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
