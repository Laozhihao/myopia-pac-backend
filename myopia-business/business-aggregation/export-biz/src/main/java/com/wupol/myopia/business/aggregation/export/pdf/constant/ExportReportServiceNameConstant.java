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
    public final String DISTRICT_SCREENING_REPORT_SERVICE = "districtScreeningReportService";
    /**
     * 学校筛查报告
     */
    public final String SCHOOL_SCREENING_REPORT_SERVICE = "schoolScreeningReportService";
    /**
     * 筛查机构筛查报告
     */
    public final String SCREENING_ORG_SCREENING_REPORT_SERVICE = "screeningOrgScreeningReportService";
    /**
     * 学校档案卡
     */
    public final String SCHOOL_ARCHIVES_SERVICE = "schoolArchivesService";
    /**
     * 筛查机构档案卡
     */
    public final String SCREENING_ORG_ARCHIVES_SERVICE = "screeningOrgArchivesService";

    /**
     * 批量当个学生档案卡
     */
    public final String STUDENT_ARCHIVES_SERVICE = "studentArchivesService";

    /**
     * 导出筛检计划下的对应数据
     *  作者：钓猫的小鱼
     */
    public final String SCREENING_PLAN = "exportScreeningPlanReportService";

    /**
     * 导出筛检计划下的对应数据
     *  作者：钓猫的小鱼
     */
    public final String EXPOR_TPLAN_STUDENT_DATA_EXCEL_SERVICE = "exportPlanStudentDataExcelService";

    /**
     * 导出筛检计划下学校学生的筛查数据
     *  作者：钓猫的小鱼
     */
    public final String EXPORT_PLAN_SCHOOL_STUDENT_DATA = "exportPlanSchoolStudentDataExcelService";


}
