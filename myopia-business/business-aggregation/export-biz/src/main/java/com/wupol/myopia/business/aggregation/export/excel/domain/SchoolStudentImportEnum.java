package com.wupol.myopia.business.aggregation.export.excel.domain;

/**
 * 筛查人员导出实体
 *
 * @author Simple4H
 */
public enum SchoolStudentImportEnum {

    NAME(0, "姓名"),
    GENDER(1, "性别"),
    BIRTHDAY(2, "出生日期"),
    NATION(3, "民族"),
    GRADE_NAME(4, "年级"),
    CLASS_NAME(5, "班级"),
    SNO(6, "学号"),
    ID_CARD(7, "身份证号"),
    PASSPORT(8, "护照"),
    PHONE(9, "手机号码"),
    PROVINCE_NAME(10, "省"),
    CITY_NAME(11, "市"),
    AREA_NAME(12, "县"),
    TOWN_NAME(13, "镇"),
    ADDRESS(14, "地址");

    /**
     * 列标
     **/
    private final Integer index;
    /**
     * 名称
     **/
    private final String name;

    SchoolStudentImportEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getIndex() {
        return index;
    }
}