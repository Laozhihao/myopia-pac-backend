package com.wupol.myopia.business.management.constant;
public enum ScreeningDataCompareType {
    TIME(0, "时间对比"),
    TIME_N_DISTRICT(1, "时间+区域对比"),
    TIME_N_SCHOOL_TYPE(2, "时间+学校类型对比"),
    TIME_N_DISTRICT_N_SCHOOL_TYPE(3, "时间+区域+学校类型对比");

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String desc;

    ScreeningDataCompareType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static ScreeningDataCompareType get(Integer type) {
        for (ScreeningDataCompareType e : ScreeningDataCompareType.values()) {
            if (e.type == type) {
                return e;
            }
        }
        return null;
    }
}
