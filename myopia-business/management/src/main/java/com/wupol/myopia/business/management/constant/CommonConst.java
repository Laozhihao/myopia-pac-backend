package com.wupol.myopia.business.management.constant;

/**
 * 常量类
 *
 * @author Simple4H
 */
public interface CommonConst {

    /**
     * 状态-删除
     */
    Integer STATUS_IS_DELETED = 2;

    /**
     * 状态-启用
     */
    Integer STATUS_NOT_DELETED = 0;

    /**
     * 筛查次数
     */
    Integer SCREENING_TIME = 0;

    /**
     * 通知中心 已读状态
     */
    Integer STATUS_NOTICE_READ = 1;

    /**
     * 通知中心 删除状态
     */
    Integer STATUS_NOTICE_DELETED = 2;

    /**
     * 系统中心 类型 档案卡模版
     */
    Integer TYPE_TEMPLATE_STUDENT_ARCHIVES = 1;

    /**
     * 系统中心 类型 筛查报告模版
     */
    Integer TYPE_TEMPLATE_SCREENING_REPORT = 2;
}