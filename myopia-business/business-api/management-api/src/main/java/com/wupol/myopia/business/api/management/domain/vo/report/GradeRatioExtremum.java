package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeRatioExtremum {
    /**
     * 最高占比班级
     */
    private String maxClassName;
    /**
     * 最低占比班级
     */
    private String minClassName;
    /**
     * 最高占比
     */
    private BigDecimal maxRatio;
    /**
     * 最低占比
     */
    private BigDecimal minRatio;
}