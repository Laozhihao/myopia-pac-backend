package com.wupol.myopia.business.common.utils.constant;

import com.wupol.myopia.base.exception.BusinessException;

import java.util.Objects;

/**
 * 性别
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public enum GenderEnum {
    MALE(0, "男"),
    FEMALE(1, "女"),

    ENMALE(2,"M"),
    ENFEMALE(3,"FM");

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

    /**
     * 格式化成VS666需要的
     *
     * @param gender 性别
     * @return M-男 FM-女
     */
    public static String getEnGenderDesc(Integer gender) {
        if (Objects.isNull(gender)) {
            throw new BusinessException("格式化成VS666二维码的性别异常");
        }
        return gender.equals(GenderEnum.MALE.type) ? ENMALE.desc : ENFEMALE.desc;
    }
}
