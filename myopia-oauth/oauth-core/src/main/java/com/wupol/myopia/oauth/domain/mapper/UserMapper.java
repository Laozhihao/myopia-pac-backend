package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 获取用户列表（带有角色信息）
     *
     * @param queryParam 查询条件
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    IPage<UserWithRole> selectUserListWithRole(@Param("page") IPage<?> page, @Param("param") UserDTO queryParam);

    List<UserWithRole> selectUserListWithRole(@Param("param") UserDTO queryParam);

    List<User> selectUserList(@Param("param") UserDTO queryParam);

    User selectByOrgId(@Param("orgId") Integer orgId);
}
