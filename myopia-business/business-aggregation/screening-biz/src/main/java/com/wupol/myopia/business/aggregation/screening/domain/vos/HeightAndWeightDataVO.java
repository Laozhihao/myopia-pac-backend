package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;

import java.io.Serializable;

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
    private String height;
    /**
     * 体重
     */
    private String weight;
}