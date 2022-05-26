package com.wupol.myopia.business.api.management.constant;

import lombok.Getter;

/**
 * 档案卡类型
 *
 * @Author HaoHao
 * @Date 2022/5/24
 **/
@Getter
public enum ArchiveTypeEnum {

    DISTRICT(1, "区域"),
    SCHOOL(2, "学校"),
    GRADE(3, "年级"),
    CLASS(4, "班级"),
    STUDENT(5, "多个或单个学生");

    /**
     * 类型
     **/
    private Integer type;
    /**
     * 描述
     **/
    private String descr;

    ArchiveTypeEnum(Integer type, String descr) {
        this.type = type;
        this.descr = descr;
    }
}
