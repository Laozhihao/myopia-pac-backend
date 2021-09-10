package com.wupol.myopia.business.aggregation.export.excel.constant;


/**
 * 导入数据的相关常量
 *
 * @author Alix
 * @Date 2021-02-22
 */
public enum ImportExcelEnum {
    // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
    SCREENING_CODE(0, "编码"),
    ID_CARD(1, "身份证号"),
    NAME(2, "姓名"),
    GENDER(3, "性别"),
    BIRTHDAY(4, "出生日期"),
    NATION(5, "民族"),
    GRADE(6, "年级"),
    CLASS(7, "班级"),
    STUDENT_NO(8, "学号"),
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

