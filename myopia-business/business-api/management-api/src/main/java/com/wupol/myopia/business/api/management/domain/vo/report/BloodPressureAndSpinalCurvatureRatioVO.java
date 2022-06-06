package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 血压与脊柱弯曲异常占比
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
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
