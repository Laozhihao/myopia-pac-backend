package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 报告基本通用信息
 * @Author wulizhou
 * @Date 2023/1/5 10:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ReportBaseSummaryDTO {

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
     * 视力不良/低常人数
     */
    private Integer lowVisionNum;

    /**
     * 视力不良/低常率
     */
    private Float lowVisionRatio;

    /**
     * 预警总人数
     */
    private Integer warningNum;

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
