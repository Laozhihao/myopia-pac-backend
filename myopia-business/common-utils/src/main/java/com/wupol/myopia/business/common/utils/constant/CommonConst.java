package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.util.List;

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
     * 筛查 未发布
     */
    public final Integer STATUS_NOT_RELEASE = 0;

    /**
     * 筛查 已发布
     */
    public final Integer STATUS_RELEASE = 1;

    /**
     * 筛查 作废
     */
    public final Integer STATUS_ABOLISH = 2;

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
    public final String SEND_SMS_WORD_ORDER_DISPOSE_NOTICE = "【青少年近视防控】您申请的工单已处理完毕，请在公众号查看工单反馈结果。";

    /**
     *  Excel格式
     */
    public static final String EXCEL_XLSX_FILE_SUFFIX = ".xlsx";

    /**
     *  中文逗号
     */
    public static final String CH_COMMA = "，";

    /**
     * 顿号
     */
    public static final String CN_PUNCTUATION_COMMA = "、";

    /**
     * 为百分之0
     */
    public static final String PERCENT_ZERO = "0.00%";

    /**
     * A01
     */
    public static final String A01 = "A01";

    /**
     * A011
     */
    public static final String A011 = "A011";

    /**
     * A02
     */
    public static final String A02 = "A02";

    /**
     * A03
     */
    public static final String A03 = "A03";

    /**
     * A04
     */
    public static final String A04 = "A04";

    /**
     * 需要插入脊柱个人信息的序号
     */
    public static List<String> getVisionSpineNotice(){
        return Lists.newArrayList(A01, A011, A02, A03, A04);
    }

    /**
     * 学校账号前缀
     */
    public static final String SCHOOL_USERNAME_PREFIX = "jsfkxd";


    /**
     * 数据完整性
     */
    public static final String DATA_INTEGRITY_FINISH = "数据完整";
    public static final String DATA_INTEGRITY_MISS = "数据缺失";

    /**
     * 零值
     */
    public static final Long ZERO_L = 0L;
    public static final Integer ZERO = 0;

    /**
     * 海南
     */
    public static final String HAI_NAN = "HaiNan";

    /** 问卷主标题文字说明*/
    public static final String QUESTIONNAIRE_MAIN_TITLE_NOTICE = "问卷须知";
    public static final String QUESTIONNAIRE_MAIN_TITLE_VISION = "视力调查问卷";
    public static final String QUESTIONNAIRE_MAIN_TITLE_HEALTHY = "健康调查问卷";

    public static final String SUCCESS = "【导出成功】数据报送数据表填写完成，点击下载或点击数据报送功能菜单查看/下载导出情况和下载数据表";

    public static final String ERROR = "【导出失败】尊敬的用户，非常抱歉通知您，因为系统问题导致视力筛查数据表填写失败，请前往数据报送功能菜单重新创建任务。如果多次出现该情况，请联系管理员，为您带来不便，我们深表歉意。";

    public static final String FILE_NAME = "%s-视力筛查数据报送表";

    /**
     *  Excel格式
     */
    public static final String EXCEL_XLS_FILE_SUFFIX = ".xls";

    /**
     * 学校筛查数据模板通知Key
     */
    public static final String SCHOOL_TEMPLATE_EXCEL_NOTICE_KEY = "【模板下载】%s-%s导入筛查学生数据表格模板已完成生成";

    /**
     * 学校筛查数据导入成功
     */
    public static final String SCHOOL_TEMPLATE_EXCEL_IMPORT_SUCCESS = "【筛查计划数据导入完成】%s-%s筛查计划数据导入完成，请核对查看";

    /**
     * 学校筛查数据导入失败
     */
    public static final String SCHOOL_TEMPLATE_EXCEL_IMPORT_ERROR = "【筛查计划数据导入异常】%s-%s筛查计划数据导入异常，请联系管理员";

    /**
     * 总体情况
     */
    public static final String TOTAL_DESC = "总体情况";

    /**
     * 通知关联
     */
    public static final String NOTICE_LINK_UNIQUE = "notice-link-%s-%s-%s-%s";
}