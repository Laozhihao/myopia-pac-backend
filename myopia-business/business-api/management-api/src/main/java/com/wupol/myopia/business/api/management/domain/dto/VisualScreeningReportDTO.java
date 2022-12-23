package com.wupol.myopia.business.api.management.domain.dto;

/**
 * 视力筛查报告
 * @Author wulizhou
 * @Date 2022/12/23 18:01
 */
public class VisualScreeningReportDTO {

    /**
     * 概述
     */
    private ScreeningSummaryDTO summary;

    /**
     * 学生近视情况
     */
    private Object studentMyopia;

    /**
     * 学生视力情况
     */
    private Object studentVisualAcuity;

}
