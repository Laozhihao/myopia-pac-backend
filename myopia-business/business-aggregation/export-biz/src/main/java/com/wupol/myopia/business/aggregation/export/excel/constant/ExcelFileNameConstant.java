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
    public String SCREENING_ORG_EXCEL_FILE_NAME = "%s筛查机构数据表";

    /**
     * 医院 Excel 文件名
     */
    public String HOSPITAL_EXCEL_FILE_NAME = "%s医院数据表";

    /**
     * 学校 Excel 文件名
     */
    public String SCHOOL_EXCEL_FILE_NAME = "%s学校数据表";

    /**
     * 筛查人员 Excel 文件名
     */
    public String STAFF_EXCEL_FILE_NAME = "%s筛查机构人员数据表";

    /**
     * 学生 Excel 文件名
     */
    public String STUDENT_EXCEL_FILE_NAME = "%s%s%s学生数据表";

    /**
     * 筛查机构 Excel 导出通知的关键内容
     */
    public String SCREENING_ORG_NOTICE_KEY_CONTENT = "%s筛查机构数据表";

    /**
     * 医院名称
     */
    public String HOSPITAL_NAME = "医院-";

    /**
     * 学校名称
     */
    public String SCHOOL_NAME = "学校-";

    /**
     * 筛查机构人员名称
     */
    public String STAFF_NAME = "筛查机构人员-";

    /**
     * 学生名称
     */
    public String STUDENT_NAME = "学生";
}
