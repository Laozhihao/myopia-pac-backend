package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 血压
 * @Date 2021/4/06 16:50
 * @Author xz
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class BloodPressureDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 舒张压
     */
    private BigDecimal dbp;

    /**
     * 收缩压
     */
    private BigDecimal sbp;
}
