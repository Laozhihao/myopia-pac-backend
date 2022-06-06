package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BloodPressureAndSpinalCurvatureVO extends BloodPressureAndSpinalCurvatureRatioVO {
    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;

}