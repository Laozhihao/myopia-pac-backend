package com.wupol.myopia.business.management.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.UserRequest;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.client.aop.annotation.OauthRequest;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 请求Oauth类
 * @author Chikong
 * @date 2021-01-18
 */
@Log4j2
@Service
public class OauthService {

    /**
     * 获取用户列表（分页）
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @OauthRequest
    public Page<UserDTO> getUserListPage(UserDTOQuery param){
        return null;
    }

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @OauthRequest
    public List<UserDTO> getUserBatchByIds(List<Integer> userIds) {
        return null;
    }

    /**
     * 新增用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @OauthRequest
    public UserDTO addUser(UserDTO param){
        return null;
    }

    /**
     * 管理端创建医院端、学校端、筛查端的管理员
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @OauthRequest
    public UserDTO addAdminUser(UserDTO param){
        return null;
    }

    /**
     * 批量新增筛查人员
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @OauthRequest
    public List<Integer> addScreeningUserBatch(List<UserDTO> param){
        return null;
    }

    /**
     * 更新用户
     *
     * @param param 用户数据
     **/
    @OauthRequest
    public UserDTO modifyUser(UserDTO param){
        return null;
    }

    /**
     * 重置管理端用户的密码【其他端用户的不适合】
     *
     * @param userId 用户ID
     **/
    @OauthRequest
    public ApiResult resetPwd(Integer userId){
        return null;
    }

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     **/
    @OauthRequest
    public UserDTO getUserDetailByUserId(Integer userId){
        return null;
    }

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     **/
    @OauthRequest
    public ApiResult getRoleList(RoleDTO param){
        return null;
    }

    @OauthRequest
    public ApiResult addRole(RoleDTO param){
        return null;
    }

    @OauthRequest
    public ApiResult updateRole(RoleDTO param){
        return null;
    }

    @OauthRequest
    public ApiResult assignRolePermission(Integer roleId, List<Integer> permissionIds){
        return null;
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     **/
    @OauthRequest
    public List<PermissionDTO> getRolePermissionTree(Integer roleId){
        return null;
    }

    /**
     * 获取权限列表
     *
     * @param param
     **/
    @OauthRequest
    public List<PermissionDTO> getPermissionList(PermissionDTO param){
        return null;
    }

    @OauthRequest
    public Object addPermission(PermissionDTO param){
        return null;
    }

    @OauthRequest
    public Object modifyPermission(PermissionDTO param){
        return null;
    }

    @OauthRequest
    public Object deletePermission(Integer permissionId){
        return null;
    }

    @OauthRequest
    public List<UserDTO> getUserByIdCard(UserRequest request){
        return null;
    }


}
