package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.PermissionMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class PermissionService extends BaseService<PermissionMapper, Permission> {
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    /**
     * 根据用户ID获取用户的所有权限
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getUserPermissionByUserId(Integer userId) {
        if (Objects.isNull(userId)) {
            return new ArrayList<>();
        }
        return baseMapper.getUserPermissionByUserId(userId);
    }

    /**
     * 获取整棵权限资源树
     *
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getAllPermissionTree() {
        return baseMapper.selectPermissionTree(0);
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param pid 父权限ID
     * @param roleId 角色ID
     * @param districtLevel 行政区ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> selectRoleAllTree(Integer pid, Integer roleId, Integer districtLevel) {
        return baseMapper.selectRolePermissionTree(pid, roleId, districtLevel);
    }
}
