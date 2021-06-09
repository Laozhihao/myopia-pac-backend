package com.wupol.myopia.business.common.utils.constant;

import lombok.experimental.UtilityClass;

/**
 * 常量类
 *
 * @author Simple4H
 */
@UtilityClass
public class CommonConst {

    /**
     * 状态-删除
     */
    public Integer STATUS_IS_DELETED = 2;

    /**
     * 状态-启用
     */
    public Integer STATUS_NOT_DELETED = 0;

    /**
     * 状态-禁用
     */
    public Integer STATUS_BAN = 1;

    /**
     * 通知中心 未读状态
     */
    public Integer STATUS_NOTICE_UNREAD = 0;

    /**
     * 通知中心 已读状态
     */
    public Integer STATUS_NOTICE_READ = 1;

    /**
     * 通知中心 删除状态
     */
    public Integer STATUS_NOTICE_DELETED = 2;

    /**
     * 通知中心 已读已创建状态
     */
    public Integer STATUS_NOTICE_CREATED = 3;

    /**
     * 系统中心 类型 档案卡模版
     */
    public Integer TYPE_TEMPLATE_STUDENT_ARCHIVES = 1;

    /**
     * 系统中心 类型 筛查报告模版
     */
    public Integer TYPE_TEMPLATE_SCREENING_REPORT = 2;

    /**
     * 筛查 未发布
     */
    public Integer STATUS_NOT_RELEASE = 0;

    /**
     * 筛查 已发布
     */
    public Integer STATUS_RELEASE = 1;

    /**
     * 筛查表默认ID
     */
    public Integer DEFAULT_ID = 0;

    /**
     * 通知-站内信
     */
    public Byte NOTICE_STATION_LETTER = 0;

    /**
     * 通知-筛查通知
     */
    public Byte NOTICE_SCREENING_NOTICE = 1;

    /**
     * 通知-筛查任务
     */
    public Byte NOTICE_SCREENING_DUTY = 2;

    /**
     * 通知-筛查计划
     */
    public Byte NOTICE_SCREENING_PLAN = 3;

    /**
     * 导出消息通知内容-成功
     */
    public String EXPORT_MESSAGE_CONTENT_SUCCESS = "【导出成功】%s，点击下载，申请时间：%tF%n";

    /**
     * 导出消息通知内容-失败
     */
    public String EXPORT_MESSAGE_CONTENT_FAILURE = "【导出失败】%s，请稍后重试";

    /**
     * 左眼
     */
    public Integer LEFT_EYE = 0;

    /**
     * 右眼
     */
    public Integer RIGHT_EYE = 1;

    /**
     * 相同眼球
     */
    public Integer SAME_EYE = 2;

    /**
     * 是
     */
    public Integer IS_TOTAL = 1;

    /**
     * 否
     */
    public Integer NOT_TOTAL = 0;

    /**
     * 导出消息通知内容-失败
     */
    public String SEND_SMS_TO_PARENT_MESSAGE = "【近视防控】%s近视筛查结果：左眼%s，右眼%s，%s";
}