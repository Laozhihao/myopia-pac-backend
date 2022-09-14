package com.wupol.myopia.business.api.school.management.domain.vo;

import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小瞳验光
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class PupilOptometryDataVO implements Serializable {
    /**
     * 右眼数据
     */
    private PupilOptometryData rightEyeData;
    /**
     * 左眼数据
     */
    private PupilOptometryData leftEyeData;

    @Data
    public static class PupilOptometryData implements Serializable {
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
    }
}
