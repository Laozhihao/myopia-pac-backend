package com.wupol.myopia.business.common.utils.constant;

/**
 * 性别
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public enum GenderEnum {
    MALE(0, "男"),
    FEMALE(1, "女");

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String desc;

    GenderEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /** 获取性别名称 */
    public static String getName(Integer type) {
        if (MALE.type.equals(type)) return MALE.desc;
        if (FEMALE.type.equals(type)) return FEMALE.desc;
        return "未知";
    }

    /** 获取性别对应数值 */
    public static Integer getType(String name) {
        if (MALE.desc.equals(name)) return MALE.type;
        if (FEMALE.desc.equals(name)) return FEMALE.type;
        return -1;
    }
}
