package com.wupol.myopia.business.management.constant;


/**
 * 学校相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum SchoolEnum {
//    寄宿状态 0- 1- 2-
    LODGE_ALL(0, "全部住校"),
    LODGE_PART(1, "部分住校"),
    LODGE_NON(2, "不住校"),
    TYPE_PRIMARY(0, "小学"),
    TYPE_MIDDLE (1, "初级中学"),
    TYPE_HIGH(2, "高级中学"),
    TYPE_INTEGRATED_MIDDLE(3, "完全中学"),
    TYPE_9(4, "九年一贯制学校"),
    TYPE_12(5, "十二年一贯制学校"),
    TYPE_VOCATIONAL(6, "职业高中"),
    TYPE_OTHER(7, "7其他"),
    KIND_1(0,"公办"),
    KIND_2(1,"民办"),
    KIND_3(2,"其他");

    /** 类型 **/
    private final Integer type;
    /** 描述 **/
    private final String name;

    SchoolEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /** 根据类型获取描述 */
    public static String getLodgeName(Integer type) {
        if (type.equals(LODGE_ALL.type)) {
            return LODGE_ALL.name;
        } else if (type.equals(LODGE_PART.type)) {
            return LODGE_PART.name;
        } else if (type.equals(LODGE_NON.type)) {
            return LODGE_NON.name;
        }
        return "";
    }

    /** 根据性质获取描述 */
    public static String getTypeName(Integer kind) {
        if (kind.equals(TYPE_PRIMARY.type)) {
            return TYPE_PRIMARY.name;
        } else if (kind.equals(TYPE_MIDDLE.type)) {
            return TYPE_MIDDLE.name;
        } else if (kind.equals(TYPE_HIGH.type)) {
            return TYPE_HIGH.name;
        } else if (kind.equals(TYPE_INTEGRATED_MIDDLE.type)) {
            return TYPE_INTEGRATED_MIDDLE.name;
        } else if (kind.equals(TYPE_9.type)) {
            return TYPE_9.name;
        } else if (kind.equals(TYPE_12.type)) {
            return TYPE_12.name;
        } else if (kind.equals(TYPE_VOCATIONAL.type)) {
            return TYPE_VOCATIONAL.name;
        } else if (kind.equals(TYPE_OTHER.type)) {
            return TYPE_OTHER.name;
        }
        return "";
    }

    public static String getKindName(Integer type) {
        if (type.equals(KIND_1.type)) {
            return KIND_1.name;
        } else if (type.equals(KIND_2.type)) {
            return KIND_2.name;
        } else if (type.equals(KIND_3.type)) {
            return KIND_3.name;
        }
        return "";
    }

    public Integer getType() {
        return this.type;
    }
    public String getName() {
        return this.name;
    }
   }

