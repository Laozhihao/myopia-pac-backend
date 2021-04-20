package com.wupol.myopia.business.management.constant;

/**
 * 家长端-学生筛查报告常量
 *
 * @author Simple4H
 */
public interface ParentReportConst {

    /**
     * 裸眼正常
     */
    Integer NAKED_NORMAL = 0;

    /**
     * 裸眼底下
     */
    Integer NAKED_LOW = 1;

    /**
     * 矫正-正常
     */
    Integer CORRECTED_NORMAL = 2;

    /**
     * 矫正-未矫
     */
    Integer CORRECTED_NOT = 3;

    /**
     * 矫正-欠矫
     */
    Integer CORRECTED_OWE = 4;

    /**
     * 标签-正常
     */
    Integer LABEL_NORMAL = 5;

    /**
     * 标签-轻度
     */
    Integer LABEL_MILD = 6;

    /**
     * 标签-中度
     */

    Integer LABEL_MODERATE = 7;

    /**
     * 标签-重度
     */
    Integer LABEL_SEVERE = 8;
}
