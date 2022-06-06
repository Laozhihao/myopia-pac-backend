package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BloodPressureAndSpinalCurvatureRatioVO {

    /**
     * 血压偏高率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal abnormalSpineCurvatureRatio;

}
