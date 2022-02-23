package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 眼压数据
 *
 * @Author tastyb
 * @Date 2022/2/16
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HeightAndWeightDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 身高
     */
    private BigDecimal height;
    /**
     * 体重
     */
    private BigDecimal weight;
}
