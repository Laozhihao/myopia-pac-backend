package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.experimental.UtilityClass;

/**
 * 导出 Excel service 实例名
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@UtilityClass
public class ExportExcelServiceNameConstant {
    /**
     * 筛查机构
     */
    public final String SCREENING_ORGANIZATION_EXCEL_SERVICE = "screeningOrganizationExcelService";

    /**
     * 筛查人员
     */
    public final String SCREENING_ORGANIZATION_STAFF_EXCEL_SERVICE = "screeningOrganizationStaffExcelService";

    /**
     * 医院
     */
    public final String HOSPITAL_EXCEL_SERVICE = "hospitalExcelService";

    /**
     * 学校
     */
    public final String SCHOOL_EXCEL_SERVICE = "schoolExcelService";

    /**
     * 学生
     */
    public final String STUDENT_EXCEL_SERVICE = "studentExcelService";

    /**
     * 筛查学生
     */
    public final String PLAN_STUDENT_SERVICE = "planStudentExcelService";
}
