package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.common.utils.interfaces.ScreeningResultStructureInterface;
import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小瞳验光数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PupilOptometryDataDO extends AbstractDiagnosisResult implements ScreeningResultStructureInterface<PupilOptometryDataDO.PupilOptometryData>,  Serializable {
    /**
     * 右眼数据
     */
    private PupilOptometryData rightEyeData;
    /**
     * 左眼数据
     */
    private PupilOptometryData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Data
    @Accessors(chain = true)
    public static class PupilOptometryData implements ValidResultDataInterface, Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 轴位
         */
        private BigDecimal axial;
        /**
         * 球镜
         */
        private BigDecimal sph;
        /**
         * 柱镜
         */
        private BigDecimal cyl;
        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;

        @Override
        public boolean judgeValidData() {
            return sph != null;
        }
    }

    /**
     * 判断诊断结果是否为正常（没有异常判断标志，故默认为正常）
     *
     * @return boolean
     **/
    @Override
    public boolean isNormal() {
        return true;
    }

}
