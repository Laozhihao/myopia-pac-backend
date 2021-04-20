package com.wupol.myopia.business.management.constant;

/**
 * 学校相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum SchoolType {
    TYPE_PRIMARY(0, "小学"),
    TYPE_MIDDLE(1, "初级中学"),
    TYPE_HIGH(2, "高级中学"),
    TYPE_INTEGRATED_MIDDLE(3, "完全中学"),
    TYPE_9(4, "九年一贯制学校"),
    TYPE_12(5, "十二年一贯制学校"),
    TYPE_VOCATIONAL(6, "职业高中"),
    TYPE_OTHER(7, "其他");

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String name;

    SchoolType(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /** 根据type获取当前枚举对象 */
    public static SchoolType get(Integer type) {
        for (SchoolType e : SchoolType.values()) {
            if (e.type == type) {
                return e;
            }
        }
        return null;
    }
}
