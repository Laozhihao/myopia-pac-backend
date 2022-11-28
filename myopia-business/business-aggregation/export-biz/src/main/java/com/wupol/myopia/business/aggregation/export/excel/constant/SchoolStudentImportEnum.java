package com.wupol.myopia.business.aggregation.export.excel.constant;

import lombok.Getter;

/**
 * 筛查人员导出实体
 *
 * @author Simple4H
 */
public enum SchoolStudentImportEnum {

    SNO(0, "学籍号"),
    NAME(1, "姓名"),
    ID_CARD(2, "身份证号"),
    GENDER(3, "性别"),
    BIRTHDAY(4, "出生日期"),
    GRADE_NAME(5, "年级"),
    CLASS_NAME(6, "班级"),
    NATION(7, "民族"),
    PASSPORT(8, "护照号"),
    PROVINCE_NAME(9, "省"),
    CITY_NAME(10, "市"),
    AREA_NAME(11, "县"),
    TOWN_NAME(12, "镇"),
    ADDRESS(13, "地址"),
    PHONE(14, "手机号码");

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

    SchoolStudentImportEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }
}