package com.wupol.myopia.business.aggregation.screening.constant;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据上报类型
 *
 * @author Simple4H
 */
public enum DataSubmitTypeEnum {

    NATION(0, "国家表格", 0),
    CHANG_SHA(1, "长沙市数据报送（教育版）", 3),
    SHANG_HAI(2,"上海市数据报送（静安区教育版）", 0),
    SHANG_HAI_HEALTH_COMMISSION(3,"上海卫健委数据数据报送", 0);

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

    public static List<DataSubmitType> getDataSubmitTypeList() {
        List<DataSubmitType> dataSubmitTypes = new ArrayList<>();
        for (DataSubmitTypeEnum value : values()) {
            DataSubmitType dataSubmitType = new DataSubmitType();
            dataSubmitType.setDesc(value.getDesc());
            dataSubmitType.setType(value.getType());
            dataSubmitTypes.add(dataSubmitType);
        }
        return dataSubmitTypes;
    }
}
