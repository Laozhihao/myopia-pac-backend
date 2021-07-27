package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.wupol.myopia.business.common.utils.interfaces.ScreeningResultStructureInterface;
import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小瞳验光数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
@Accessors(chain = true)
public class PupilOptometryDataDO implements ScreeningResultStructureInterface<PupilOptometryDataDO.PupilOptometryData>,  Serializable {
    /**
     * 右眼数据
     */
    private PupilOptometryData rightEyeData;
    /**
     * 左眼数据
     */
    private PupilOptometryData leftEyeData;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;
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

}
