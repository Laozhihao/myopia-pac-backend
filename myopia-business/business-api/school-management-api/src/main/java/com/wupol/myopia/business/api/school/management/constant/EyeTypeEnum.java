package com.wupol.myopia.business.api.school.management.constant;

import lombok.Getter;

/**
 * 眼型
 *
 * @author hang.yuan 2022/9/18 18:37
 */
public enum EyeTypeEnum {

    LEFT_EYE(0,"左眼"),
    RIGHT_EYE(1,"右眼");

    @Getter
    private final Integer code;
    @Getter
    private final String desc;

    EyeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
