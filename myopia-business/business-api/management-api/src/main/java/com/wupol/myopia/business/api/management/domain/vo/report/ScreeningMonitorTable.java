package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 筛查情况（学校/班级）-表格数据
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class ScreeningMonitorTable {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;


    /**
     * 有龋人数
     */
    private Integer saprodontiaNum;

    /**
     * 龋患（失、补）人数
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 超重数
     */
    private Integer overweightNum;
    /**
     * 肥胖数
     */
    private Integer obeseNum;
    /**
     * 营养不良数
     */
    private Integer malnourishedNum;
    /**
     * 生长迟缓数据
     */
    private Integer stuntingNum;

    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;


    //=========== 不带% =============
    /**
     * 龋患率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaRatio;

    /**
     * 龋患（失、补）率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossAndRepairRatio;
    /**
     * 超重率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal overweightRatio;
    /**
     * 肥胖率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal obeseRatio;

    /**
     * 营养不良率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal malnourishedRatio;

    /**
     * 生长迟缓率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal stuntingRatio;

    /**
     * 血压偏高率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal abnormalSpineCurvatureRatio;

}