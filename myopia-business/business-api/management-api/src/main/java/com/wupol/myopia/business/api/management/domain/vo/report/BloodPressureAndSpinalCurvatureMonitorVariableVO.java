package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

@Data
public class BloodPressureAndSpinalCurvatureMonitorVariableVO {
    /**
     * 血压偏高率
     */
    private String highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    private String abnormalSpineCurvatureRatio;
}