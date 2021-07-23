package com.wupol.myopia.oauth.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 角色权限
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class RolePermissionDTO {

    /**
     * 部门Id
     */
    private List<Integer> govIds;

    /**
     * 权限Id
     */
    private List<Integer> permissionIds;

    public RolePermissionDTO(List<Integer> govIds, List<Integer> permissionIds) {
        this.govIds = govIds;
        this.permissionIds = permissionIds;
    }
}
