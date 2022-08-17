package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 血压与脊柱弯曲异常-不同学龄
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class BloodPressureAndSpinalCurvatureSchoolAge implements SchoolAgeRatioVO {

    /**
     * 血压偏高率
     */
    private String highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    private String abnormalSpineCurvatureRatio;
    /**
     * 最高年级血压偏高率
     */
    private GradeRatio maxHighBloodPressureRatio;

    /**
     * 最高年级脊柱弯曲异常率
     */
    private GradeRatio maxAbnormalSpineCurvatureRatio;

    @Override
    public Integer type() {
        return 3;
    }
}