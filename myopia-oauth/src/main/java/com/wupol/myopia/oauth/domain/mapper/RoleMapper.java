package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.model.Role;

import java.util.List;

/**
 * 角色表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 获取角色列表
     *
     * @param query 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> selectRoleList(RoleDTO query);

    /**
     * 查询指定用户的角色
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> selectRoleListByUserId(Integer userId);

    /**
     * 查询用户ID列表
     *
     * @param query 查询条件
     * @return java.util.List<java.lang.Integer>
     **/
    List<Integer> selectUserIdList(Role query);

}
