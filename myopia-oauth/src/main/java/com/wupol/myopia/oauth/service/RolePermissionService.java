package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.RolePermissionMapper;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RolePermissionService extends BaseService<RolePermissionMapper, RolePermission> {

    /**
     * 通过role删除所有的角色权限
     *
     * @param roleId 角色ID
     */
    @Transactional
    public void deleteByRoleId(Integer roleId) {
        baseMapper.delete(new QueryWrapper<RolePermission>().eq("role_id", roleId));
    }

    /**
     * 批量新增角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限资源ID
     */
    @Transactional
    public void insertRolePermissionBatch(Integer roleId, List<Integer> permissionIds) {
        baseMapper.insertRolePermissionBatch(roleId, permissionIds);
    }

    /**
     * 通过角色id获取列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    public List<RolePermission> getRolePermissionByRoleId(Integer roleId) {
        return baseMapper.selectList(new QueryWrapper<RolePermission>().eq("role_id", roleId));
    }
}
