package com.wupol.myopia.oauth.domain.mapper;

import com.wupol.myopia.oauth.domain.model.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    Integer insertRolePermissionBatch(@Param("roleId") Integer roleId, @Param("permissionIds") List<Integer> permissionIds);
}
