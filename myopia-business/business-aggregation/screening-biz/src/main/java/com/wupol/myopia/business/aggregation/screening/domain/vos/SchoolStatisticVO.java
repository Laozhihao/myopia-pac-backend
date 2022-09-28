package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学校统计信息
 *
 * @author hang.yuan 2022/9/20 10:52
 */
@Data
@Accessors(chain = true)
public class SchoolStatisticVO implements Serializable {
    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 计划筛查人数
     */
    private Integer planScreeningNum;
    /**
     * 实际筛查人数
     */
    private Integer realScreeningNum;
}
