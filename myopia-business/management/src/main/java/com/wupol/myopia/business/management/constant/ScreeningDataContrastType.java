package com.wupol.myopia.business.management.constant;

public enum ScreeningDataContrastType {
    TIME(0, "时间对比"),
    TIME_N_DISTRICT(1, "时间+区域对比"),
    TIME_N_SCHOOL_AGE(2, "时间+学龄段对比"),
    TIME_N_DISTRICT_N_SCHOOL_AGE(3, "时间+区域+学龄段对比");

    /** 类型 **/
    public final Integer code;

    /** 描述 **/
    public final String desc;

    ScreeningDataContrastType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ScreeningDataContrastType get(Integer code) {
        for (ScreeningDataContrastType e : ScreeningDataContrastType.values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
