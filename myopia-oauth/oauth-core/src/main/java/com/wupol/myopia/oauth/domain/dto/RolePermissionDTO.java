package com.wupol.myopia.oauth.domain.dto;

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
     * 权限Id
     */
    private List<Integer> permissionIds;

    /**
     * 机构Id
     */
    private List<Integer> orgIds;
}
