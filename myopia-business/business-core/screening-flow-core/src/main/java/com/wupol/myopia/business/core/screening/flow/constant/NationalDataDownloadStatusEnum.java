package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author Simple4H
 */
public enum NationalDataDownloadStatusEnum {

    CREATE(0, "创建"),
    PROCESS(1, "进行中"),
    SUCCESS(1, "成功"),
    FAIL(1, "失败");

    /**
     * 类型
     **/
    @Getter
    private final Integer type;
    /**
     * 描述
     **/
    @Getter
    private final String name;

    NationalDataDownloadStatusEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
