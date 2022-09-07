package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.Getter;

/**
 * rec导出数据类型
 *
 * @author hang.yuan 2022/8/30 21:45
 */
public enum ExportDataTypeEnum {
    ARCHIVE_REC(1,"监测表数据"),
    QUESTIONNAIRE(2,"问卷数据");

    @Getter
    private final Integer code;
    @Getter
    private final String desc;

    ExportDataTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
