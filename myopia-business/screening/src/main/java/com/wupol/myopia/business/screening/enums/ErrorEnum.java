package com.wupol.myopia.business.screening.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {

    SYS_ADMIN_USERNAME_NULL(11001,"请输入登录名"),
    SYS_ADMIN_DEPT_NULL(11002,"请输入部门信息"),
    SYS_ADMIN_ROLE_NULL(11003,"请输入角色信息"),
    SYS_ADMIN_USER_IS_NOT_EXIST(11004,"该账号存在"),
    SYS_ADMIN_USER_SAVE_FAIL(11005,"保存失败"),
    SYS_ADMIN_USER_IS_FOUL(11006,"该账号名称不符合规范"),
    SYS_SCHOOL_BIND_FAIL_BEFORE(12001,"系统检测到其他机构和"),
    SYS_SCHOOL_BIND_FAIL_AFTER(12002,"已经存在绑定关系，不能重复绑定..."),
    SYS_SCHOOL_NAME_NULL(12003,"请输入学校名称"),
    SYS_SCHOOL_DEPT_NULL(12004,"请输入部门ID"),
    SYS_SCHOOL_USER_IS_NOT_EXIST(12005,"系统登录账号存在该用户，请联联系管理员"),
    SYS_SCHOOL_EXIST(12006,"该学校存在"),
    SYS_SCHOOL_SAVE_FAIL(12007,"学校保存失败,或者是已有部门关联改学校，请联系管理员"),
    SYS_SCHOOL_IS_NOT_EXIST(12008,"该学校存在"),
    SYS_SCHOOL_NAME_FOUL(12009,"学校名称不符合规范"),
    SYS_PASSWORD_NULL(12010,"密码不能为空"),
    SYS_USER_IS_NOT_EXIST(13001,"该学校存在"),
    SYS_DEPT_IS_NOT_EXIST(14001,"该用户不存在"),
    SYS_STUDENT_BIRTHDAY_FORMAT_ERROR(15001,"学生生日格式错误"),
    SYS_STUDENT_DEPT_NULL(15002,"请输入部门ID"),
    SYS_STUDENT_USER_NULL(15003,"请输入用户ID"),
    SYS_STUDENT_SCHOOL_NULL(15004,"请输入学校ID"),
    SYS_STUDENT_DEPT_NOT_NULL(15005,"该学校还没有绑定部门,不能进行该操作"),
    SYS_STUDENT_EYE_SCHOOL_NULL(16001,"请输入主键ID"),

    IPAD_BIOLOGY_STUDENT_NULL(21001,"请输入学生ID"),
    IPAD_BIOLOGY_DEPT_NULL(21002,"请输入部门ID"),
    IPAD_BIOLOGY_CLIENT_NULL(21003,"请输入连接客户端"),
    IPAD_BIOLOGY_SCHOOL_NULL(21004,"请输入学校ID"),
    IPAD_BIOLOGY_USER_NULL(21005,"请输入用户ID"),

    ARCHIVES_STUDENT_NULL(31001,"学生ID为空"),
    ARCHIVES_STUDENT_IS_NULL(31002,"学生为空"),
    ARCHIVES_STUDENT_ID_NULL(31003,"请输入学生ID"),
    ARCHIVES_DEPT_NULL(32001,"请输入部门ID"),
    ARCHIVES_DEPT_ID_NULL(32002,"请输入部门ID"),
    ARCHIVES_DEPT_INFO_NULL(32003,"请输入部门信息"),
    ARCHIVES_DEPT_IS_NOT_EXIST(32004,"部门不存在"),
    ARCHIVES_SCHOOL_ID_NULL(33001,"请输入学校ID"),
    ARCHIVES_SCHOOL_NULL(33002,"学校为空"),
    ARCHIVES_ID_NULL(34001,"请输入ID"),
    ARCHIVES_USER_IS_NOT_EXIST(34002,"该用户不存在"),
    ARCHIVES_FILE_PATH_IS_NOT_EXIST(34003,"文件路径不存在"),
    ARCHIVES_NO_IS_NOT(34004,"报告编号为空"),
    ARCHIVES_NO_DOCTOR(34005,"医生不存在"),

    OFFACC_STUDENT_IS_NOT_EXIST(41001,"查无此人"),

    SCHOOL_SCHOOL_IS_NOT_EXIST(51001,"学校不存在"),
    SCHOOL_STUDENT_SCHOOL_ID_NULL(51002,"请输入学校ID"),
    SCHOOL_STUDENT_SCHOOL_NAME_NULL(51003,"学校名为空"),
    SCHOOL_STUDENT_BIRTHDAY_FORMAT_ERROR(52001,"学生生日格式错误"),
    SCHOOL_STUDENT_ID_NULL(52002,"请输入学生ID"),
    SCHOOL_STUDENT_ID_IS_NULL(52003,"学生ID为空"),
    SCHOOL_DEPT_ID_NULL(53001,"请输入部门ID"),
    SCHOOL_USER_ID_NULL(54001,"请输入用户ID"),
    SCHOOL_GRADE_NULL(55001,"请输入年级"),
    SCHOOL_CLAZZ_NULL(56001,"请输入班级"),
    SCHOOL_CLAZZ_AND_GRADE_NULL(56002,"年级和班级必须输入一个"),


    REVIEW_EYE_NULL(66001,"眼睛复测ID"),

    TASK_NAME_FOUL(71001,"任务名称不符合规范"),

    ID_NOT_FIND(81001,"学生id不存在"),


    ;
    private Integer code;
    private String message;
    ErrorEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
