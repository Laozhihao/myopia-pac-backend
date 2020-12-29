package com.wupol.myopia.business.management.client;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.domain.dto.Permission;
import com.wupol.myopia.business.management.domain.dto.Role;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

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
     * 新增用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PostMapping("/oauth/user")
    ApiResult addUser(@RequestBody UserDTO param);

    /**
     * 更新用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PutMapping("/oauth/user")
    ApiResult modifyUser(@RequestBody UserDTO param);

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PutMapping("/oauth/password/{userId}")
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
