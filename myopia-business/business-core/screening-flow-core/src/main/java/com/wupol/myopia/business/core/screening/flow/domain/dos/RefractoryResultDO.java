package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 验光仪检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractoryResultDO {

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
}
