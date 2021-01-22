package com.wupol.myopia.business.management.constant;

/**
 * 常量类
 *
 * @author Simple4H
 */
public class Const {

    public static final Integer STATUS_IS_DELETED = 2;

    public static final Integer STATUS_NOT_DELETED = 0;

    public static final String LOCK_ORG_REDIS = "org:lock.org:string";

    public static final String LOCK_ORG_STAFF_REDIS = "org:lock.org.staff:string";

    public static final String LOCK_SCHOOL_REDIS = "school:lock.school:string";

    public static final String LOCK_STUDENT_REDIS = "student:lock.student:string";

    public static final String LOCK_HOSPITAL_REDIS = "hospital:lock.student:String";

    public static final Integer SCREENING_TIME = 0;

    public static final String DISTRICT_CN_NAME = "district:cn.name:integer";

    public static final Integer READ_NOTICE = 1;

    public static final Integer DELETED_NOTICE = 2;

    public static final Integer STUDENT_ARCHIVES = 1;

    public static final Integer SCREENING_REPORT = 2;
}