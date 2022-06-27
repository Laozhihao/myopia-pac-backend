package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 龋齿监测-不同学龄
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class SaprodontiaSchoolAge implements SchoolAgeRatioVO {

    /**
     * 龋患率
     */
    private String saprodontiaRatio;
    /**
     * 龋失率
     */
    private String saprodontiaLossRatio;

    /**
     * 龋补率
     */
    private String saprodontiaRepairRatio;
    /**
     * 最高年级龋患率
     */
    private GradeRatio maxSaprodontiaRatio;

    /**
     * 最高年级龋失率
     */
    private GradeRatio maxSaprodontiaLossRatio;

    /**
     * 最高年级龋补率
     */
    private GradeRatio maxSaprodontiaRepairRatio;


    @Override
    public Integer type() {
        return 1;
    }
}