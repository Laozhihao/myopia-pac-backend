package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学校占比极值对比
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class SchoolRatioExtremum {
    /**
     * 最高占比学校
     */
    private String maxSchoolName;

    /**
     * 最低占比学校
     */
    private String minSchoolName;
    /**
     * 最高占比
     */
    private BigDecimal maxRatio;
    /**
     * 最低占比
     */
    private BigDecimal minRatio;
}