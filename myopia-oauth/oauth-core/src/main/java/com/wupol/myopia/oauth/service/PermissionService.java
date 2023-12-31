package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.RegExpUtil;
import com.wupol.myopia.oauth.constant.OrgScreeningMap;
import com.wupol.myopia.oauth.domain.mapper.PermissionMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class PermissionService extends BaseService<PermissionMapper, Permission> {

    /**
     * 根据用户ID获取用户的所有权限
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getUserDistinctPermissionByUserId(Integer userId) {
        if (Objects.isNull(userId)) {
            return Collections.emptyList();
        }
        List<Permission> permissions = baseMapper.getUserPermissionByUserId(userId);
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        return permissions.stream().distinct().collect(Collectors.toList());
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

    /**
     * 获取平台管理员的角色权限树（全量）
     *
     * @param pid
     * @param roleId
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getAdminRolePermissionTree(Integer pid, Integer roleId) {
        return baseMapper.selectAdminRolePermissionTree(pid, roleId);
    }

    /**
     * 校验参数
     *
     * @param param 参数
     * @return void
     **/
    public void validateParam(Permission param) {
        if (!StringUtils.isEmpty(param.getMenuBtnName())) {
            List<Permission> exist = findByList(new Permission().setMenuBtnName(param.getMenuBtnName()));
            Assert.isTrue(CollectionUtils.isEmpty(exist), "该页面或按钮name已经存在");
        }
        Assert.isTrue(param.getIsPage() == 1 || RegExpUtil.isApiUrl(param.getApiUrl()), "apiUrl参数格式错误");
    }

    /**
     * 获取指定角色的权限
     *
     * @param roleIds 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    public List<Permission> getUsablePermissionByRoleIds(List<Integer> roleIds) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        return baseMapper.selectByRoleIds(roleIds);
    }

    /**
     * 通过筛查结构的ConfigType获取权限
     *
     * @param configType 配置
     * @return List<String>
     */
    public List<String> getPermissionByConfigType(Integer configType) {
        return baseMapper.getPermissionByDistrictLevel(OrgScreeningMap.ORG_CONFIG_TYPE_TO_TEMPLATE.get(configType));
    }

}
