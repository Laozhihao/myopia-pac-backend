package com.wupol.myopia.business.management.constant;


/**
 * 导入数据的相关常量
 *
 * @author Alix
 * @Date 2021-02-22
 */
public enum ImportExcelEnum {
    // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
    NAME(0, "姓名"),
    GENDER(1, "性别"),
    BIRTHDAY(2, "出生日期"),
    NATION(3, "民族"),
    SCHOOL_NO(4, "学校编号"),
    GRADE(5, "年级"),
    CLASS(6, "班级"),
    STUDENT_NO(7, "学号"),
    ID_CARD(8, "身份证号"),
    PHONE(9, "手机号码"),
    PROVINCE(10, "省"),
    CITY(11, "市"),
    AREA(12, "县区"),
    TOWN(13, "镇/街道"),
    ADDRESS(14, "居住地址");

    /** 列标 **/
    private final Integer index;
    /** 名称 **/
    private final String name;

    ImportExcelEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return this.index;
    }
    public String getName() {
        return this.name;
    }
   }

