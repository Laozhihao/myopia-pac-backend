package com.wupol.myopia.business.aggregation.screening.constant;

import lombok.Getter;

/**
 * 数据上报类型
 *
 * @author Simple4H
 */
public enum DataSubmitTypeEnum {

    NATION(0, "国家表格", 0),
    CHANG_SHA(1, "长沙市数据报送（教育版）", 3);

    @Getter
    private final Integer type;
    @Getter
    private final String desc;
    @Getter
    private final Integer removeRows;

    DataSubmitTypeEnum(Integer type, String desc, Integer removeRows) {
        this.type = type;
        this.desc = desc;
        this.removeRows = removeRows;
    }
}
