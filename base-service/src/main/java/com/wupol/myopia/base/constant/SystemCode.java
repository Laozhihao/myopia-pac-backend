package com.wupol.myopia.base.constant;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 各个端系统编号
 *
 * @Author HaoHao
 * @Date 2020/12/25 14:32
 **/
@Getter
public enum SystemCode {
    /** 管理端系统编号 */
    MANAGEMENT_CLIENT(1, "管理端"),
    SCHOOL_CLIENT(2, "学校端"),
    SCREENING_CLIENT(3, "筛查端"),
    HOSPITAL_CLIENT(4, "医院端"),
    PATENT_CLIENT(5, "家长端");

    /**
     * 系统编号
     **/
    private final Integer code;
    /**
     * 描述
     **/
    private final String msg;

    SystemCode(Integer code, String descr) {
        this.code = code;
        this.msg = descr;
    }

    /**
     * 根据编号获取对应SystemCode
     *
     * @param code 编号
     * @return com.wupol.myopia.base.constan.SystemCode
     **/
    public static SystemCode getByCode(Integer code) {
        return Arrays.stream(values()).filter(systemCode -> systemCode.getCode().equals(code)).findFirst().orElse(null);
    }

    /**
     * 获取所有code
     *
     * @return java.util.List<java.lang.Object>
     **/
    public static List<Object> getAllCode() {
        return EnumUtil.getFieldValues(SystemCode.class, "code");
    }
}
