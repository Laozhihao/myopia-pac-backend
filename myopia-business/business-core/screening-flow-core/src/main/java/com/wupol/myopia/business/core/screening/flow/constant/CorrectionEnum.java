package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.Getter;

/**
 * 矫正枚举
 *
 * @author hang.yuan 2022/4/24 17:09
 */
public enum CorrectionEnum {
    /**
     *
     */
    NORMAL_CORRECTION(1,"未矫"),
    ABOVE_CORRECTION(2,"足矫"),
    UNDER_CORRECTION(3,"欠矫");

    @Getter
    private Integer code;
    @Getter
    private String desc;

    CorrectionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
