package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.RoleMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;

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
     * @param roleId 角色ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getRolePermissionTree(Integer roleId) {
        Role role = getById(roleId);
        if (Objects.isNull(role)) {
            throw new ValidationException("不存在该角色");
        }
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return permissionService.getAdminRolePermissionTree(0, roleId);
        }
        // TODO: 根据角色所属的部门所在的行政区获取行政区等级
        return permissionService.selectRoleAllTree(0, roleId, 1);
    }

    /**
     * 通过ID集批量获取角色
     *
     * @param ids 角色ID集
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

    /**
     * 获取指定用户的角色
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> getRoleListByUserId(Integer userId) {
        return baseMapper.getRoleListByUserId(userId);
    }

}
