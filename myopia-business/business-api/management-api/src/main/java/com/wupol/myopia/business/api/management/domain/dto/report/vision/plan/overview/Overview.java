package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.SchoolAgeCount;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 概述
 *
 * @author Simple4H
 */
@Data
public class Overview {

    /**
     * 标题
     */
    private String title;

    /**
     * 报告生成时间
     */
    private Date reportCreateTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 学校统计
     */
    private Long schoolCount;

    /**
     * 筛查类型
     */
    private String screeningType;

    /**
     * 详情
     */
    private List<SchoolAgeCount> items;


    /**
     * 计划筛查人数
     */
    private Long planScreeningCount;

    /**
     * 未筛查人数
     */
    private Long unScreeningCount;

    /**
     * 无效筛查人数
     */
    private Long invalidScreeningCount;

    /**
     * 有效筛查人数
     */
    private Long validScreeningCount;

    /**
     * 本次筛查对象分布
     */
    private List<Table1> table1;

    /**
     * 幼儿园儿童视力筛查情况
     */
    private Table2 table2;

    /**
     * 小学及以上教育阶段儿童青少年视力筛查情况
     */
    private Table3 table3;

    /**
     * 小学及以上各教育阶段视力情况
     */
    private Table4 table4;

    /**
     * 各教育阶段视力监测预警
     */
    private Table5 table5;
}
