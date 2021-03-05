package com.wupol.myopia.business.management.constant;

/**
 * 性别
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public enum GenderEnum {
    MALE(1, "男"),
    FEMALE(2, "女");

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String name;

    GenderEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /** 获取性别名称 */
    public static String getName(Integer type) {
        if (MALE.type.equals(type)) return MALE.name;
        if (FEMALE.type.equals(type)) return FEMALE.name;
        return "未知";
    }

    /** 获取性别对应数值 */
    public static Integer getType(String name) {
        if (MALE.name.equals(name)) return MALE.type;
        if (FEMALE.name.equals(name)) return FEMALE.type;
        return 0;
    }
}
