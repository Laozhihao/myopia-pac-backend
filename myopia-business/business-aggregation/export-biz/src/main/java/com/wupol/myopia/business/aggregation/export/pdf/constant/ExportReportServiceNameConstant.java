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
     * 行政区域档案卡
     */
    public final String EXPORT_DISTRICT_ARCHIVES_SERVICE = "exportDistrictArchivesService";

    /**
     * 学校或年级档案卡
     */
    public final String EXPORT_SCHOOL_OR_GRADE_ARCHIVES_SERVICE = "exportSchoolOrGradeArchivesService";

    /**
     * 班级或若干个学生档案卡
     */
    public final String EXPORT_CLASS_OR_STUDENT_ARCHIVES_SERVICE = "exportClassOrStudentArchivesService";


    /**
     * 导出筛检计划下的对应数据
     *  作者：钓猫的小鱼
     */
    public final String SCREENING_PLAN = "exportScreeningPlanReportService";

    /**
     * 导出筛检计划下的对应数据
     *  作者：钓猫的小鱼
     */
    public final String EXPORT_PLAN_STUDENT_DATA_EXCEL_SERVICE = "exportPlanStudentDataExcelService";

    /**
     * 导出筛检计划下学校学生的筛查数据
     *  作者：钓猫的小鱼
     */
    public final String EXPORT_VISION_SCREENING_RESULT_EXCEL_SERVICE = "exportVisionScreeningResultExcelService";



    /**
     * 导出筛检计划下学校学生的筛查数据
     *  作者：钓猫的小鱼
     */
    public final String EXPORT_QRCODE_SCREENING_SERVICE = "screeningQrCodeService";


}
