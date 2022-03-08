package com.wupol.myopia.business.common.utils.constant;

/**
 * 学校端-学生来源
 *
 * @author Simple4H
 */
public enum SourceClientEnum {

    MANAGEMENT(0, "多端"),
    SCHOOL(1, "学校端"),
    SCREENING_PLAN(2, "筛查计划"),
    SCREENING_APP(3, "筛查App");

    public final Integer type;

    /** 学龄段描述 */
    public final String desc;

    SourceClientEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
