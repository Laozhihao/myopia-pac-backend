package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.RoleMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RoleService extends BaseService<RoleMapper, Role> {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    /**
     * 获取角色列表
     *
     * @param query 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> selectRoleList(Role query) {
        return baseMapper.selectRoleList(query);
    }

    /**
     * 赋予新角色权限
     *
     * @param roleId        角色id
     * @param permissionIds 权限资源ID
     * @return 角色权限列表
     */
    @Transactional
    public List<RolePermission> assignRolePermission(Integer roleId, List<Integer> permissionIds) {
        rolePermissionService.deleteByRoleId(roleId);
        rolePermissionService.insertRolePermissionBatch(roleId, permissionIds);
        return rolePermissionService.getRolePermissionByRoleId(roleId);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId        角色ID
     * @param districtLevel 行政区等级
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getRolePermissionTree(Integer roleId, Integer districtLevel) {
        return permissionService.selectRoleAllTree(0, roleId, districtLevel);
    }

    List<Role> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

}
