package com.wupol.myopia.business.core.hospital.constant;

/**
 * 学生类型
 *
 * @author Simple4H
 */
public enum StudentTypeEnum {

    HOSPITAL_TYPE(1, "医院端"),
    PRESCHOOL_TYPE(2, "0到6岁"),
    HOSPITAL_AND_PRESCHOOL(3, "医院和0到6");


    private final Integer type;

    private final String desc;

    StudentTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
