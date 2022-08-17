package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.Getter;

/**
 * 筛查人员导出实体
 *
 * @author Simple4H
 */
public enum StaffImportEnum {

    NAME(0, "姓名"),

    GENDER(1, "性别"),

    ID_CARD(2, "身份证号"),

    PHONE(3, "手机号码"),

    REMARK(4, "备注");

    /**
     * 列标
     **/
    @Getter
    private final Integer index;
    /**
     * 名称
     **/
    @Getter
    private final String name;

    StaffImportEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }
}
