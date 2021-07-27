package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 眼压数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
@Accessors(chain = true)
public class IntraocularPressureDataDO implements Serializable {
    /**
     * 右眼数据
     */
    private IntraocularPressureData rightEyeData;
    /**
     * 左眼数据
     */
    private IntraocularPressureData leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class IntraocularPressureData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 眼压
         */
        private BigDecimal pressure;
    }

}
