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
    public String DISTRICT_REPORT_HTML_URL = "%s?notificationId=%d&districtId=%d";
    /**
     * 学校报告HTML页面地址，带筛查通知ID参数
     */
    public String SCHOOL_REPORT_HTML_URL_WITH_NOTICE_ID = "%s?notificationId=%d&schoolId=%d";

    /**
     * 学校报告HTML页面地址，带筛查计划ID参数
     */
    public String SCHOOL_REPORT_HTML_URL_WITH_PLAN_ID = "%s?planId=%d&schoolId=%d";

    /**
     * 学校档案卡HTML页面地址
     */
    public String SCHOOL_ARCHIVES_HTML_URL = "%s?planId=%d&schoolId=%d&cardStatus=true";

    /**
     * 筛查计划总报告HTML页面地址
     */
    public String REPORT_HTML_URL_WITH_PLAN_ID = "%s?planId=%d";
}
