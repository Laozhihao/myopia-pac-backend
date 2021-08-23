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
public class EyePressureDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 右眼数据
     */
    private EyePressureData rightEyeData;
    /**
     * 左眼数据
     */
    private EyePressureData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Data
    @Accessors(chain = true)
    public static class EyePressureData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 眼压
         */
        private BigDecimal pressure;
    }

    /**
     * 判断诊断结果是否为正常，筛查APP没有录入初诊结果，故默认为正常
     *
     * @return boolean
     **/
    @Override
    public boolean isNormal() {
        return true;
    }

}
