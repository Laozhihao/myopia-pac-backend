package com.wupol.myopia.base.constant;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 医院服务类型
 *
 * @Author HaoHao
 * @Date 2020/12/25 14:32
 **/
@Getter
public enum HospitalServiceType {
    /** 医院端用 */
    RESIDENT(0, "居民健康系统"),
    PRESCHOOL(1, "0-6岁眼保健系统"),
    SCREENING_ORGANIZATION(2, "0-6岁眼保健+居民健康系统");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;

    HospitalServiceType(Integer type, String descr) {
        this.type = type;
        this.msg = descr;
    }

    /**
     * 根据类型获取对应RoleType
     *
     * @param type 类型
     * @return com.wupol.myopia.base.constan.RoleType
     **/
    public static HospitalServiceType getByType(Integer type) {
        return Arrays.stream(values()).filter(roleType -> roleType.getType().equals(type))
                .findFirst().orElse(null);
    }

    /**
     * 获取所有Type
     *
     * @return java.util.List<java.lang.Object>
     **/
    public static List<Object> getAllType() {
        return EnumUtil.getFieldValues(HospitalServiceType.class, "type");
    }
}
