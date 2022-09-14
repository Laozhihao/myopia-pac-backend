package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 其它
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class OtherDataVO implements Serializable {
    /**
     * 左裂隙灯
     */
    private String leftSlitLamp;
    /**
     * 右裂隙灯
     */
    private String rightSlitLamp;
    /**
     * 左眼位
     */
    private String leftOcularInspection;
    /**
     * 右眼位
     */
    private String rightOcularInspection;
    /**
     * 左眼压
     */
    private String leftFundus;
    /**
     * 右眼压
     */
    private String rightFundus;
    /**
     * 左其它眼病
     */
    private String leftOtherEyeDiseases;
    /**
     * 右其它眼病
     */
    private String rightOtherEyeDiseases;



}