package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.RolePermissionMapper;
import com.wupol.myopia.oauth.domain.model.RolePermission;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class RolePermissionService extends BaseService<RolePermissionMapper, RolePermission> {

    /**
     * 批量插入集合
     *
     * @param roleId        角色Id
     * @param permissionIds 权限集合
     */
    public void batchInsert(Integer roleId, List<Integer> permissionIds) {
        baseMapper.insertRolePermissionBatch(roleId, permissionIds);
    }

    /**
     * 批量删除
     *
     * @param roleId        角色Id
     * @param permissionIds 权限集合
     */
    public void batchDeleted(Integer roleId, List<Integer> permissionIds) {
        baseMapper.deletedRolePermissionBatch(roleId, permissionIds);
    }

}
