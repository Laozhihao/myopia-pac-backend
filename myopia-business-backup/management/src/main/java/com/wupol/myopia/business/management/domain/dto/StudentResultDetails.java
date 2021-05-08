package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentResultDetails {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;

    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private String glassesType;

    /**
     * 矫正视力
     */
    private BigDecimal correctedVision;

    /**
     * 裸眼视力
     */
    private BigDecimal nakedVision;

    /**
     * 轴位
     */
    private BigDecimal axial;

    /**
     * 球镜
     */
    private BigDecimal sph;

    /**
     * 等效球镜
     */
    private BigDecimal se;

    /**
     * 柱镜
     */
    private BigDecimal cyl;

    /**
     * AD
     */
    private String AD;

    /**
     眼轴
     */
    private String AL;

    /**
     * 角膜中央厚度
     */
    private String CCT;

    /**
     * 晶状体厚度
     */
    private String LT;

    /**
     * 角膜白到白距离
     */
    private String WTW;

    /**
     * 眼部疾病
     */
    private List<String> eyeDiseases;
}
