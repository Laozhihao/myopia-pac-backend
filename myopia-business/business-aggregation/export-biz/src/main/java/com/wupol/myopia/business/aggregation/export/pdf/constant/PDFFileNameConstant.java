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
     * PDF 计划总报告文件名
     */
    public static final String PLAN_REPORT_PDF_FILE_NAME = "%s年学生近视筛查结果报告";

    /**
     * PDF档案卡文件名，如：希望小学一年级1班的学生档案卡、希望中学初一的学生监测表
     */
    public static final String ARCHIVES_PDF_FILE_NAME = "%s的学生%s";
    /**
     * PDF档案卡文件名，如：xxx（学校）xxx（年级）xxx（班级）的学生档案卡.pdf
     */
    public static final String CLASS_ARCHIVES_PDF_FILE_NAME = "%s%s%s的学生%s.pdf";

    /**
     * 档案卡
     */
    public static final String VISION_ARCHIVE = "档案卡";
    /**
     * 监测表
     */
    public static final String COMMON_DISEASE_ARCHIVE = "监测表";

    /**
     * 眼健康数据
     */
    public static final String SCHOOL_EYE_HEALTH = "眼健康中心数据表";

}
