package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限资源表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface PermissionMapper extends BaseMapper<Permission> {
    /**
     * 根据用户ID获取用户的所有权限
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    List<Permission> getUserPermissionByUserId(Integer userId);

    /**
     * 获取权限资源树
     *
     * @param pid 父权限ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    List<Permission> selectPermissionTree(Integer pid);

    /**
     * 获取指定角色的权限树（基于指定行政区等级权限）
     *
     * @param pid
     * @param roleId
     * @param districtLevel
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    List<Permission> selectRolePermissionTree(@Param("pid")Integer pid, @Param("roleId")Integer roleId, @Param("districtLevel")Integer districtLevel);

    List<Permission> selectAdminRolePermissionTree(@Param("pid")Integer pid, @Param("roleId")Integer roleId);
}
