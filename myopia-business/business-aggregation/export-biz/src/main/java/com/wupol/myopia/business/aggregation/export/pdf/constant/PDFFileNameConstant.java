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
     * PDF告知书
     */
    public static final String REPORT_NOTICE_QR_CODE_FILE_NAME = "%s%s%s告知书";
    public static final String REPORT_SCREENING_QR_CODE_FILE_NAME = "%s%s%s筛查二维码";
    public static final String REPORT_VS666_QR_CODE_FILE_NAME = "%s%s%sVS666专属筛查二维码";
    public static final String REPORT_FICTITIOUS_QR_CODE_FILE_NAME = "%s%s%s虚拟二维码";

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
    public static final String ARCHIVES_PDF_FILE_NAME_GRADE_CLASS = "%s的%s%s的档案卡.pdf";

    /**
     * PDF 计划总报告文件名
     */
    public static final String PLAN_REPORT_PDF_FILE_NAME = "%s年学生近视筛查结果报告";


    /**
     * 全校
     */
    public static final String ALL_SCHOOL_PDF_FILE_NAME = "%s的全校档案卡";

    /**
     * 制定班级
     */
    public static final String GRADE_CLASS_PDF_FILE_NAME = "%s的%s%s的档案卡";

    /**
     * PDF档案卡批量学生导出文件名
     */
    public static final String ARCHIVES_PDF_FILE_NAME_STUDENT = "%s的档案卡.pdf";

}
