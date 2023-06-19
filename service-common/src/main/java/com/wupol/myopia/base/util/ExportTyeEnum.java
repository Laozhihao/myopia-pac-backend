package com.wupol.myopia.base.util;

import lombok.Getter;

/**
 * 导出类型
 *
 * @author Simple4H
 */

public enum ExportTyeEnum {

    REPORT(1, "报告"),
    QR_CODE(2, "二维码"),
    OTHER(3, "其他");

    @Getter
    private final Integer type;

    @Getter
    private final String desc;

    ExportTyeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
