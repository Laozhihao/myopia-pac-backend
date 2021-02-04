package com.wupol.myopia.business.management.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @Description 电脑验光数据
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
@Accessors(chain = true)
public class ComputerOptometryDO {
    private ComputerOptometry rightEyeData;
    private ComputerOptometry leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class ComputerOptometry {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 轴位
         */
        private String axial;

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

