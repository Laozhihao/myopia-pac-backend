package com.wupol.myopia.business.management.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.UserRequest;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.login.LoginInfoDTO;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/11
 **/
@FeignClient(name = "myopia-oauth", fallbackFactory = OauthServiceFallbackFactory.class)
public interface OauthServiceClient {

    /**
     * 获取用户列表（分页）
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/user/page")
    ApiResult<Page<UserDTO>> getUserListPage(@SpringQueryMap UserDTOQuery param);

    /**
     * 获取用户列表（仅支持用户名模糊查询）
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/user/list")
    ApiResult<List<UserDTO>> getUserList(@SpringQueryMap UserDTOQuery param);

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/oauth/user/batch/id")
    ApiResult<List<UserDTO>> getUserBatchByIds(@RequestParam("userIds") List<Integer> userIds);

    /**
     * 根据手机号码批量获取用户
     *
     * @param phones 手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/oauth/user/batch/phone")
    ApiResult<List<UserDTO>> getUserBatchByPhones(@RequestParam("phones") List<String> phones, @RequestParam("systemCode") Integer systemCode);

    /**
     * 根据手机号码批量获取用户
     *
     * @param idCards 身份证号码集
     * @param systemCode 系统编号
     * @param orgId 机构ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/oauth/user/batch/idCard")
    ApiResult<List<UserDTO>> getUserBatchByIdCards(@RequestParam("idCards") List<String> idCards, @RequestParam("systemCode") Integer systemCode, @RequestParam("orgId") Integer orgId);

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
    ApiResult<List<UserDTO>> addScreeningUserBatch(@RequestBody List<UserDTO> param);

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
     * @param password 密码
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @PutMapping("/oauth/user/password/{userId}")
    ApiResult<UserDTO> resetPwd(@PathVariable("userId") Integer userId, @RequestParam("password") String password);

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.base.domain.ApiResult<com.wupol.myopia.business.management.domain.dto.UserDTO>
     **/
    @GetMapping("/oauth/user/{userId}")
    ApiResult<UserDTO> getUserDetailByUserId(@PathVariable("userId") Integer userId);

    /**
     * 统计
     *
     * @param queryParam 查询条件
     * @return java.lang.Integer
     **/
    @GetMapping("/oauth/user/count")
    ApiResult<Integer> count(@SpringQueryMap UserDTO queryParam);

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/role/list")
    ApiResult<List<RoleDTO>> getRoleList(@SpringQueryMap RoleDTO param);

    @PostMapping("/oauth/role")
    ApiResult<RoleDTO> addRole(@RequestBody RoleDTO param);

    @PutMapping("/oauth/role")
    ApiResult<RoleDTO> updateRole(@RequestBody RoleDTO param);

    @PostMapping("/oauth/role/permission/{roleId}")
    ApiResult assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionIds);

    @GetMapping("/oauth/role/{roleId}")
    ApiResult<RoleDTO> getRoleById(@PathVariable("roleId") Integer roleId);

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId 角色ID
     * @param templateType 模板类型
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/role/permission/structure/{roleId}/{templateType}")
    ApiResult<List<PermissionDTO>> getRolePermissionTree(@PathVariable("roleId") Integer roleId, @PathVariable("templateType") Integer templateType);

    /**
     * 获取权限列表
     *
     * @param param
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/oauth/permission/list")
    ApiResult<List<PermissionDTO>> getPermissionList(@SpringQueryMap PermissionDTO param);

    @PostMapping("/oauth/permission")
    ApiResult<PermissionDTO> addPermission(@RequestBody PermissionDTO param);

    @PutMapping("/oauth/permission")
    ApiResult<PermissionDTO> modifyPermission(@RequestBody PermissionDTO param);

    @DeleteMapping("/oauth/permission/{permissionId}")
    ApiResult deletePermission(@PathVariable("permissionId") Integer permissionId);

    @GetMapping("/oauth/user/getByIds")
    ApiResult<List<UserDTO>> getUserByIds(@SpringQueryMap UserRequest request);

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/oauth/districtPermission/{templateType}")
    ApiResult<List<PermissionDTO>> getPermissionTemplate(@PathVariable("templateType") Integer templateType);

    /**
     * 根据模板类型获取模板权限的ID集
     *
     * @param templateType 模板类型
     * @return com.wupol.myopia.base.domain.ApiResult<java.util.List<java.lang.Integer>>
     **/
    @GetMapping("/oauth/districtPermission/list/{templateType}")
    ApiResult<List<Integer>> getPermissionTemplateIdList(@PathVariable("templateType") Integer templateType);

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @PutMapping("/oauth/districtPermission/{templateType}")
    ApiResult<Boolean> updatePermissionTemplate(@PathVariable("templateType") Integer templateType, @RequestBody List<Integer> permissionIds);

    /**
     * 根据手机号码批量获取用户
     *
     * @param systemCode 系统编号
     * @param orgIds     机构Ids
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/oauth/user/batch/orgIds")
    ApiResult<List<UserDTO>> getUserBatchByOrgIds(@RequestParam("orgIds") List<Integer> orgIds, @RequestParam("systemCode") Integer systemCode);

    /**
     * 登录
     *
     * @param clientId 客户端ID
     * @param clientSecret 客户端秘钥
     * @param username 用户名
     * @param password 密码
     * @return com.wupol.myopia.base.domain.ApiResult<com.wupol.myopia.business.management.domain.dto.login.LoginInfoDTO>
     **/
    @PostMapping("/login")
    ApiResult<LoginInfoDTO> login(@RequestParam("client_id") String clientId, @RequestParam("client_secret") String clientSecret, @RequestParam("username") String username, @RequestParam("password") String password);
}
