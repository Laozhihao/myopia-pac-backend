package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.RoleMapper;
import com.wupol.myopia.oauth.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RoleService extends BaseService<RoleMapper, Role> {

    @Autowired
    private PermissionService permissionService;

    public Boolean assignRolePermission(Integer roleId) {
        return true;
    }

    public Object getRolePermissionTree(Integer roleId) {
        return permissionService.selectAllTree(0);
    }
}
