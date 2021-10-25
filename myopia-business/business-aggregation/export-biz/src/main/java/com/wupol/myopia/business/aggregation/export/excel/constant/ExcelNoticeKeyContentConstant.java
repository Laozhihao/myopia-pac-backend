package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.experimental.UtilityClass;

/**
 * 导出Excel消息关键内容常量
 *
 * @author Simple4H
 */
@UtilityClass
public class ExcelNoticeKeyContentConstant {

    /**
     * 医院 Excel 导出通知消息的关键内容
     */
    public final String HOSPITAL_EXCEL_NOTICE_KEY_CONTENT = "%s医院数据表";

    /**
     * 学校 Excel 导出通知消息的关键内容
     */
    public final String SCHOOL_EXCEL_NOTICE_KEY_CONTENT = "%s学校数据表";

    /**
     * 筛查人员 Excel 导出通知消息的关键内容
     */
    public final String STAFF_EXCEL_NOTICE_KEY_CONTENT = "%s筛查机构人员数据表";

    /**
     * 学生 Excel 导出通知消息的关键内容
     */
    public final String STUDENT_EXCEL_NOTICE_KEY_CONTENT = "%s%s%s学生数据表";

    /**
     * 筛查机构 Excel 导出通知的关键内容
     */
    public final String SCREENING_ORG_NOTICE_KEY_CONTENT = "%s筛查机构数据表";

    /**
     * 筛查数据 Excel 导出通知消息的关键内容
     */
    public final String PLAN_STUDENT_EXCEL_NOTICE_KEY_CONTENT = "%s在%s至%s的%s%s筛查学生数据表";

    /**
     * 学生预警跟踪档案 Excel 导出通知消息的关键内容
     */
    public final String STUDENT_WARNING_ARCHIVE_EXCEL_NOTICE_KEY_CONTENT = "%s在%s至%s的学生预警跟踪数据表";

}
