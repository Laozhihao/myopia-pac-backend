package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 身高体重监测-表格数据
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class HeightAndWeightMonitorTable {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;

    /**
     * 超重人数
     */
    private Integer overweightNum;

    /**
     * 肥胖人数
     */
    private Integer obeseNum;

    /**
     * 营养不良人数
     */
    private Integer malnourishedNum;
    /**
     * 生长迟缓人数
     */
    private Integer stuntingNum;

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

}