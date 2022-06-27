package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 性别
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public enum GenderEnum {
    /** 性别常量 */
    UNKNOWN(-1,"未知","UNKNOWN", "未知"),
    MALE(0, "男","M","男生"),
    FEMALE(1, "女", "FM","女生");

    /** 类型 **/
    public final Integer type;
    /** 描述 **/
    public final String desc;
    /** 英文描述*/
    public final String enDesc;

    /** 中文描述*/
    public final String cnDesc;

    GenderEnum(Integer type, String desc, String enDesc, String cnDesc) {
        this.type = type;
        this.desc = desc;
        this.enDesc = enDesc;
        this.cnDesc = cnDesc;
    }

    /** 获取性别名称 */
    public static String getName(Integer type) {
        if (MALE.type.equals(type)) {return MALE.desc;}
        if (FEMALE.type.equals(type)) {return FEMALE.desc;}
        return UNKNOWN.desc;
    }

    /** 获取性别对应数值 */
    public static Integer getType(String name) {
        if (MALE.desc.equals(name) || MALE.enDesc.equals(name)) {return MALE.type;}
        if (FEMALE.desc.equals(name) || FEMALE.enDesc.equals(name)) {return FEMALE.type;}
        return UNKNOWN.type;
    }

    /**
     * 格式化成VS666需要的
     *
     * @param gender 性别
     * @return M-男 FM-女
     */
    public static String getEnGenderDesc(Integer gender) {
        if (Objects.isNull(gender)) {
            throw new BusinessException("性别不能为空");
        }
        return Arrays.stream(values()).filter(x -> x.type.equals(gender)).map(x -> x.enDesc).findFirst().orElse(UNKNOWN.enDesc);
    }

    public static List<Integer> genderTypeList() {
        return Lists.newArrayList(MALE.type, FEMALE.type);
    }

    public static List<GenderEnum> genderList() {
        return Lists.newArrayList(MALE, FEMALE);
    }

    /** 获取性别名称 */
    public static String getCnName(Integer type) {
        if (MALE.type.equals(type)) return MALE.cnDesc;
        if (FEMALE.type.equals(type)) return FEMALE.cnDesc;
        return UNKNOWN.cnDesc;
    }
}
