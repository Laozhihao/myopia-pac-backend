package com.wupol.myopia.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/26
 **/
@Data
public class CurrentUser {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 筛查机构ID
     */
    private Integer screeningOrgId;

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 系统编号
     */
    private Integer systemCode;

    /**
     * 角色类型
     */
    private List<Integer> roleTypes;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 系统用户类型
     */
    private String clientId;

    /**
     * 是否平台管理员
     */
    public boolean isPlatformAdminUser() {
        return !CollectionUtils.isEmpty(roleTypes) && (roleTypes.contains(RoleType.SUPER_ADMIN.getType()) || roleTypes.contains(RoleType.PLATFORM_ADMIN.getType()));
    }

    /**
     * 是否政府部门用户
     */
    @JsonIgnore
    public boolean isGovDeptUser() {
        return !CollectionUtils.isEmpty(roleTypes) && roleTypes.contains(RoleType.GOVERNMENT_DEPARTMENT.getType());
    }

    /**
     * 是否筛查管理端用户
     */
    @JsonIgnore
    public boolean isScreeningUser() {
        return !CollectionUtils.isEmpty(roleTypes) && roleTypes.contains(RoleType.SCREENING_ORGANIZATION.getType());
    }

    /**
     * 是否医院管理员用户
     * @return
     */
    @JsonIgnore
    public boolean isHospitalUser() {
        return UserType.HOSPITAL_ADMIN.getType().equals(userType) && SystemCode.MANAGEMENT_CLIENT.getCode().equals(systemCode);
    }

}
