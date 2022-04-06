package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * 血压
 */
@Data
public class BloodPressureDataDO {
    // 舒张压
    private Float dbp;

    // 收缩压
    private Float sbp;
}
