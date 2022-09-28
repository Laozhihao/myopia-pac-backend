package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentResultDetailsDTO {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;
    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private Integer glassesType;
    /**
     * 佩戴眼镜的类型描述： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private String glassesTypeDes;
    /**
     * 夜戴角膜塑形镜的度数
     */
    private BigDecimal okDegree;

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
     * 房水深度（前房深度）
     */
    private String ad;

    /**
     * 眼轴
     */
    private String al;

    /**
     * 角膜中央厚度
     */
    private String cct;

    /**
     * 晶状体厚度
     */
    private String lt;

    /**
     * 角膜白到白距离
     */
    private String wtw;

    /**
     * 眼部疾病
     */
    private String eyeDiseases;

    /**
     * 是否远视
     */
    private Boolean isHyperopia;

    /**
     * 身高体重
     */
    private HeightAndWeightDataDO heightAndWeightData;
}
