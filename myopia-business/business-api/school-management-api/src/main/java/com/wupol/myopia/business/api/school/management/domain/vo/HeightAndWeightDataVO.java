package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 身高体重
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class HeightAndWeightDataVO implements Serializable {
    /**
     * 身高
     */
    private BigDecimal height;
    /**
     * 体重
     */
    private BigDecimal weight;
}