package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.experimental.UtilityClass;

/**
 * 导出常量
 *
 * @author Simple4H
 */
@UtilityClass
public class ExcelFileNameConstant {

    /**
     * 筛查机构 Excel 文件名
     */
    public final String SCREENING_ORG_EXCEL_FILE_NAME = "%s筛查机构数据表";

    /**
     * 医院 Excel 文件名
     */
    public final String HOSPITAL_FILE_NAME = "医院-";

    /**
     * 学校 Excel 文件名
     */
    public final String SCHOOL_FILE_NAME = "学校-";

    /**
     * 筛查机构人员 Excel 文件名
     */
    public final String STAFF_FILE_NAME = "筛查机构人员-";

    /**
     * 学生 Excel 文件名
     */
    public final String STUDENT_FILE_NAME = "学生";

    /**
     * 筛查人员 Excel 文件名
     */
    public final String PLAN_STUDENT_FILE_NAME = "筛查学生数据表";

    /**
     * VS666设备数据 Excel 文件名
     */
    public final String VS_EQUIPMENT_FILE_NAME = "VS666筛查数据表";

    /**
     * 学生预警跟踪档案 Excel 文件名
     */
    public final String STUDENT_WARNING_ARCHIVE_EXCEL_FILE_NAME = "%s在%s至%s的学生预警跟踪数据表";
}
