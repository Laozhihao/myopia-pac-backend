package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;

/**
 * 眼压
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class EyePressureDataVO {
    /**
     * 右眼眼压
     */
    private String rightEyePressure;
    /**
     * 左眼眼压
     */
    private String leftEyePressure;
}