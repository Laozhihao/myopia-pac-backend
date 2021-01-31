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
     * 通知中心 未读状态
     */
    Integer STATUS_NOTICE_UNREAD = 0;

    /**
     * 通知中心 已读状态
     */
    Integer STATUS_NOTICE_READ = 1;

    /**
     * 通知中心 删除状态
     */
    Integer STATUS_NOTICE_DELETED = 2;

    /**
     * 通知中心 已读已创建状态
     */
    Integer STATUS_NOTICE_CREATED = 3;

    /**
     * 系统中心 类型 档案卡模版
     */
    Integer TYPE_TEMPLATE_STUDENT_ARCHIVES = 1;

    /**
     * 系统中心 类型 筛查报告模版
     */
    Integer TYPE_TEMPLATE_SCREENING_REPORT = 2;

    /**
     * 筛查 未发布
     */
    Integer STATUS_NOT_RELEASE = 0;

    /**
     * 筛查 已发布
     */
    Integer STATUS_RELEASE = 1;

    /**
     * 筛查表默认ID
     */
    Integer DEFAULT_ID = 0;

    /**
     * 通知-站内信
     */
    Byte NOTICE_STATION_LETTER = 0;

    /**
     * 通知-筛查通知
     */
    Byte NOTICE_SCREENING_NOTICE = 1;
}