package com.wupol.myopia.business.management.constant;


import java.util.Arrays;
import java.util.Objects;

/**
 * 医院相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum HospitalEnum {
    TYPE_DESIGNATED(0, "定点医院"),
    TYPE_NON_DESIGNATED(1, "非定点医院"),
    KIND_PUBLIC(0, "公立"),
    KIND_PRIVATE(1, "私立");

    /** 类型 **/
    private final Integer type;
    /** 描述 **/
    private final String name;

    HospitalEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /** 根据类型获取描述 */
    public static String getTypeName(Integer type) {
        if (type.equals(TYPE_DESIGNATED.type)) {
            return TYPE_DESIGNATED.name;
        } else if (type.equals(TYPE_NON_DESIGNATED.type)) {
            return TYPE_NON_DESIGNATED.name;
        }
        return "";
    }

    /** 根据性质获取描述 */
    public static String getKindName(Integer kind) {
        if (kind.equals(KIND_PUBLIC.type)) {
            return KIND_PUBLIC.name;
        } else if (kind.equals(KIND_PRIVATE.type)) {
            return KIND_PRIVATE.name;
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

