package com.wupol.myopia.business.aggregation.export.excel.constant;


/**
 * 导入数据的相关常量
 *
 * @author Alix
 * @Date 2021-02-22
 */
public enum ImportExcelEnum {

    SCREENING_CODE(0, "编码"),
    ID_CARD(1, "身份证号"),
    PASSPORT(2, "护照"),
    NAME(3, "姓名"),
    GENDER(4, "性别"),
    BIRTHDAY(5, "出生日期"),
    NATION(6, "民族"),
    GRADE(7, "年级"),
    CLASS(8, "班级"),
    STUDENT_NO(9, "学号"),
    PHONE(10, "手机号码");

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

