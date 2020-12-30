package com.wupol.myopia.business.management.client;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.domain.dto.Permission;
import com.wupol.myopia.business.management.domain.dto.Role;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/11
 **/
@FeignClient(name ="myopia-oauth", fallback = OauthServiceFallback.class)
public interface OauthServiceClient {

    /**
     * 获取用户列表（分页）
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/user/list")
    ApiResult getUserListPage(@SpringQueryMap UserDTO param);

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch")
    ApiResult<List<UserDTO>> getUserBatchByIds(@RequestBody List<Integer> userIds);

    /**
     * 新增用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/oauth/user")
    ApiResult<UserDTO> addUser(@RequestBody UserDTO param);

    /**
     * 管理端创建医院端、学校端、筛查端的管理员
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/oauth/user/admin")
    ApiResult<UserDTO> addAdminUser(@RequestBody UserDTO param);

    /**
     * 批量新增筛查人员
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/oauth/user/screening/batch")
    ApiResult<List<Integer>> addScreeningUserBatch(@RequestBody List<UserDTO> param);

    /**
     * 更新用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PutMapping("/oauth/user")
    ApiResult<UserDTO> modifyUser(@RequestBody UserDTO param);

    /**
     * 重置管理端用户的密码【其他端用户的不适合】
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PutMapping("/oauth/user/password/{userId}")
    ApiResult resetPwd(@PathVariable("userId") Integer userId);

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/role/list")
    ApiResult getRoleList(@SpringQueryMap Role param);

    @PostMapping("/oauth/role")
    ApiResult addRole(@RequestBody Role param);

    @PutMapping("/oauth/role")
    ApiResult modifyRole(@RequestBody Role param);

    @PostMapping("/oauth/role/permission/{roleId}")
    ApiResult assignRolePermission(@PathVariable("roleId") Integer roleId);

    @GetMapping("/oauth/role/permission/structure/{roleId}")
    ApiResult getRolePermissionTree(@PathVariable("roleId") Integer roleId);

    /**
     * 获取角色列表
     * @param param
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/permission/list")
    ApiResult getPermissionList(@SpringQueryMap Permission param);

    @PostMapping("/oauth/permission")
    ApiResult addPermission(@RequestBody Permission param);

    @PutMapping("/oauth/permission")
    ApiResult modifyPermission(@RequestBody Permission param);

    @DeleteMapping("/oauth/permission/{permissionId}")
    ApiResult deletePermission(@PathVariable("permissionId") Integer permissionId);
}
