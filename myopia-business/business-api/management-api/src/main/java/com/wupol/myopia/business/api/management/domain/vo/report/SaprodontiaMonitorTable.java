package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 龋齿监测-表格数据
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class SaprodontiaMonitorTable {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;

    /**
     * 龋失补牙数
     */
    private Integer dmftNum;

    /**
     * 龋均
     */
    private String dmftRatio;

    /**
     * 有龋人数
     */
    private Integer saprodontiaNum;

    /**
     * 龋失人数
     */
    private Integer saprodontiaLossNum;

    /**
     * 龋补人数
     */
    private Integer saprodontiaRepairNum;

    /**
     * 龋患（失、补）人数
     */
    private Integer saprodontiaLossAndRepairNum;

    /**
     * 龋患（失、补）牙数
     */
    private Integer saprodontiaLossAndRepairTeethNum;

    /**
     * 龋患率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaRatio;
    /**
     * 龋失率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossRatio;

    /**
     * 龋补率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaRepairRatio;
    /**
     * 龋患（失、补）率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossAndRepairRatio;

    /**
     * 龋患（失、补）构成比
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saprodontiaLossAndRepairTeethRatio;

}