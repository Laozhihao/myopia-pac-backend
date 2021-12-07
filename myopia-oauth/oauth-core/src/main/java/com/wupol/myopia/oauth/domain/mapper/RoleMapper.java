package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.oauth.domain.dto.RoleDTO;
import com.wupol.myopia.oauth.domain.model.Role;
import org.apache.ibatis.annotations.Param;

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
    List<Role> selectRoleList(@Param("param") RoleDTO query);

    /**
     * 获取角色列表 - 分页
     *
     * @param page  分页
     * @param query 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.Role>
     **/
    IPage<Role> selectRoleList(@Param("page") IPage<?> page, @Param("param") RoleDTO query);


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

    /**
     * 获取指定筛查机构的第一个角色
     *
     * @param screeningOrgId 筛查机构ID
     * @param systemCode 系统编号
     * @param roleType 角色类型
     * @return com.wupol.myopia.oauth.domain.model.Role
     **/
    Role getFirstOneRoleByScreeningOrgId(@Param("screeningOrgId") Integer screeningOrgId, @Param("systemCode") Integer systemCode, @Param("roleType") Integer roleType);
}
