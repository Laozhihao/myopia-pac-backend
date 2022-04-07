package com.wupol.myopia.business.api.device.domain.constant;

import lombok.Getter;

/**
 * 业务类型枚举
 *
 * @author Simple4H
 */
@Getter
public enum BusinessTypeEnum {

    VISION_DATA(1, "视力数据");


    private final Integer type;

    private final String desc;


    BusinessTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
