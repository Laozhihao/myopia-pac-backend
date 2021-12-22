package com.wupol.myopia.business.core.hospital.constant;


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
    KIND_PRIVATE(1, "私立"),
    SERVICE_TYPE_HEALTHY_SYSTEM(0, "居民健康系统"),
    SERVICE_TYPE_PRE_SCHOOL(1, "0-6岁眼保健"),
    SERVICE_TYPE_HEALTHY_SYSTEM_AND_PRE_SCHOOL(2, "0-6岁眼保健+居民健康系统"),
    COOPERATION_TYPE_COOPERATE(0, "合作"),
    COOPERATION_TYPE_TRY_OUT(1, "试用"),
    COOPERATION_TIME_TYPE_CUSTOMIZE(-1, "自定义"),
    COOPERATION_TIME_TYPE_30_DAY(0, "30天"),
    COOPERATION_TIME_TYPE_60_DAY(1, "60天"),
    COOPERATION_TIME_TYPE_180_DAY(2, "180天"),
    COOPERATION_TIME_TYPE_1_YEAR(3, "1年"),
    COOPERATION_TIME_TYPE_2_YEAR(4, "2年"),
    COOPERATION_TIME_TYPE_3_YEAR(5, "3年");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String name;

    public Integer getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    HospitalEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 根据类型获取描述
     */
    public static String getTypeName(Integer type) {
        if (type.equals(TYPE_DESIGNATED.type)) {
            return TYPE_DESIGNATED.name;
        } else if (type.equals(TYPE_NON_DESIGNATED.type)) {
            return TYPE_NON_DESIGNATED.name;
        }
        return "";
    }

    /**
     * 根据性质获取描述
     */
    public static String getKindName(Integer kind) {
        if (kind.equals(KIND_PUBLIC.type)) {
            return KIND_PUBLIC.name;
        } else if (kind.equals(KIND_PRIVATE.type)) {
            return KIND_PRIVATE.name;
        }
        return "";
    }

    public static String getServiceTypeName(Integer type) {
        if (type.equals(SERVICE_TYPE_HEALTHY_SYSTEM.type)) {
            return SERVICE_TYPE_HEALTHY_SYSTEM.name;
        }
        if (type.equals(SERVICE_TYPE_PRE_SCHOOL.type)) {
            return SERVICE_TYPE_PRE_SCHOOL.name;
        }
        if (type.equals(SERVICE_TYPE_HEALTHY_SYSTEM_AND_PRE_SCHOOL.type)) {
            return SERVICE_TYPE_HEALTHY_SYSTEM_AND_PRE_SCHOOL.name;
        }
        return "";
    }

    public static String getCooperationTypeName(Integer type) {
        if (type.equals(COOPERATION_TYPE_COOPERATE.type)) {
            return COOPERATION_TYPE_COOPERATE.name;
        }
        if (type.equals(COOPERATION_TYPE_TRY_OUT.type)) {
            return COOPERATION_TYPE_TRY_OUT.name;
        }
        return "";
    }

    public static String getCooperationTimeTypeName(Integer type) {
        if (type.equals(COOPERATION_TIME_TYPE_CUSTOMIZE.type)) {
            return COOPERATION_TIME_TYPE_CUSTOMIZE.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_30_DAY.type)) {
            return COOPERATION_TIME_TYPE_30_DAY.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_60_DAY.type)) {
            return COOPERATION_TIME_TYPE_60_DAY.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_180_DAY.type)) {
            return COOPERATION_TIME_TYPE_180_DAY.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_1_YEAR.type)) {
            return COOPERATION_TIME_TYPE_1_YEAR.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_2_YEAR.type)) {
            return COOPERATION_TIME_TYPE_2_YEAR.name;
        }
        if (type.equals(COOPERATION_TIME_TYPE_3_YEAR.type)) {
            return COOPERATION_TIME_TYPE_3_YEAR.name;
        }
        return "";
    }
}