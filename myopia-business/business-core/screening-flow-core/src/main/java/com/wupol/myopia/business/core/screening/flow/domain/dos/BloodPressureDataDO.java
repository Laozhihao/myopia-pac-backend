package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * @Description 血压
 * @Date 2021/4/06 16:50
 * @Author xz
 */
@Data
public class BloodPressureDataDO {
    /**
     * 舒张压
     */
    private Float dbp;

    /**
     * 收缩压
     */
    private Float sbp;
}