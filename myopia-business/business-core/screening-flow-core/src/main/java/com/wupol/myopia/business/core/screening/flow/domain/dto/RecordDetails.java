package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RecordDetails {

    /**
     * 是否拥有复测报告
     */
    private boolean hasRescreenReport;

    private Integer schoolId;

    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;

    /**
     * 筛查计划标题
     */
    private String planTitle;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 计划的学生数量
     */
    private Integer planScreeningNumbers;

    /**
     * 实际筛查的学生数量
     */
    private Integer realScreeningNumbers;

    /**
     * 计划开始时间
     */
    private Date startTime;

    /**
     * 计划结束时间
     */
    private Date endTime;

    /**
     * 机构质控员名字
     */
    private String qualityControllerName;

    /**
     * 机构质控员队长
     */
    private String qualityControllerCommander;


}
