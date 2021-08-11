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
    public static final String ARCHIVES_PDF_FILE_NAME = "%s%s%s档案卡";

    /**
     * PDF 计划总报告文件名
     */
    public static final String PLAN_REPORT_PDF_FILE_NAME = "%s年学生近视筛查结果报告";

    /**
     * 文件保存路径
     */
    public static final String FILE_SAVE_DIR = "%s/%s/%s";

}
