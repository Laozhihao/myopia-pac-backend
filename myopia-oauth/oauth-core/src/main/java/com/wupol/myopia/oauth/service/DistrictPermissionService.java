package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.constant.PermissionTemplateType;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.DistrictPermissionMapper;
import com.wupol.myopia.oauth.domain.model.DistrictPermission;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictPermissionService extends BaseService<DistrictPermissionMapper, DistrictPermission> {

    @Autowired
    private RoleService roleService;

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> selectTemplatePermissionTree(Integer templateType) {
        Assert.notNull(templateType, "模板类型不能为空");
        return baseMapper.selectTemplatePermissionTree(0, templateType);
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermissionTemplate(Integer templateType, List<Integer> permissionIds) {
        Assert.notNull(permissionIds, "模板的权限不能为null");
        remove(new DistrictPermission().setDistrictLevel(templateType));
        List<DistrictPermission> permissions = permissionIds.stream().distinct().map(x -> new DistrictPermission().setDistrictLevel(templateType).setPermissionId(x)).collect(Collectors.toList());
        boolean success = saveBatch(permissions);
        // 同步更新筛查管理端角色权限
        if (PermissionTemplateType.SCREENING_ORGANIZATION.getType().equals(templateType)) {
            Role role = roleService.findOne(new Role().setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode()));
            roleService.assignRolePermission(role.getId(), permissionIds);
        }
        return success;
    }
}
