package com.wupol.myopia.business.aggregation.export.pdf.constant;

import lombok.experimental.UtilityClass;

/**
 * 文件名常量
 *
 * @Author HaoHao
 * @Date 2021/3/26
 **/
@UtilityClass
public class PDFFileNameConstant {
    /**
     * PDF报告文件名
     */
    public static final String REPORT_PDF_FILE_NAME = "%s筛查报告";
    /**
     * PDF档案卡文件名
     */
    public static final String ARCHIVES_PDF_FILE_NAME = "%s档案卡";

    /**
     * 全校
     */
    public static final String ARCHIVES_PDF_FILE_ALL_SCHOOL = "%s的全校档案卡";

    /**
     * PDF档案卡文件名(年级班级)
     */
    public static final String ARCHIVES_PDF_FILE_NAME_GRADE_CLASS = "%s的%s%s的档案卡";

    /**
     * PDF 计划总报告文件名
     */
    public static final String PLAN_REPORT_PDF_FILE_NAME = "%s年学生近视筛查结果报告";

}
