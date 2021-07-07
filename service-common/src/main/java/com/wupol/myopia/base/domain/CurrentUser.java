package com.wupol.myopia.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.base.constant.RoleType;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/26
 **/
@Data
@UtilityClass
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
     * 是否平台管理员
     */
    public boolean isPlatformAdminUser() {
        return !CollectionUtils.isEmpty(roleTypes) && roleTypes.contains(RoleType.SUPER_ADMIN.getType());
    }

    /**
     * 是否政府部门用户
     */
    @JsonIgnore
    public boolean isGovDeptUser() {
        return !CollectionUtils.isEmpty(roleTypes) && roleTypes.contains(RoleType.GOVERNMENT_DEPARTMENT.getType());
    }

    /**
     * 是否筛查端用户
     */
    @JsonIgnore
    public boolean isScreeningUser() {
        return !CollectionUtils.isEmpty(roleTypes) && roleTypes.contains(RoleType.SCREENING_ORGANIZATION.getType());
    }
}
