package com.wupol.myopia.business.aggregation.export.pdf.constant;

import lombok.experimental.UtilityClass;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@UtilityClass
public class HtmlPageUrlConstant {
    /**
     * 行政区域报告HTML页面地址
     */
    public static final String DISTRICT_REPORT_HTML_URL = "%s?notificationId=%d&districtId=%d";
    /**
     * 学校报告HTML页面地址，带筛查通知ID参数
     */
    public static final String SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID = "%s?notificationId=%d&schoolId=%d";

    /**
     * 学校报告HTML页面地址，带筛查计划ID参数
     */
    public static final String SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID = "%s?planId=%d&schoolId=%d";

    /**
     * 学校档案卡HTML页面地址
     */
    public static final String SCHOOL_ARCHIVES_HTML_URL = "%s?planId=%d&schoolId=%d&templateId=%d&gradeId=%d&classId=%d&planStudentIds=%s";

    /**
     * 筛查计划总报告HTML页面地址
     */
    public static final String REPORT_HTML_URL_WITH_PLAN_ID = "%s?planId=%d";

    /**
     * 学校档案卡HTML页面地址
     */
    public static final String STUDENT_ARCHIVES_HTML_URL = "%s?planId=%d&schoolId=%d&templateId=%d&planStudentIds=%s&gradeId=%s&classId=%s";

    /**
     * 学生二维码HTML页面地址
     */
    public static final String STUDENT_QRCODE_HTML_URL = "%s?screeningPlanId=%s&schoolId=%s&gradeId=%s&classId=%s&planStudentIds=%s&type=%d";
    /**
     * 学生档案卡
     */
    public static final String STUDENT_ARCHIVES = "%s?resultId=%d&templateId=%d";

    public static final String REPORT_AREA_VISION = "%s?reportType=visonArea&noticeId=%d&districtId=%d";

    public static final String REPORT_PRIMARY_VISION = "%s?reportType=visonArea&planId=%d&schoolId=%d&noticeId=%d";
    public static final String REPORT_KINDERGARTEN_VISION = "%s?reportType=visonArea&planId=%d&schoolId=%d&noticeId=%d";;




}
