package com.wupol.myopia.base.constant;

import com.wupol.myopia.base.util.DateUtil;

import java.util.Date;

/**
 * 合作时间类型
 *
 * @author Simple4H
 */
public enum CooperationTimeTypeEnum {

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
     * 名称
     **/
    private final String name;

    CooperationTimeTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
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

    public static String getCooperationTimeTypeDesc(Integer cooperationType, Integer cooperationTimeType, Date cooperationStartTime, Date cooperationEndTime) {
        if (COOPERATION_TIME_TYPE_CUSTOMIZE.getType().equals(cooperationTimeType)) {
            return CooperationTypeEnum.getCooperationTypeName(cooperationType) + " " + DateUtil.betweenDay(cooperationStartTime, cooperationEndTime) + "天";
        } else {
            return CooperationTypeEnum.getCooperationTypeName(cooperationType) + " " + CooperationTimeTypeEnum.getCooperationTimeTypeName(cooperationTimeType);
        }
    }
}
