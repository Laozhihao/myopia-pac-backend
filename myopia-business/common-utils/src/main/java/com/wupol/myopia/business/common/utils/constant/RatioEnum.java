package com.wupol.myopia.business.common.utils.constant;

/**
 * @Author wulizhou
 * @Date 2021/6/10 10:59
 */
public enum RatioEnum {

    MYOPIA(0, "近视率"),
    LOW_VISION(1, "视力低下率"),
    AVERAGE_VISION(2, "平均视力"),
    UNCORRECTED(3, "未矫率"),
    WEARING_RATIO(4, "戴镜率"),
    UNDER_CORRECTED(5, "欠矫率"),
    ENOUGH_CORRECTED(6, "足矫率"),
    WARNING_LEVEL_0(7, "0级预警率"),
    WARNING_LEVEL_1(8, "1级预警率"),
    WARNING_LEVEL_2(9, "2级预警率"),
    WARNING_LEVEL_3(10, "3级预警率"),
    ;

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String desc;

    RatioEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
