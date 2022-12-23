package com.wupol.myopia.business.api.management.domain.dto;

import java.util.Date;

/**
 * 学校筛查概述
 * @Author wulizhou
 * @Date 2022/12/23 18:07
 */
public class ScreeningSummaryDTO {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校所在地区，省市区
     */
    private String schoolDistrict;

    /**
     * 报告生成时间
     */
    private Date reportTime;

    /**
     * 筛查计划--开始时间
     */
    private Date startTime;

    /**
     * 筛查计划--结束时间
     */
    private Date endTime;

}
