package com.wupol.myopia.business.management.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.UserRequest;
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
    public Page<UserDTO> getUserListPage(UserDTOQuery param) {
        return null;
    }

    /**
     * 获取用户列表（仅支持用户名模糊查询）
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @OauthRequest
    public List<UserDTO> getUserList(UserDTOQuery param) {
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
     * 根据用户ID集批量获取用户
     *
     * @param phones 手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @OauthRequest
    public List<UserDTO> getUserBatchByPhones(List<String> phones, Integer systemCode) {
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
    public List<UserDTO> addScreeningUserBatch(List<UserDTO> param){
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
     * 重置管理端用户的密码
     *
     * @param userId 用户ID
     * @param password 密码
     **/
    @OauthRequest
    public UserDTO resetPwd(Integer userId, String password){
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
     * 统计
     *
     * @param queryParam 查询条件
     * @return java.lang.Integer
     **/
    @OauthRequest
    public Integer count(UserDTO queryParam) {
        return null;
    }

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     **/
    @OauthRequest
    public List<RoleDTO> getRoleList(RoleDTO param){
        return null;
    }

    @OauthRequest
    public RoleDTO addRole(RoleDTO param){
        return null;
    }

    @OauthRequest
    public RoleDTO updateRole(RoleDTO param){
        return null;
    }

    @OauthRequest
    public Object assignRolePermission(Integer roleId, List<Integer> permissionIds){
        return null;
    }

    @OauthRequest
    public RoleDTO getRoleById(Integer roleId) {
        return null;
    }

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     * @param templateType 模板类型
     **/
    @OauthRequest
    public List<PermissionDTO> getRolePermissionTree(Integer roleId, Integer templateType){
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
    public PermissionDTO addPermission(PermissionDTO param){
        return null;
    }

    @OauthRequest
    public PermissionDTO modifyPermission(PermissionDTO param){
        return null;
    }

    @OauthRequest
    public Object deletePermission(Integer permissionId){
        return null;
    }

    @OauthRequest
    public List<UserDTO> getUserByIds(UserRequest request){
        return null;
    }

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @OauthRequest
    public List<PermissionDTO> getPermissionTemplate(Integer templateType) {
        return null;
    }

    /**
     * 根据模板类型获取模板权限的ID集
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.DistrictPermission>
     **/
    @OauthRequest
    public List<Integer> getPermissionTemplateIdList(Integer templateType) {
        return null;
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @OauthRequest
    public Boolean updatePermissionTemplate(Integer templateType, List<Integer> permissionIds) {
        return null;
    }


}
