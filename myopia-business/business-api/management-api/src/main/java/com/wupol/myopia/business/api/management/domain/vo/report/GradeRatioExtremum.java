package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 班级占比极值对比
 * @author hang.yuan
 * @date 2022/6/6
 */
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