package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.RoleType;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.mapper.RoleMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<Role> getRoleList(RoleDTO query) {
        return baseMapper.selectRoleList(query);
    }

    /**
     * 获取角色列表 - 分页
     *
     * @param query 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public IPage<Role> getRoleListByPage(RoleDTO query) {
        Assert.notNull(query.getCurrent(), "页码为空");
        Assert.notNull(query.getSize(), "页数为空");
        Page<Role> page = new Page<>(query.getCurrent(), query.getSize());
        return baseMapper.selectRoleList(page, query);
    }

    /**
     * 赋予新角色权限
     *
     * @param roleId        角色id
     * @param permissionIds 权限资源ID
     * @return 角色权限列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RolePermission> assignRolePermission(Integer roleId, List<Integer> permissionIds) {
        rolePermissionService.remove(new RolePermission().setRoleId(roleId));
        List<RolePermission> rolePermission = permissionIds.stream().map(id -> new RolePermission().setRoleId(roleId).setPermissionId(id)).collect(Collectors.toList());
        rolePermissionService.saveBatch(rolePermission);
        return rolePermission;
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId       角色ID
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getRolePermissionTree(Integer roleId, Integer templateType) {
        Role role = getById(roleId);
        Assert.notNull(role, "不存在该角色");
        if (RoleType.SUPER_ADMIN.getType().equals(role.getRoleType())) {
            return permissionService.getAdminRolePermissionTree(0, roleId);
        }
        return permissionService.selectRoleAllTree(0, roleId, templateType);
    }

    /**
     * 通过ID集批量获取角色
     *
     * @param ids 角色ID集
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 获取指定用户的角色(全部)
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getRoleListByUserId(Integer userId) {
        return baseMapper.selectRoleListByUserId(userId);
    }

    /**
     * 获取指定用户的角色(可用的)
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    public List<Role> getUsableRoleByUserId(Integer userId) {
        List<Role> roleList = getRoleListByUserId(userId);
        if (CollectionUtils.isEmpty(roleList)) {
            return roleList;
        }
        return roleList.stream().filter(x -> x.getStatus() == 0).collect(Collectors.toList());
    }

    /**
     * 获取用户ID列表
     *
     * @param query 查询条件
     * @return java.util.List<java.lang.Integer>
     **/
    public List<Integer> getUserIdList(Role query) {
        List<Integer> userIds = baseMapper.selectUserIdList(query);
        if (CollectionUtils.isEmpty(userIds)) {
            return userIds;
        }
        return userIds.stream().distinct().collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermission(Integer roleId, List<Integer> permissionIds) {
        List<RolePermission> originLists = rolePermissionService.getByRoleId(roleId);

        List<Integer> addList = permissionIds.stream()
                .filter(item -> !originLists.stream()
                        .map(RolePermission::getPermissionId)
                        .collect(Collectors.toList())
                        .contains(item))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(addList)) {
            rolePermissionService.batchInsert(roleId, addList);
        }

        // 同理，取删除的
        List<Integer> deletedLists = originLists.stream()
                .map(RolePermission::getPermissionId)
                .filter(permissionId -> !permissionIds.contains(permissionId))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(deletedLists)) {
            rolePermissionService.batchDeleted(roleId, deletedLists);
        }
    }

}
