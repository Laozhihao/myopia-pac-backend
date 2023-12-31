package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 概述
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Outline {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 地址
     */
    private String address;

    /**
     * 开始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 年级统计
     */
    private Long gradeTotal;

    /**
     * 班级统计
     */
    private Long classTotal;

    /**
     * 合计人数
     */
    private Long studentTotal;

    /**
     * 本次未筛
     */
    private Long unScreening;

    /**
     * 无效人数
     */
    private Long invalidTotal;

    /**
     * 有效人数
     */
    private Long validTotal;

    /**
     * 男-有效人数
     */
    private Long mValidTotal;

    /**
     * 女-有效人数
     */
    private Long fValidTotal;
}
