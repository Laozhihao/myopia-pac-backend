package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Getter
@Setter
public class BmiData {

    /**
     * 身高值 cm
     */
    private String height;

    /**
     * 体重值 kg
     */
    private String weight;

    /**
     * 身体质量指数值
     */
    private String bmi;
}
