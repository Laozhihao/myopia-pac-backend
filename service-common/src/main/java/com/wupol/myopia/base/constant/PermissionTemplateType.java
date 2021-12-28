package com.wupol.myopia.base.constant;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.ImmutableMap;
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

    /** 筛查机构管理员 */
    SCREENING_ORGANIZATION(0, "（省级）筛查机构权限集合包", RoleType.SCREENING_ORGANIZATION.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    SCREENING_ORG_SINGLE(7, "筛查机构单点权限集合包", RoleType.SCREENING_ORGANIZATION.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    SCREENING_ORG_VS666(8, "筛查机构VS666权限集合包", RoleType.SCREENING_ORGANIZATION.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    SCREENING_ORG_SINGLE_AND_VS666(9, "筛查机构单点+vs666权限集合包", RoleType.SCREENING_ORGANIZATION.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),

    /** 政府部门管理员 */
    PROVINCE(1, "省级权限集合包", RoleType.GOVERNMENT_DEPARTMENT.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    CITY(2, "市级权限集合包", RoleType.GOVERNMENT_DEPARTMENT.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    COUNTY(3, "县/区级权限集合包", RoleType.GOVERNMENT_DEPARTMENT.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    TOWN(4, "镇/乡/街道级权限集合包", RoleType.GOVERNMENT_DEPARTMENT.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),

    /** 平台管理员 */
    ALL(5, "超级管理员", RoleType.SUPER_ADMIN.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),
    PLATFORM_ADMIN(6, "平台管理员权限集合包", RoleType.PLATFORM_ADMIN.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),

    /** 医院管理员 */
    HOSPITAL_ADMIN(10, "医院管理员权限集合包", RoleType.HOSPITAL_ADMIN.getType(), SystemCode.MANAGEMENT_CLIENT.getCode()),

    /** 医院APP */
    HOSPITAL_RESIDENT_APP(11, "居民健康APP权限集合包", RoleType.RESIDENT_DOCTOR.getType(), SystemCode.HOSPITAL_CLIENT.getCode()),
    HOSPITAL_PRESCHOOL_APP(12, "0-6岁眼保健APP权限集合包", RoleType.PRESCHOOL_DOCTOR.getType(), SystemCode.HOSPITAL_CLIENT.getCode());

    private static final ImmutableMap<Integer, Integer> HOSPITAL_PERMISSION_TEMPLATE_TYPE_MAP;

    static {
        HOSPITAL_PERMISSION_TEMPLATE_TYPE_MAP = ImmutableMap.of(
                HospitalServiceType.RESIDENT.getType(), HOSPITAL_RESIDENT_APP.getType(),
                HospitalServiceType.PRESCHOOL.getType(), HOSPITAL_PRESCHOOL_APP.getType());
    }

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String msg;
    /**
     * 角色类型
     **/
    private final Integer roleType;
    /**
     * 系统编号
     **/
    private final Integer systemCode;

    PermissionTemplateType(Integer type, String descr, Integer roleType, Integer systemCode) {
        this.type = type;
        this.msg = descr;
        this.roleType = roleType;
        this.systemCode = systemCode;
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
        Assert.notNull(districtCode, "districtCode不能为空");
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
    public static boolean isGovTemplate(Integer type) {
        return PROVINCE.type.equals(type) || CITY.type.equals(type)
                || COUNTY.type.equals(type) || TOWN.type.equals(type);
    }

    /**
     * 是否特殊筛查机构类型
     *
     * @param type 类型
     * @return 是否筛查机构
     */
    public static boolean isScreeningOrgTemplate(Integer type) {
        return SCREENING_ORGANIZATION.type.equals(type) || SCREENING_ORG_SINGLE.type.equals(type)
                || SCREENING_ORG_VS666.type.equals(type) || SCREENING_ORG_SINGLE_AND_VS666.type.equals(type);
    }

    /**
     * 根据医院服务类型获取权限模板类型
     *
     * @param hospitalServiceType 医院服务类型
     * @return java.lang.Integer
     **/
    public static Integer getTemplateTypeByHospitalServiceType(Integer hospitalServiceType) {
        Assert.notNull(hospitalServiceType, "医院服务类型不能为空");
        return HOSPITAL_PERMISSION_TEMPLATE_TYPE_MAP.get(hospitalServiceType);
    }
}
