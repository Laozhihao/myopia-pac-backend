package com.wupol.myopia.oauth.domain.mapper;

import com.wupol.myopia.oauth.domain.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
    List<Role> selectRoleList(Role query);

    /**
     * 通过ID集批量获取角色
     *
     * @param ids 角色ID集
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> getByIds(List<Integer> ids);

    /**
     * 获取指定用户的角色
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Role>
     **/
    List<Role> getRoleListByUserId(Integer userId);

}
