package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.interfaces.ScreeningResultStructureInterface;
import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 电脑验光数据
 * @Description
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class ComputerOptometryDO extends AbstractDiagnosisResult implements ScreeningResultStructureInterface<ComputerOptometryDO.ComputerOptometry>, Serializable {
    /**
     * 右眼数据
     */
    private ComputerOptometry rightEyeData;
    /**
     * 左眼数据
     */
    private ComputerOptometry leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    /**
     * 电脑验光具体数据
     * @Description
     * @Date 2021/1/22 16:37
     * @Author by jacob
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Accessors(chain = true)
    public static class ComputerOptometry implements ValidResultDataInterface, Serializable {
        /**
         * 0 为左眼 1为右眼
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

        @Override
        public boolean judgeValidData() {
            return sph != null;
        }

        public BigDecimal getAxial() {
            return BigDecimalUtil.getBigDecimalByFormat(axial, 2);
        }

        public BigDecimal getSph() {
            return BigDecimalUtil.getBigDecimalByFormat(sph, 2);
        }

        public BigDecimal getCyl() {
            return BigDecimalUtil.getBigDecimalByFormat(cyl, 2);
        }
    }

    /**
     * 电脑数据是否完整
     * @return
     */
    public boolean valid() {
        return ObjectsUtil.allNotNull(getLeftEyeData()
                , getLeftEyeData().getAxial(), getLeftEyeData().getCyl(), getLeftEyeData().getSph()
                , getRightEyeData(), getRightEyeData().getAxial(), getRightEyeData().getCyl(), getRightEyeData().getSph());
    }

}

