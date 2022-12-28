package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 学校筛查概述
 *
 * @Author wulizhou
 * @Date 2022/12/23 18:07
 */
@Data
@Builder
@Accessors(chain = true)
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

    /**
     * 年级数量
     */
    private Integer gradeNum;

    /**
     * 班级数据
     */
    private Integer classNum;

    /**
     * 计划筛查人数
     */
    private Integer planScreeningNum;

    /**
     * 未筛查人数
     */
    private Integer unscreenedNum;

    /**
     * 无效筛查人数
     */
    private Integer invalidScreeningNum;

    /**
     * 有效筛查人数
     */
    private Integer validScreeningNum;

    /**
     * 有效筛查人数（男）
     */
    private Integer maleValidScreeningNum;

    /**
     * 有效筛查人数（女）
     */
    private Integer femaleValidScreeningNum;

    /**
     * 平均视力
     */
    private Float averageVision;

    /**
     * 视力不良人数
     */
    private Long lowVisionNum;

    /**
     * 视力不良率
     */
    private Float lowVisionRatio;

    /**
     * 近视人数
     */
    private Long myopiaNum;

    /**
     * 近视率
     */
    private Float myopiaRatio;

    /**
     * 未矫率
     */
    private Float uncorrectedRatio;

    /**
     * 欠矫率
     */
    private Float underCorrectedRatio;

    /**
     * 低度近视率
     */
    private Float lightMyopiaRatio;

    /**
     * 高度近视率
     */
    private Float highMyopiaRatio;

    /**
     * 预警总人数
     */
    private Long warningNum;

    /**
     * 预警人数百分比
     */
    private Float warningRatio;

    /**
     * 0级预警人数
     */
    private Long warningLevelZeroNum;

    /**
     * 1级预警人数
     */
    private Long warningLevelOneNum;

    /**
     * 2级预警人数
     */
    private Long warningLevelTwoNum;

    /**
     * 3级预警人数
     */
    private Long warningLevelThreeNum;

}
