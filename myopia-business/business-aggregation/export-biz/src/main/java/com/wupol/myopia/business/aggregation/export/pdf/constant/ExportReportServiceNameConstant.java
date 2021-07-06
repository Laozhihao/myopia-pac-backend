package com.wupol.myopia.business.aggregation.export.pdf.constant;

import lombok.experimental.UtilityClass;

/**
 * 导出报告service实例名
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@UtilityClass
public class ExportReportServiceNameConstant {
    /**
     * 行政区域筛查报告
     */
    public String DISTRICT_SCREENING_REPORT_SERVICE = "districtScreeningReportService";
    /**
     * 学校筛查报告
     */
    public String SCHOOL_SCREENING_REPORT_SERVICE = "schoolScreeningReportService";
    /**
     * 筛查机构筛查报告
     */
    public String SCREENING_ORG_SCREENING_REPORT_SERVICE = "screeningOrgScreeningReportService";
    /**
     * 学校档案卡
     */
    public String SCHOOL_ARCHIVES_SERVICE = "schoolArchivesService";
    /**
     * 筛查机构档案卡
     */
    public String SCREENING_ORG_ARCHIVES_SERVICE = "screeningOrgArchivesService";
}
