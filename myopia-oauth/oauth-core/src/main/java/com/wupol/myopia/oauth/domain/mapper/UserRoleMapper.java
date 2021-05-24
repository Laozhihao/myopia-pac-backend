package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.oauth.domain.model.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {
   Boolean deleteByRoleIds(@Param("userId") Integer userId, @Param("roleIds") List<Integer> existRoleList);
   Boolean insertBatch(@Param("userRoles") List<UserRole> userRoleList);

}
