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
    public final Integer STATUS_IS_DELETED = 2;

    /**
     * 状态-启用
     */
    public final Integer STATUS_NOT_DELETED = 0;

    /**
     * 状态-禁用
     */
    public final Integer STATUS_BAN = 1;

    /**
     * 通知中心 未读状态
     */
    public final Integer STATUS_NOTICE_UNREAD = 0;

    /**
     * 通知中心 已读状态
     */
    public final Integer STATUS_NOTICE_READ = 1;

    /**
     * 通知中心 删除状态
     */
    public final Integer STATUS_NOTICE_DELETED = 2;

    /**
     * 通知中心 已读已创建状态
     */
    public final Integer STATUS_NOTICE_CREATED = 3;

    /**
     * 系统中心 类型 档案卡模版
     */
    public final Integer TYPE_TEMPLATE_STUDENT_ARCHIVES = 1;

    /**
     * 系统中心 类型 筛查报告模版
     */
    public final Integer TYPE_TEMPLATE_SCREENING_REPORT = 2;

    /**
     * 筛查 未发布
     */
    public final Integer STATUS_NOT_RELEASE = 0;

    /**
     * 筛查 已发布
     */
    public final Integer STATUS_RELEASE = 1;

    /**
     * 筛查表默认ID
     */
    public final Integer DEFAULT_ID = 0;

    /**
     * 通知-站内信
     */
    public final Byte NOTICE_STATION_LETTER = 0;

    /**
     * 通知-筛查通知
     */
    public final Byte NOTICE_SCREENING_NOTICE = 1;

    /**
     * 通知-筛查任务
     */
    public final Byte NOTICE_SCREENING_DUTY = 2;

    /**
     * 通知-筛查计划
     */
    public final Byte NOTICE_SCREENING_PLAN = 3;

    /**
     * 导出消息通知内容-成功
     */
    public final String EXPORT_MESSAGE_CONTENT_SUCCESS = "【导出成功】%s，点击下载，申请时间：%tF%n";

    /**
     * 导出消息通知内容-失败
     */
    public final String EXPORT_MESSAGE_CONTENT_FAILURE = "【导出失败】%s，请稍后重试";

    /**
     * 合作即将到期通知
     */
    public final String COOPERATION_WARN_NOTICE = "%s将于%s合作终止，请注意！";

    /**
     * 左眼
     */
    public final Integer LEFT_EYE = 0;

    /**
     * 右眼
     */
    public final Integer RIGHT_EYE = 1;

    /**
     * 相同眼球
     */
    public final Integer SAME_EYE = 2;

    /**
     * 是
     */
    public final Integer IS_TOTAL = 1;

    /**
     * 否
     */
    public final Integer NOT_TOTAL = 0;

    /**
     * 导出消息通知内容-失败
     */
    public final String SEND_SMS_TO_PARENT_MESSAGE = "【青少年近视防控】%s近视筛查结果：左眼%s，右眼%s，%s";


    /**
     * 导出告知书
     */
    public final Integer EXPORT_NOTICE = 0;
    /**
     * 导出二维码
     */
    public final Integer EXPORT_QRCODE = 1;

    /**
     * 导出VS666数据
     */
    public final Integer EXPORT_VS666 = 2;

    /**
     * 导出虚拟二维码
     */
    public final Integer EXPORT_SCREENING_QRCODE = 3;

    /**
     * 合作医院 1-合作医院
     */
    public final Integer IS_COOPERATION = 1;

    /**
     * 合作医院 0-非合作医院
     */
    public final Integer NOT_COOPERATION = 0;

    /**
     * 短信通知-孩子年龄到了后会短信或公众号提醒家长做保健
     */
    public final String SEND_SMS_PRESCHOOL_NOTICE = "【青少年近视防控】%s家长您好，请及时到保健院为孩子做%s眼保健检查。";

    /**
     * 短信通知-工单处理完成
     */
    public final String SEND_SMS_WORD_ORDER_DISPOSE_NOTICE = "【儿童青少年近视防控】您申请的工单已处理完毕，请在公众号查看工单反馈结果。";

    /**
     *  Excel格式
     */
    public static final String FILE_SUFFIX = ".xlsx";
}