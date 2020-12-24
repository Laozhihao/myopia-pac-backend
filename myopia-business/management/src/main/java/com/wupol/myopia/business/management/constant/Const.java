package com.wupol.myopia.business.management.constant;

/**
 * 常量类
 *
 * @author Simple4H
 */
public class Const {

    public static final Integer STATUS_IS_DELETED = 2;

    public static final Integer CREATE_USER_ID = 1;

    public static final Integer GOV_DEPT_ID = 1;

    public static final Integer STAFF_USER_ID = 1;

    public interface MANAGEMENT_TYPE {
        // 学校（含幼儿园等）ID
        Integer SCHOOL = 1;
        // 学生ID
        Integer STUDENT = 2;
        // 医院ID
        Integer HOSPITAL = 3;
        // 筛查机构ID
        Integer SCREENING_ORGANIZATION = 4;
        // 筛查人员ID
        Integer SCREENING_ORGANIZATION_STAFF = 5;
    }
}
