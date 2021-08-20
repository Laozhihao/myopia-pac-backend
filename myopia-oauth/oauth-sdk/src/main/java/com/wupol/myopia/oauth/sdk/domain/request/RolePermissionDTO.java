package com.wupol.myopia.oauth.sdk.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色权限
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO {
    /**
     * 部门Id
     */
    private List<Integer> govIds;

    /**
     * 权限Id
     */
    private List<Integer> permissionIds;

    /**
     * 筛查机构Id
     */
    private List<Integer> orgIds;
}
