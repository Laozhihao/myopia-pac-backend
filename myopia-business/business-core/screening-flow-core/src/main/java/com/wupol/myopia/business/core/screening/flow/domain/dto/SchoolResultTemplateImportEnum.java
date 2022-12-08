package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;

/**
 * 数据上传导入
 *
 * @author Simple4H
 */
public enum SchoolResultTemplateImportEnum {

    PLAN_STUDENT_ID(0, "筛查学生ID"),

    SNO(1, "学籍号"),

    STUDENT_NAME(2, "姓名"),

    CREDENTIALS(3, "证件号"),

    GENDER(4, "性别"),

    BIRTHDAY(5, "出生日期"),

    GRADE_NAME(6, "年级"),

    CLASS_NAME(7, "班级"),

    PASSPORT(8, "护照号"),

    GLASSES_TYPE(9, "戴镜情况"),

    RIGHT_NAKED_VISION(10, "裸眼（右）"),

    LEFT_NAKED_VISION(11, "裸眼（左）"),

    RIGHT_CORRECTION(12, "矫正（右）"),

    LEFT_CORRECTION(13, "矫正（左）"),

    RIGHT_SPH(14, "球镜（右）"),

    RIGHT_CYL(15, "柱镜（右）"),

    RIGHT_AXIAL(16, "轴位（右）"),

    LEFT_SPH(17, "球镜（左）"),

    LEFT_CYL(18, "柱镜（左）"),

    LEFT_AXIAL(19, "轴位（左）"),

    HEIGHT(20, "身高（单位CM）"),

    WEIGHT(21, "体重（单位KG）");


    @Getter
    private final Integer index;

    @Getter
    private final String desc;

    SchoolResultTemplateImportEnum(Integer index, String desc) {
        this.index = index;
        this.desc = desc;
    }
}