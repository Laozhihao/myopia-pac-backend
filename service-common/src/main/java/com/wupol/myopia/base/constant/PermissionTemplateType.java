package com.wupol.myopia.base.constant;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * 权限模板类型（权限集合包）
 *
 * @Author HaoHao
 * @Date 2020/12/25 14:32
 **/
@Getter
public enum PermissionTemplateType {
    /** 权限集合包 */
    SCREENING_ORGANIZATION(0, "筛查机构权限集合包"),
    PROVINCE(1, "省级权限集合包"),
    CITY(2, "市级权限集合包"),
    COUNTY(3, "县/区级权限集合包"),
    TOWN(4, "镇/乡/街道级权限集合包"),
    ALL(5, "超级管理员"),
    PLATFORM_ADMIN(6,"平台管理员权限集合包");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;

    PermissionTemplateType(Integer type, String descr) {
        this.type = type;
        this.msg = descr;
    }

    /**
     * 根据类型获取对应模板类型
     *
     * @param type 类型
     * @return com.wupol.myopia.base.constan.RoleType
     **/
    public static PermissionTemplateType getByType(Integer type) {
        return Arrays.stream(values()).filter(roleType -> roleType.getType().equals(type))
                .findFirst().orElse(null);
    }

    /**
     * 获取所有Type
     *
     * @return java.util.List<java.lang.Object>
     **/
    public static List<Object> getAllType() {
        return EnumUtil.getFieldValues(PermissionTemplateType.class, "type");
    }

    /**
     * 根据行政区编号获取对应的模板类型
     *
     * @param districtCode 行政区编号
     * @return java.lang.Integer
     **/
    public static Integer getTypeByDistrictCode(Long districtCode) {
        String prefix = StrUtil.subBefore(String.valueOf(districtCode), "000", false);
        Assert.hasLength(prefix, "无效行政区编号");
        switch (prefix.length()) {
            case 2: return PROVINCE.type;
            case 3: return CITY.type;
            case 4: return CITY.type;
            case 5: return COUNTY.type;
            case 6: return COUNTY.type;
            default: return TOWN.type;
        }
    }

    /**
     * 是否政府人员类型
     *
     * @param type 类型
     * @return 是否政府人员
     */
    public static boolean isGovUser(Integer type) {
        return PROVINCE.type.equals(type) || CITY.type.equals(type)
                || COUNTY.type.equals(type) || TOWN.type.equals(type);
    }
}
