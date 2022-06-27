package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 血压与脊柱弯曲异常监测结果-说明变量
 *
 * @author hang.yuan
 * @date 2022/6/6
 */

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